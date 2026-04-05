package net.astellismodding.golfwithmates.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.astellismodding.golfwithmates.entity.custom.GolfBallEntity;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GolfBallEntityRenderer extends EntityRenderer<GolfBallEntity> {

    public GolfBallEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1f;
    }

    @Override
    public void render(GolfBallEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // The golf ball model geometry is centered at (8, 1, 8) in 0-16 space = (0.5, 0.0625, 0.5)
        // in block coords. Scale 2x to get a visible 0.25-block ball, then shift XZ so the
        // model center lands on the entity position rather than 0.5 blocks offset.
        poseStack.translate(0, 0.5, 0);
        poseStack.scale(1.0f, 1.0f, 1.0f);
        ItemStack ballStack = new ItemStack(ModBlocks.GOLF_BALL.get().asItem());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ballStack, ItemDisplayContext.NONE,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource,
                entity.level(), entity.getId()
        );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GolfBallEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}