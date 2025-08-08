package net.astellismodding.golfwithmates.network;

import io.netty.buffer.ByteBuf;
import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StrokeBallPayload(int strokes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HoledOutPayload> TYPE = new CustomPacketPayload.Type<>(GolfWithMates.getRL("holed_out_payload"));

    public static final StreamCodec<ByteBuf, HoledOutPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    HoledOutPayload::strokes,
                    HoledOutPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(HoledOutPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();

        //Add ball to the cup
    }
}

