package net.astellismodding.golfwithmates.block.custom;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.astellismodding.golfwithmates.network.OpenCupScreenPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.network.PacketDistributor;
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
        // BER handles all rendering so it can swap in the disguise block model dynamically
        return RenderShape.ENTITYBLOCK_ANIMATED;
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

    public boolean InsertBall(BlockEntity blockEntity, ItemStack stack, BlockPos pos, Level level) {
        boolean result = false;
        if (level.getBlockEntity(pos.below()) instanceof GolfCupBlockEntity golfCupBlockEntity) {
            if (golfCupBlockEntity.inventory.getStackInSlot(0).isEmpty()) {
                golfCupBlockEntity.inventory.insertItem(0, stack.copy(), false);
                result = true;
            }
        }
        GolfBallBlockEntity ball = (GolfBallBlockEntity) blockEntity;
        displayWin(ball.getPuttCounter(), ball.getCustomName(), pos, level);
        return result;
    }

    private void displayWin(int strokes, Component playerName, BlockPos pos, Level level) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        if (!(level.getBlockEntity(pos.below()) instanceof GolfCupBlockEntity golfCup)) return;

        int par = golfCup.getGolfPar();

        // Fireworks
        if (strokes == 1 || strokes <= par) {
            int count = strokes == 1 ? 5 : 2;
            int delay = strokes == 1 ? 20 : 10;
            golfCup.startCelebration(count, delay);
        }

        // Score label
        String label;
        if (strokes == 1) {
            label = "Hole-in-one!";
        } else {
            int diff = strokes - par;
            label = switch (diff) {
                case -2 -> "Eagle!";
                case -1 -> "Birdie!";
                case  0 -> "Par";
                case  1 -> "Bogey";
                case  2 -> "Double Bogey";
                default -> diff > 0 ? "+" + diff : String.valueOf(diff);
            };
        }

        // Build message: "PlayerName — Birdie! at The Links (2 strokes, par 3)"
        String course = golfCup.getCourseName();
        String courseText = course.isEmpty() ? "" : " at " + course;
        String strokeWord = strokes == 1 ? "stroke" : "strokes";
        Component message = Component.literal(
                playerName.getString() + " — " + label + courseText
                + " (" + strokes + " " + strokeWord + ", par " + par + ")");

        serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
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

        // Shift + right-click — open settings GUI
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp
                    && level.getBlockEntity(pos) instanceof GolfCupBlockEntity be) {
                PacketDistributor.sendToPlayer(sp,
                        new OpenCupScreenPayload(pos, be.getCourseName(), be.getGolfPar(), be.getDisguiseBlock()));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Right-click with block item — set as disguise
        if (stack.getItem() instanceof BlockItem bi && (!bi.getBlock().asItem().equals(ModBlocks.GOLF_BALL.get().asItem()))) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GolfCupBlockEntity be) {
                    be.setDisguiseBlock(BuiltInRegistries.BLOCK.getKey(bi.getBlock()).toString());
                    player.displayClientMessage(
                            Component.literal("Disguise set: " + bi.getBlock().getName().getString()), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

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
