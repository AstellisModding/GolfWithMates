package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.ClubUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.astellismodding.golfwithmates.util.ClubUtils.*;

public class GolfBallBlock extends FallingBlock {

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
    protected MapCodec<? extends FallingBlock> codec() {
        return null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CustomBoundingBox;
    }

    //todo Feat: Add logic for rebounds
    //todo Feat: Add partical trace or maybe entity, visual of ball moving?
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (isClub(new ItemStack(player.getMainHandItem().getItem()))){
            float roty = ((player.getYRot() % 360 + 360) % 360);
            ItemStack club = player.getMainHandItem();

            Vec3 TargetLocation = ClubUtils.CalculateHitResultLocation(pos.getX(), pos.getZ(), roty,  club.get(ModDataComponent.put_power).value(), 1);

            if (level.isClientSide) {
                level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.GolfPutt, SoundSource.BLOCKS, 1f, 1f, 0);
            }
            level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.GolfPutt, SoundSource.BLOCKS, 1f, 1f, 0);

            this.teleport(TargetLocation, state, level, pos);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        //maybe we add the ability to nudge the ball with the hands ?
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void teleport(Vec3 TargetLocation, BlockState state, Level level, BlockPos pos) {
        WorldBorder worldborder = level.getWorldBorder();

        for (int i = 0; i < 1000; ++i) {

            Vec3 offsetvec = pos.getCenter().subtract(TargetLocation);
            int Targetx = (int) offsetvec.x;
            int Targety = 0; //(int) offsetvec.y; <- not required for this tester
            int Targetz = (int) offsetvec.z;
            BlockPos targetpos = pos.offset(Targetx, Targety, Targetz);

            if (level.getBlockState(targetpos).isAir() && worldborder.isWithinBounds(targetpos)) {
                if (level.isClientSide) {
                    //client side 4-6 seconds display ?
                    for (int j = 0; j < 128; ++j) {

                        //unsure
                        double d0 = level.random.nextDouble();

                        //adding fuzzieness to the particles? dunno what this is
                        float f = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float f1 = (level.random.nextFloat() - 0.5F) * 0.2F;
                        float f2 = (level.random.nextFloat() - 0.5F) * 0.2F;

                        //Particles, getting location and adding the particles
                        double d1 = Mth.lerp(d0, (double) targetpos.getX(), (double) pos.getX()) + (level.random.nextDouble() - (double) 0.5F) + (double) 0.5F;
                        double d2 = Mth.lerp(d0, (double) targetpos.getY(), (double) pos.getY()) + level.random.nextDouble() - (double) 0.5F;
                        double d3 = Mth.lerp(d0, (double) targetpos.getZ(), (double) pos.getZ()) + (level.random.nextDouble() - (double) 0.5F) + (double) 0.5F;
                        level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double) f, (double) f1, (double) f2);

                    }
                } else {

                    level.setBlock(targetpos, state, 2);
                    level.removeBlock(pos, false);
                }

                if (CheckHole(targetpos, level)){
                    level.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.GolfScore, SoundSource.BLOCKS, 1f, 1f, 0);
                    TransferToCup(targetpos, level);

                }


                return;
            }
        }
    }

    public void TransferToCup(BlockPos context, Level level) {
        BlockPos cuppos = context.below();
        Block CupBlock = level.getBlockState(cuppos).getBlock();
        ItemStack item = new ItemStack(this.asItem());
        Boolean wasInserted = false;
        //todo Feat: once block entity is setup for Ball you need to use the GetCloneItemStack method to copy the data across
        if (CupBlock instanceof GolfCupBlock) {
            wasInserted = ((GolfCupBlock) CupBlock).InsertBall(item, context, level);
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
