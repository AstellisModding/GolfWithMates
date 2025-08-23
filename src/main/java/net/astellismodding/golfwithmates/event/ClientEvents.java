package net.astellismodding.golfwithmates.event;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.block.entity.renderer.BeamBlockEntityRenderer;
import net.astellismodding.golfwithmates.block.entity.renderer.GolfBallBlockEntityRender;
import net.astellismodding.golfwithmates.block.entity.renderer.GolfCupBlockEntityRenderer;
import net.astellismodding.golfwithmates.block.entity.renderer.NameplateBlockEntityRenderer;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.network.PuttPowerPayload;
import net.astellismodding.golfwithmates.network.StrokePowerPayload;
import net.astellismodding.golfwithmates.render.CScrollIncrementer;
import net.astellismodding.golfwithmates.util.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;

@Mod(value = GolfWithMates.MOD_ID, dist = Dist.CLIENT)
public class ClientEvents {
    @EventBusSubscriber(modid = GolfWithMates.MOD_ID, value = Dist.CLIENT)
    public static class ClientNeoForgeEvents {

        private static final Minecraft minecraft = Minecraft.getInstance();
        private static final CScrollIncrementer scrollIncrementer = new CScrollIncrementer(true);

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

        @SubscribeEvent
        public static void onMouseEvent(MouseScrollingEvent event) {
            if (minecraft.player != null && minecraft.player.isShiftKeyDown()) {
                double delta = event.getScrollDeltaY();
                if (delta != 0) {
                    int shift = scrollIncrementer.scroll(delta);
                    if (shift != 0) {
                        PacketDistributor.sendToServer(new StrokePowerPayload(shift));
                    }
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(
                    ModBlockEntities.GOLF_CLUP_BE.get(),
                    GolfCupBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                    ModBlockEntities.Nameplate_Block_BE.get(),
                    NameplateBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                    ModBlockEntities.Beam_Block_BE.get(),
                    BeamBlockEntityRenderer::new
            );
            event.registerBlockEntityRenderer(
                    ModBlockEntities.GOLF_BALL_BE.get(),
                    GolfBallBlockEntityRender::new
            );
        }
            }



}
