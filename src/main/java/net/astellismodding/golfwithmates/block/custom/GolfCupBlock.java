package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GolfCupBlock extends BaseEntityBlock {
    public static final MapCodec<GolfCupBlock> CODEC = simpleCodec(GolfCupBlock::new);
    public GolfCupBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GolfCupBlockEntity(blockPos,blockState);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof GolfCupBlockEntity golfCupBlockEntity){
                golfCupBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public boolean InsertBall(ItemStack stack, BlockPos pos,Level level) {
        if (level.getBlockEntity(pos.below()) instanceof GolfCupBlockEntity golfCupBlockEntity) {
            if (golfCupBlockEntity.inventory.getStackInSlot(0).isEmpty()) {
                golfCupBlockEntity.inventory.insertItem(0, stack.copy(), false);
                return true;
            }
        }
        return false;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {

        GolfCupBlockEntity targetEntity = (GolfCupBlockEntity) level.getBlockEntity(pos);

        if(targetEntity != null) {
            /*Give the ball -- Blocked out because we dont need to manually put things into the hole...
            if(targetEntity.inventory.getStackInSlot(0).isEmpty() && !stack.isEmpty()) {
                targetEntity.inventory.insertItem(0, stack.copy(), false);
                stack.shrink(1);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
             }
            */
            //Take the ball
            if(!targetEntity.inventory.getStackInSlot(0).isEmpty()) {

                ItemStack CupItem = targetEntity.inventory.getStackInSlot(0);
                ItemStack HandItem = player.getMainHandItem();
                Integer HandItemMax  = HandItem.getMaxStackSize();
                Integer HandItemCurrent = HandItem.getCount();

                Boolean testcase1 = (HandItem.getItem().equals(ModBlocks.GOLF_BALL.get().asItem()));
                Boolean testcase2 = (!HandItemCurrent.equals(HandItemMax));


                if ( stack.isEmpty() || testcase1 && testcase2) {
                    ItemStack stackOnPedestal = targetEntity.inventory.extractItem(0, 1, false);

                    if (!stack.isEmpty()) {
                        stackOnPedestal.setCount( stackOnPedestal.getCount() + player.getMainHandItem().getCount());
                    }

                    player.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                    targetEntity.clearContents();
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);

                }

            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}
