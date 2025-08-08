package net.astellismodding.golfwithmates.network;

import io.netty.buffer.ByteBuf;
import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.component.PutterPower;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.astellismodding.golfwithmates.util.ClubUtils.isClub;

public record HoledOutPayload(int strokes) implements CustomPacketPayload {
    public static final Type<HoledOutPayload> TYPE = new Type<>(GolfWithMates.getRL("holed_out_payload"));

    public static final StreamCodec<ByteBuf, HoledOutPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    HoledOutPayload::strokes,
                    HoledOutPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(HoledOutPayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();

        //Add ball to the cup
    }

}
