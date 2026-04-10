package net.astellismodding.golfwithmates.entity.custom;

import java.util.List;
import net.astellismodding.golfwithmates.block.custom.GolfBallBlock;
import net.astellismodding.golfwithmates.block.custom.GolfCupBlock;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.entity.ModEntities;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.PathNode;
import net.astellismodding.golfwithmates.util.ShotResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class GolfBallEntity extends Entity implements IEntityWithComplexSpawn {

    /**
     * Visual speed in blocks per tick. Keeps animation duration proportional to shot distance
     * regardless of STEP_SIZE or path density. 0.5 b/t = ~10 blocks/sec at 20 tps.
     */
    private static final double VISUAL_SPEED_BLOCKS_PER_TICK = 0.25;

    private ShotResult shotResult = ShotResult.empty();
    private int pathIndex = 0;
    private int nodesPerTick = 1;
    private Component customName = Component.literal("Default Name");
    private int puttCounter = 0;

    public GolfBallEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * Factory — call this instead of the constructor.
     * Sets spawn position, shot data, and computes playback speed.
     */
    public static GolfBallEntity create(Level level, Vec3 spawnPos, ShotResult shot,
                                        Component name, int putts) {
        GolfBallEntity entity = new GolfBallEntity(ModEntities.GOLF_BALL_ENTITY.get(), level);
        entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        entity.shotResult = shot;
        entity.customName = name;
        entity.puttCounter = putts;
        entity.nodesPerTick = computeNodesPerTick(shot);
        return entity;
    }

    private static int computeNodesPerTick(ShotResult shot) {
        List<PathNode> path = shot.path;
        if (path.size() < 2) return 1;
        double totalDist = 0;
        for (int i = 1; i < path.size(); i++) {
            totalDist += path.get(i).position.distanceTo(path.get(i - 1).position);
        }
        double avgNodeSpacing = totalDist / (path.size() - 1);
        if (avgNodeSpacing < 1e-6) return 1;
        return Math.max(1, (int) (VISUAL_SPEED_BLOCKS_PER_TICK / avgNodeSpacing));
    }

    // -------------------------------------------------------------------------
    // Tick — advance along path, place block when done
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        pathIndex += nodesPerTick;

        if (pathIndex >= shotResult.path.size()) {
            if (!level().isClientSide) {
                placeBlock();
                discard();
            }
            return;
        }

        PathNode node = shotResult.path.get(pathIndex);
        setPos(node.position.x, node.position.y, node.position.z);
    }

    /**
     * Finds the nearest valid resting position at or below startPos.
     * Scans downward up to 16 blocks to find a position that is air with a solid block beneath it.
     * Returns startPos unchanged if no better position is found (e.g. over the void).
     */
    private BlockPos findGroundedPos(ServerLevel level, BlockPos startPos) {
        if (isValidRestPos(level, startPos)) return startPos;
        for (int dy = 1; dy <= 16; dy++) {
            BlockPos candidate = startPos.below(dy);
            if (isValidRestPos(level, candidate)) return candidate;
            if (!level.getBlockState(candidate).isAir()) break; // hit a solid block mid-scan
        }
        return startPos;
    }

    private boolean isValidRestPos(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && !level.getBlockState(pos.below()).isAir();
    }

    /**
     * Called on the server when the entity reaches the end of the path.
     * Places the GolfBallBlock at the REST position and restores all metadata.
     * Also checks for hole-in.
     */
    private void placeBlock() {
        PathNode restNode = shotResult.getRestNode();
        if (restNode == null || !(level() instanceof ServerLevel serverLevel)) return;

        Vec3 restPos = restNode.position;
        BlockPos targetPos = findGroundedPos(serverLevel, BlockPos.containing(restPos));

        if (!serverLevel.getBlockState(targetPos).isAir()) return;
        if (!serverLevel.getWorldBorder().isWithinBounds(targetPos)) return;

        int newSubX = Math.min(2, (int) ((restPos.x - Math.floor(restPos.x)) * 3));
        int newSubZ = Math.min(2, (int) ((restPos.z - Math.floor(restPos.z)) * 3));

        BlockState ballState = ModBlocks.GOLF_BALL.get().defaultBlockState();
        serverLevel.setBlock(targetPos, ballState, 3);

        if (serverLevel.getBlockEntity(targetPos) instanceof GolfBallBlockEntity newBE) {
            newBE.setCustomName(customName);
            newBE.setPuttCounter(puttCounter);
            newBE.setShotResult(shotResult);
            newBE.setSubPos(newSubX, newSubZ);
            newBE.setActive(true);
            newBE.setChanged();
        }

        // Check for hole-in
        if (serverLevel.getBlockState(targetPos.below()).getBlock() instanceof GolfCupBlock) {
            serverLevel.playSeededSound(null,
                    targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                    ModSounds.GolfScore, SoundSource.BLOCKS, 1f, 1f, 0);
            if (serverLevel.getBlockEntity(targetPos) instanceof GolfBallBlockEntity be
                    && ModBlocks.GOLF_BALL.get() instanceof GolfBallBlock ballBlock) {
                ballBlock.TransferToCup(be, targetPos, serverLevel);
            }
        }
    }

    // -------------------------------------------------------------------------
    // IEntityWithComplexSpawn — sends ShotResult to client on entity spawn
    // -------------------------------------------------------------------------

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(shotResult.toNbt());
        buf.writeInt(nodesPerTick);
        buf.writeUtf(customName.getString());
        buf.writeInt(puttCounter);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        shotResult = tag != null ? ShotResult.fromNbt(tag) : ShotResult.empty();
        nodesPerTick = buf.readInt();
        customName = Component.literal(buf.readUtf());
        puttCounter = buf.readInt();
    }

    // -------------------------------------------------------------------------
    // NBT — server-side persistence (chunk unload / server restart fallback)
    // -------------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!shotResult.path.isEmpty()) {
            tag.put("ShotResult", shotResult.toNbt());
        }
        tag.putInt("PathIndex", pathIndex);
        tag.putInt("NodesPerTick", nodesPerTick);
        tag.putString("CustomName", customName.getString());
        tag.putInt("PuttCounter", puttCounter);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ShotResult")) {
            shotResult = ShotResult.fromNbt(tag.getCompound("ShotResult"));
        }
        pathIndex = tag.getInt("PathIndex");
        nodesPerTick = Math.max(1, tag.getInt("NodesPerTick"));
        customName = Component.literal(tag.getString("CustomName"));
        puttCounter = tag.getInt("PuttCounter");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced data needed — ShotResult is sent once via spawn packet
    }
}