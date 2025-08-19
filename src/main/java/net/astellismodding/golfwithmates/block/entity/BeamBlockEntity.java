package net.astellismodding.golfwithmates.block.entity;

import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeamBlockEntity extends BlockEntity {
    private boolean isActive = false;
    private List<Vec3> targetPositions = new ArrayList<>();
    private transient Vec3[] cachedPositionsArray;
    private transient boolean isCacheDirty = true;

    public BeamBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.Beam_Block_BE.get(), pPos, pBlockState);
    }

    //
    //SETTERS
    //
    public void setActive(boolean active) {
        this.isActive = active;
        setChangedAndUpdate();
    }

    /**
     * Adds a position to the list of positions for the renderer, absolute world positions.
     * this will mark the cache dirty as renderer will need to recache-
     * @param positionToAdd Position to add to the end of master list.
     */
    public void addTargetPosition(Vec3 positionToAdd) {
        this.targetPositions.add(positionToAdd);
        this.isCacheDirty = true; // Mark the cache as dirty
        setChangedAndUpdate();
    }

    /**
     * Sets the list of target positions - absolute world positions.
     * this will mark cache dirty and renderer will need to recache-
     * @param positions Positions to add
     */
    public void setTargetPositions(Vec3[] positions) {
        this.targetPositions = new ArrayList<>(Arrays.asList(positions));
        this.isCacheDirty = true; // Mark the cache as dirty
        setChangedAndUpdate();
    }

    //
    //GETTERS
    //
    public boolean isActive() {
        return this.isActive;
    }
    public Vec3[] getPositionsForRendering() {
        if (this.isCacheDirty || this.cachedPositionsArray == null) {
            // If the data has changed, regenerate the cache
            this.cachedPositionsArray = this.targetPositions.toArray(new Vec3[0]);
            this.isCacheDirty = false; // Mark the cache as clean
        }
        return this.cachedPositionsArray;
    }


    //
    //DATA SAVING
    //
    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        // ... saving other fields

        ListTag listTag = new ListTag();
        for (Vec3 pos : this.targetPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putDouble("x", pos.x);
            posTag.putDouble("y", pos.y);
            posTag.putDouble("z", pos.z);
            listTag.add(posTag);
        }
        pTag.put("TargetPositions", listTag);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        // ... loading other fields

        this.targetPositions = new ArrayList<>();
        if (pTag.contains("TargetPositions", Tag.TAG_LIST)) {
            ListTag listTag = pTag.getList("TargetPositions", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag posTag = listTag.getCompound(i);
                this.targetPositions.add(new Vec3(
                        posTag.getDouble("x"),
                        posTag.getDouble("y"),
                        posTag.getDouble("z")
                ));
            }
        }
        // IMPORTANT: Mark cache as dirty after loading data
        this.isCacheDirty = true;
    }

    //
    //CLIENT SYNC
    //
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        // We can reuse our saveAdditional method to write the data
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}

