package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    public boolean InsertBall(BlockEntity blockEntity, ItemStack stack, BlockPos pos,Level level) {
        Boolean Result = false;
        if (level.getBlockEntity(pos.below()) instanceof GolfCupBlockEntity golfCupBlockEntity) {
            if (golfCupBlockEntity.inventory.getStackInSlot(0).isEmpty()) {
                golfCupBlockEntity.inventory.insertItem(0, stack.copy(), false);
                Result = true;
            }
        }
        DisplayWin(((GolfBallBlockEntity) blockEntity).getPuttCounter(),pos, level);
        return Result;
    }

    private void DisplayWin(int puttCounter, BlockPos pos, Level level) {
        // The entity spawning logic now runs on the server, so we don't need to call it from the client.
        if (level.isClientSide()) {
            return;
        }

        if (level.getBlockEntity(pos.below()) instanceof GolfCupBlockEntity golfCup) {
            Integer par = golfCup.getGolfPar();

            if (par != null) {
                if (puttCounter == 1) {

                    golfCup.startCelebration(5, 20);
                } else if (puttCounter <= par) {
                    golfCup.startCelebration(2, 10);
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // We only want this to run on the server side to prevent ghost entities.
        if (!level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.GOLF_CLUP_BE.get(),
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {

        GolfCupBlockEntity targetEntity = (GolfCupBlockEntity) level.getBlockEntity(pos);

        if(targetEntity != null) {
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
