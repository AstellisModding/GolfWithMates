package net.astellismodding.golfwithmates.block.entity;

import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class GolfBallBlockEntity extends BlockEntity {
    private Component customName = Component.literal("Default Name");
    private int puttCounter = 0;

    public GolfBallBlockEntity(BlockPos pPos, BlockState pBlockState) {
         super(ModBlockEntities.GOLF_BALL_BE.get(), pPos, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("CustomName", Component.Serializer.toJson(this.customName,registries));
        tag.putInt("PuttCounter", this.puttCounter);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName")) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"),registries);
        }
        if (tag.contains("PuttCounter")) {
            this.puttCounter = tag.getInt("PuttCounter");
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

    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public int getPuttCounter() {
        return this.puttCounter;
    }

    public void setPuttCounter(int count) {
        this.puttCounter = count;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void IncrementPuttCounter() {
        this.puttCounter++;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

}
