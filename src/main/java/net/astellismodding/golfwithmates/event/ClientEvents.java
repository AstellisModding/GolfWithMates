package net.astellismodding.golfwithmates.event;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.network.PuttPowerPayload;
import net.astellismodding.golfwithmates.sound.ModSounds;
import net.astellismodding.golfwithmates.util.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = GolfWithMates.MOD_ID, dist = Dist.CLIENT)
public class ClientEvents {
    @EventBusSubscriber(modid = GolfWithMates.MOD_ID, value = Dist.CLIENT)
    public static class ClientNeoForgeEvents {

        private static final Component TESTER_COMP =
                Component.translatable("message."+ GolfWithMates.MOD_ID+".testkeyaffirmation");

        //REGISTERING KEYS AND SUCH
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event){
            event.register(ModKeyMappings.Golf_PowerDOWN.get());
            event.register(ModKeyMappings.Golf_PowerUP.get());
        }

        @SubscribeEvent
        public static void onKeyInput(ClientTickEvent.Post event){
            Minecraft minecraft = Minecraft.getInstance();
            while (ModKeyMappings.Golf_PowerUP.get().consumeClick() && minecraft.player != null){
                //minecraft.player.displayClientMessage(TESTER_COMP, true);
                PacketDistributor.sendToServer(new PuttPowerPayload(1));
                //minecraft.player.playSound(ModSounds.GolfPowUp.value(),1f,1f);
            }

            while (ModKeyMappings.Golf_PowerDOWN.get().consumeClick() && minecraft.player != null){
                //minecraft.player.displayClientMessage(TESTER_COMP, true);
                PacketDistributor.sendToServer(new PuttPowerPayload(0));
                //minecraft.player.playSound(ModSounds.GolfPowDown.value(),1f,1f);

            }
        }
    }

}
