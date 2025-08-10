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
import org.jetbrains.annotations.NotNull;

import static net.astellismodding.golfwithmates.util.ClubUtils.isClub;

public record StrokePowerPayload(int shift) implements CustomPacketPayload {
    public static final Type<StrokePowerPayload> TYPE = new Type<>(GolfWithMates.getRL("stroke_power"));

    public static final StreamCodec<ByteBuf, StrokePowerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    StrokePowerPayload::shift,
                    StrokePowerPayload::new
            );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handlePayload(StrokePowerPayload payload, IPayloadContext context) {
        double xvar = 0;
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        if (isClub(new ItemStack(player.getMainHandItem().getItem()))) {
            if (stack.get(ModDataComponent.put_power) != null) {
                PutterPower test = stack.get(ModDataComponent.put_power).cycle(payload.shift);
                stack.set(ModDataComponent.put_power, test);
                xvar = test.value();
            }
            player.displayClientMessage(Component.literal("%" + Math.round(xvar*100) + " Stroke Power"), true);

        }
    }
}


