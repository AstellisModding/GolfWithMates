package net.astellismodding.golfwithmates.sound;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENT =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, GolfWithMates.MOD_ID);

    public static final Holder<SoundEvent> GolfPutt = SOUND_EVENT.register("golf_putt",SoundEvent::createVariableRangeEvent);
    public static final Holder<SoundEvent> GolfScore = SOUND_EVENT.register("golf_score",SoundEvent::createVariableRangeEvent);
    public static final Holder<SoundEvent> GolfPowUp = SOUND_EVENT.register("golf_power_up",SoundEvent::createVariableRangeEvent);
    public static final Holder<SoundEvent> GolfPowDown = SOUND_EVENT.register("golf_power_down",SoundEvent::createVariableRangeEvent);


    public static void register(IEventBus eventBus) {
        SOUND_EVENT.register(eventBus);
    }

}
