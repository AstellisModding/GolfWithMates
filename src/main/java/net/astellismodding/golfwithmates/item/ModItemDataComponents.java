package net.astellismodding.golfwithmates.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.component.PutterPower;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItemDataComponents {
    // Create a registrar specifically for Data Components
    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, GolfWithMates.MOD_ID);

    // Codec for saving the PutterPower to disk (persistent)
    public static final Codec<PutterPower> PUTTER_POWER_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(PutterPower::level)
            ).apply(instance, PutterPower::new)
    );

    // StreamCodec for sending the PutterPower over the network
    public static final StreamCodec<ByteBuf, PutterPower> PUTTER_POWER_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PutterPower::level,
            PutterPower::new
    );

    // Register the DataComponentType
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PutterPower>> PUTTER_POWER =
            REGISTRAR.registerComponentType(
                    "putter_power",
                    builder -> builder
                            .persistent(PUTTER_POWER_CODEC) // This makes it save to disk
                            .networkSynchronized(PUTTER_POWER_STREAM_CODEC) // This makes it sync to clients
            );
    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
