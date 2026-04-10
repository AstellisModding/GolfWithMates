package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.entity.custom.GolfBallEntity;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.ClubUtils;
import net.astellismodding.golfwithmates.util.ShotResult;
import net.astellismodding.golfwithmates.util.ShotType;
import net.astellismodding.golfwithmates.util.TrajectoryCalculator;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.astellismodding.golfwithmates.util.ClubUtils.*;

public class GolfBallBlock extends BaseEntityBlock {

    public static final MapCodec<GolfBallBlock> CODEC = simpleCodec(GolfBallBlock::new);

    // 9 hitbox shapes pre-built at class load — indexed by subZ * 3 + subX
    private static final VoxelShape[] SUB_CELL_SHAPES = buildSubCellShapes();

    private static VoxelShape[] buildSubCellShapes() {
        VoxelShape[] shapes = new VoxelShape[9];
        for (int sz = 0; sz < 3; sz++) {
            for (int sx = 0; sx < 3; sx++) {
                double cx = (sx + 0.5) * (16.0 / 3.0);
                double cz = (sz + 0.5) * (16.0 / 3.0);
                shapes[sz * 3 + sx] = Block.box(cx - 3, 0, cz - 3, cx + 3, 5, cz + 3);
            }
        }
        return shapes;
    }

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
        if (level.getBlockEntity(pos) instanceof GolfBallBlockEntity be) {
            return SUB_CELL_SHAPES[be.getSubZ() * 3 + be.getSubX()];
        }
        return SUB_CELL_SHAPES[4]; // default: centre cell (1,1)
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
        //return RenderShape.MODEL; results in model staying centred despite subspace location
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
            ShotType shotType = ClubUtils.getShotType(club);
            double rawPower = ClubUtils.getVelocity(club);
            float effectiveAngle = ClubUtils.getLaunchAngle(shotType, rawPower);

            // Compensate speed so horizontal distance stays proportional to power,
            // not to the shallower angle — cos(fullAngle)/cos(effectiveAngle) < 1
            // when angle has been reduced, pulling distance back in line.
            double speed = ClubUtils.applyPowerCurve(rawPower, shotType) * ClubUtils.getMaxDistance(club);
            if (shotType != ShotType.PUTTER) {
                float fullAngle = ClubUtils.getLaunchAngle(shotType, 1.0);
                double compensation = Math.cos(Math.toRadians(fullAngle)) / Math.cos(Math.toRadians(effectiveAngle));
                speed *= compensation;
            }

            ShotResult result;
            if (shotType == ShotType.PUTTER) {
                result = TrajectoryCalculator.simulatePutterShot(startPos, yaw, speed, level);
            } else {
                result = TrajectoryCalculator.simulateParabolicShot(startPos, yaw, speed, effectiveAngle, level);
            }

            // Capture metadata before removing the block
            int nextPuttCount = golfBall.getPuttCounter() + 1;

            // Spawn entity to animate along the path — it will place the block when done
            GolfBallEntity ballEntity = GolfBallEntity.create(
                    level, startPos, result, golfBall.getCustomName(), nextPuttCount);
            level.addFreshEntity(ballEntity);
            level.removeBlock(pos, false);
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        //maybe we add the ability to nudge the ball with the hands ?
        return InteractionResult.sidedSuccess(level.isClientSide);
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
