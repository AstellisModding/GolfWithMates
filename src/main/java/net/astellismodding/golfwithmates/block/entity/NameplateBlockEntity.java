package net.astellismodding.golfwithmates.block.entity;

import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class NameplateBlockEntity extends BlockEntity {

    private Component customName = Component.literal("Default Name");

    public NameplateBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.Nameplate_Block_BE.get(), pPos, pBlockState);
    }

    // --- NBT SAVING & LOADING ---
    // Saves the custom name to the world save.
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("CustomName", Component.Serializer.toJson(this.customName,registries));
    }

    // Loads the custom name from the world save.
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName")) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"),registries);
        }
    }

    // --- DATA SYNCING ---
    // This is crucial for sending data from the server to the client.
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
}
