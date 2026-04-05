package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.astellismodding.golfwithmates.block.entity.BeamBlockEntity;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.ClubUtils;
import net.astellismodding.golfwithmates.util.PathNode;
import net.astellismodding.golfwithmates.util.ShotResult;
import net.astellismodding.golfwithmates.util.TrajectoryCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.astellismodding.golfwithmates.util.ClubUtils.*;

public class GolfBallBlock extends BaseEntityBlock {

    public static final MapCodec<GolfBallBlock> CODEC = simpleCodec(GolfBallBlock::new);
    private static final DirectionProperty PuttDirection = null;
    private static final VoxelShape CustomBoundingBox = Block.box(5, 0, 5, 11,5,11);
    int blockX = 0;
    int blockZ = 0;
    int blockY = 0;

    int OffsetX = 1;
    int OffsetY = 1;
    int OffsetZ = 1;

    public GolfBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GolfBallBlockEntity(blockPos, blockState);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CustomBoundingBox;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        GolfBallBlockEntity targetEntity = (GolfBallBlockEntity) level.getBlockEntity(pos);

        if (placer == null) {
            return;
        }

        if (targetEntity == null) {
            return;
        }
        Component input = placer.getDisplayName();
        targetEntity.setCustomName(input);
        // Shot result starts empty — will be populated when the ball is hit
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!isClub(new ItemStack(player.getMainHandItem().getItem()))) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.GolfPutt, SoundSource.BLOCKS, 1f, 1f, 0);

        if (!level.isClientSide) {
            GolfBallBlockEntity golfBall = (GolfBallBlockEntity) level.getBlockEntity(pos);
            if (golfBall == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            ItemStack club = player.getMainHandItem();

            // Use stored sub-cell position as the precise shot origin
            double startX = pos.getX() + (golfBall.getSubX() + 0.5) / 3.0;
            double startZ = pos.getZ() + (golfBall.getSubZ() + 0.5) / 3.0;
            Vec3 startPos = new Vec3(startX, pos.getY() + 0.25, startZ);

            float yaw = player.getYRot();
            double speed = ClubUtils.getVelocity(club) * ClubUtils.getMaxDistance(club) * 30;

            ShotResult result = TrajectoryCalculator.simulatePutterShot(startPos, yaw, speed, level);
            golfBall.setShotResult(result);
            golfBall.setActive(true);

            PathNode restNode = result.getRestNode();
            if (restNode != null) {
                // Compute sub-cell from the rest position and store before teleport
                Vec3 restPos = restNode.position;
                int newSubX = Math.min(2, (int)((restPos.x - Math.floor(restPos.x)) * 3));
                int newSubZ = Math.min(2, (int)((restPos.z - Math.floor(restPos.z)) * 3));
                golfBall.setSubPos(newSubX, newSubZ);

                BlockPos targetBlockPos = BlockPos.containing(restNode.position);
                teleportToResult(targetBlockPos, state, level, pos);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        //maybe we add the ability to nudge the ball with the hands ?
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void teleportToResult(BlockPos targetBlockPos, BlockState state, Level level, BlockPos pos) {
        WorldBorder worldborder = level.getWorldBorder();
        if (!level.getBlockState(targetBlockPos).isAir() || !worldborder.isWithinBounds(targetBlockPos)) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof GolfBallBlockEntity golfBallBE) {
            golfBallBE.IncrementPuttCounter();
            CompoundTag nbtData = golfBallBE.saveWithoutMetadata(level.registryAccess());
            level.setBlock(targetBlockPos, state, 2);
            if (level.getBlockEntity(targetBlockPos) instanceof GolfBallBlockEntity newGolfBallBE) {
                nbtData.putInt("x", targetBlockPos.getX());
                nbtData.putInt("y", targetBlockPos.getY());
                nbtData.putInt("z", targetBlockPos.getZ());
                newGolfBallBE.loadWithComponents(nbtData, level.registryAccess());
                newGolfBallBE.setChanged();
            }
            level.removeBlock(pos, false);
        }

        if (CheckHole(targetBlockPos, level)) {
            level.playSeededSound(null, targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ(), ModSounds.GolfScore, SoundSource.BLOCKS, 1f, 1f, 0);
            if (level.getBlockEntity(targetBlockPos) instanceof GolfBallBlockEntity golfBallBE) {
                TransferToCup(golfBallBE, targetBlockPos, level);
            }
        }
    }

    public void TransferToCup(BlockEntity blockEntity,BlockPos context, Level level) {
        BlockPos cuppos = context.below();
        Block CupBlock = level.getBlockState(cuppos).getBlock();
        ItemStack item = new ItemStack(this.asItem());
        Boolean wasInserted = false;
        if (CupBlock instanceof GolfCupBlock) {
            wasInserted = ((GolfCupBlock) CupBlock).InsertBall(blockEntity,item, context, level);
        }

        if (!wasInserted) {
            Block.popResource(level, context, item);
        }
        level.removeBlock(context, false);
    }

    public boolean CheckHole(BlockPos context, Level level) {
        BlockPos postion = context.below();
        Block BlockUnderBall = level.getBlockState(postion).getBlock();
        if ( BlockUnderBall instanceof GolfCupBlock ) {
            return true;
        }
        return false;
    }


    protected int getDelayAfterPlace() {
        return 5;
    }

}
