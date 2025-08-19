package net.astellismodding.golfwithmates.block.entity;

import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GolfBallBlockEntity extends BlockEntity {
    private Component customName = Component.literal("Default Name");
    private int puttCounter = 0;
    private boolean isActive = false;
    private List<Vec3> targetPositions = new ArrayList<>();
    private transient Vec3[] cachedPositionsArray;
    private transient boolean isCacheDirty = true;

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
    public Vec3[] getPositionsForRendering() {
        if (this.isCacheDirty || this.cachedPositionsArray == null) {
            // If the data has changed, regenerate the cache
            this.cachedPositionsArray = this.targetPositions.toArray(new Vec3[0]);
            this.isCacheDirty = false; // Mark the cache as clean
        }
        return this.cachedPositionsArray;
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

        if (!targetPositions.isEmpty()) {
            CompoundTag positionsTag = new CompoundTag();
            positionsTag.putInt("Size", targetPositions.size());
            for (int i = 0; i < targetPositions.size(); i++) {
                Vec3 pos = targetPositions.get(i);
                CompoundTag posTag = new CompoundTag();
                posTag.putDouble("x", pos.x);
                posTag.putDouble("y", pos.y);
                posTag.putDouble("z", pos.z);
                positionsTag.put("Pos" + i, posTag);
            }
            tag.put("TargetPositions", positionsTag);
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

        if (tag.contains("TargetPositions")) {
            this.targetPositions.clear();
            CompoundTag positionsTag = tag.getCompound("TargetPositions");
            int size = positionsTag.getInt("Size");
            for (int i = 0; i < size; i++) {
                CompoundTag posTag = positionsTag.getCompound("Pos" + i);
                Vec3 pos = new Vec3(
                        posTag.getDouble("x"),
                        posTag.getDouble("y"),
                        posTag.getDouble("z")
                );
                this.targetPositions.add(pos);
            }
            this.isCacheDirty = true; // Mark cache as dirty after loading new positions
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