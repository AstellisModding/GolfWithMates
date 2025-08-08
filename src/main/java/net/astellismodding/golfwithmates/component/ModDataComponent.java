package net.astellismodding.golfwithmates.component;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.astellismodding.golfwithmates.component.PutterPower.BASIC_CODEC;

public class ModDataComponent {
    //public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(SonicResonant.MOD_ID);
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, GolfWithMates.MOD_ID);


    public static final DeferredHolder<DataComponentType<?>,DataComponentType<PutterPower>> put_power = REGISTRAR.registerComponentType(
            "put_power",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(BASIC_CODEC)
    );

    public static void registerHandler(IEventBus eventBus){
        REGISTRAR.register(eventBus);
    }
}
