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

public class BeamBlockEntity extends BlockEntity {

    public BeamBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.Beam_Block_BE.get(), pPos, pBlockState);
    }
}
