package net.astellismodding.golfwithmates.network;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetCupDataPayload(BlockPos pos, String courseName, int par,
                                String disguiseBlock) implements CustomPacketPayload {

    public static final Type<SetCupDataPayload> TYPE = new Type<>(GolfWithMates.getRL("set_cup_data"));

    public static final StreamCodec<FriendlyByteBuf, SetCupDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetCupDataPayload::pos,
                    ByteBufCodecs.STRING_UTF8,
                    SetCupDataPayload::courseName,
                    ByteBufCodecs.INT,
                    SetCupDataPayload::par,
                    ByteBufCodecs.STRING_UTF8,
                    SetCupDataPayload::disguiseBlock,
                    SetCupDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(SetCupDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(payload.pos()) instanceof GolfCupBlockEntity be) {
                be.setCourseName(payload.courseName());
                be.setGolfPar(Math.max(1, Math.min(9, payload.par())));
                be.setDisguiseBlock(payload.disguiseBlock());
            }
        });
    }
}
