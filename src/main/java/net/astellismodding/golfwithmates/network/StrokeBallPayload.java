package net.astellismodding.golfwithmates.network;

import io.netty.buffer.ByteBuf;
import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StrokeBallPayload(int strokes) implements CustomPacketPayload {

    public static final Type<StrokeBallPayload> TYPE = new Type<>(GolfWithMates.getRL("stroke_ball"));

    public static final StreamCodec<ByteBuf, StrokeBallPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    StrokeBallPayload::strokes,
                    StrokeBallPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(StrokeBallPayload payload, IPayloadContext context) {
        // Reserved for future use
    }
}
