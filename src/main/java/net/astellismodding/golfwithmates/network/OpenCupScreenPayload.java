package net.astellismodding.golfwithmates.network;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.screen.GolfCupScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCupScreenPayload(BlockPos pos, String courseName, int par,
                                   String disguiseBlock) implements CustomPacketPayload {

    public static final Type<OpenCupScreenPayload> TYPE = new Type<>(GolfWithMates.getRL("open_cup_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenCupScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    OpenCupScreenPayload::pos,
                    ByteBufCodecs.STRING_UTF8,
                    OpenCupScreenPayload::courseName,
                    ByteBufCodecs.INT,
                    OpenCupScreenPayload::par,
                    ByteBufCodecs.STRING_UTF8,
                    OpenCupScreenPayload::disguiseBlock,
                    OpenCupScreenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(OpenCupScreenPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.enqueueWork(() -> GolfCupScreen.open(
                    payload.pos(), payload.courseName(), payload.par(), payload.disguiseBlock()));
        }
    }
}
