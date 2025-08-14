package net.astellismodding.golfwithmates.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class GolfCupBlockEntity extends BlockEntity {
    private int GolfPar = 3;

    private int celebrationTicks = 0;
    private int fireworksLaunched = 0;

    private static int FIREWORKS_TO_LAUNCH = 5;
    private static int DELAY_BETWEEN_FIREWORKS = 10; // 10 ticks = 0.5 seconds

    // Add your existing constructors and other methods here...

    public void startCelebration(int fireworkcount, int delay) {
        FIREWORKS_TO_LAUNCH = fireworkcount;
        DELAY_BETWEEN_FIREWORKS = delay;
        // Kicks off the celebration sequence from your DisplayWin method.
        if (this.celebrationTicks <= 0) {
            this.fireworksLaunched = 0;
            this.celebrationTicks = (FIREWORKS_TO_LAUNCH * DELAY_BETWEEN_FIREWORKS) + 20;
            this.setChanged(); // Mark for saving
        }
    }



    // This method will be called once per tick by the game.
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (this.celebrationTicks > 0) {
            this.celebrationTicks--;

            // Use the modulo operator to check if it's time to launch a firework.
            if (this.celebrationTicks % DELAY_BETWEEN_FIREWORKS == 0) {
                if (this.fireworksLaunched < FIREWORKS_TO_LAUNCH) {
                    spawnRandomFirework(level, pos);
                    this.fireworksLaunched++;
                }
            }
        }
    }

    private static void spawnRandomFirework(Level level, BlockPos pos) {
        int[] colors = new int[]{0xFFD700, 0xFF8C00, 0xFF4500, 0xDC143C, 0xC71585};
        double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3.0;
        double y = pos.getY() + 1.5; // Spawn a bit above the cup itself
        double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3.0;
        int color = colors[level.random.nextInt(colors.length)];

        // --- The flexible firework spawning logic from your previous code ---
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkExplosion explosion = new FireworkExplosion(
                FireworkExplosion.Shape.SMALL_BALL,
                new IntArrayList(new int[]{color}),
                new IntArrayList(),
                false,
                false
        );
        Fireworks fireworks = new Fireworks((byte) 1, List.of(explosion));
        fireworkStack.set(DataComponents.FIREWORKS, fireworks);
        FireworkRocketEntity fireworkEntity = new FireworkRocketEntity(level, x, y, z, fireworkStack);
        level.addFreshEntity(fireworkEntity);
    }


    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(), 3);
            }

            super.onContentsChanged(slot);
        }
    };

    public void setGolfPar(int golfPar) {
        this.GolfPar = golfPar;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public int getGolfPar() {
        return this.GolfPar;
    }



    private float rotation;

    public float getRenderingRotation() {
        rotation += 0.18f;
        if(rotation>=360){
            rotation = 0;
        }
        return rotation;
    }


    public GolfCupBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GOLF_CLUP_BE.get(), pos, blockState);
    }

    public void clearContents(){
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("GolfPar", this.GolfPar);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("GolfPar")) {
            this.GolfPar = tag.getInt("GolfPar");
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

}
