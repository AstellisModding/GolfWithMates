package net.astellismodding.golfwithmates.block.entity;

import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.util.ShotResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class GolfBallBlockEntity extends BlockEntity {
    private Component customName = Component.literal("Default Name");
    private int puttCounter = 0;
    private boolean isActive = false;
    private ShotResult currentShot = ShotResult.empty();
    private int animationTick = 0;        // pinned — leave unused for now
    private boolean animationDone = false; // pinned — leave unused for now

    public GolfBallBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.GOLF_BALL_BE.get(), pPos, pBlockState);
    }

    //
    //SETTERS
    //
    public void setActive(boolean active) {
        this.isActive = active;
        setChangedAndUpdate();
    }

    public void setShotResult(ShotResult result) {
        this.currentShot = result;
        this.animationTick = 0;
        this.animationDone = false;
        setChangedAndUpdate();
    }
    public void setCustomName(Component name) {
        this.customName = name;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setPuttCounter(int count) {
        this.puttCounter = count;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void IncrementPuttCounter() {
        this.puttCounter++;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    //
    //GETTERS
    //
    public boolean isActive() {
        return this.isActive;
    }
    public ShotResult getShotResult() {
        return this.currentShot;
    }
    public Component getCustomName() {
        return this.customName;
    }


    public int getPuttCounter() {
        return this.puttCounter;
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("CustomName", Component.Serializer.toJson(this.customName, registries));
        tag.putInt("PuttCounter", this.puttCounter);
        tag.putBoolean("isActive", isActive);

        if (!currentShot.path.isEmpty()) {
            tag.put("ShotResult", currentShot.toNbt());
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName")) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }
        if (tag.contains("PuttCounter")) {
            this.puttCounter = tag.getInt("PuttCounter");
        }
        this.isActive = tag.getBoolean("isActive");

        if (tag.contains("ShotResult")) {
            this.currentShot = ShotResult.fromNbt(tag.getCompound("ShotResult"));
        } else {
            this.currentShot = ShotResult.empty();
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

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }


}