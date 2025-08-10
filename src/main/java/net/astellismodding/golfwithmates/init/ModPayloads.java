package net.astellismodding.golfwithmates.init;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.network.PuttPowerPayload;
import net.astellismodding.golfwithmates.network.StrokePowerPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {
    private ModPayloads(){

    }
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar(GolfWithMates.MOD_ID);
        registrar.playToServer(PuttPowerPayload.TYPE, PuttPowerPayload.STREAM_CODEC, PuttPowerPayload::handlePayload);
        registrar.playToServer(StrokePowerPayload.TYPE, StrokePowerPayload.STREAM_CODEC, StrokePowerPayload::handlePayload);
    }
}
