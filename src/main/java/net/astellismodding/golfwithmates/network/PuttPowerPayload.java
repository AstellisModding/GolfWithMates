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

import static net.astellismodding.golfwithmates.util.ClubUtils.*;

public record PuttPowerPayload(int direction) implements CustomPacketPayload {
    public static final Type<PuttPowerPayload> TYPE = new Type<>(GolfWithMates.getRL("putt_power"));

    public static final StreamCodec<ByteBuf, PuttPowerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PuttPowerPayload::direction,
                    PuttPowerPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(PuttPowerPayload payload, IPayloadContext context) {
        int xvar = 0;
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        if (isClub(new ItemStack(player.getMainHandItem().getItem()))) {
            if (payload.direction == 1) {

                if (stack.get(ModDataComponent.put_power) != null) {
                    PutterPower test = stack.get(ModDataComponent.put_power).cycle(1);
                    stack.set(ModDataComponent.put_power, test);
                    xvar = test.value();
                }
                player.displayClientMessage(Component.literal("Club Power :" + xvar), true);


            } else if (payload.direction == 0) {
                if (stack.get(ModDataComponent.put_power) != null) {
                    PutterPower test = stack.get(ModDataComponent.put_power).cycle(0);
                    stack.set(ModDataComponent.put_power, test);
                    xvar = test.value();
                }
                player.displayClientMessage(Component.literal("Club Power :" + xvar), true);

            }
        }
    }

}
