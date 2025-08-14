package net.astellismodding.golfwithmates.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.astellismodding.golfwithmates.block.entity.BeamBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.awt.Color;

public class BeamBlockEntityRenderer implements BlockEntityRenderer<BeamBlockEntity> {


    public static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    public BeamBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BeamBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // We'll render a simple beam of a fixed height and color to start.
        int height = 2;
        int color = Color.WHITE.getRGB();

        long gameTime = blockEntity.getLevel().getGameTime();
        renderBeaconBeam(poseStack, bufferSource, partialTick, gameTime, 0, height, color);
    }

    /**
     * Renders a beacon beam segment. This is a simplified version of the vanilla beacon renderer.
     * @param yOffset The starting Y level of the beam segment, relative to the block.
     * @param height The height of this beam segment.
     * @param color The ARGB color of the beam.
     */
    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime, int yOffset, int height, int color) {
        int endY = yOffset + height;
        float beamRadius = 0.2F;
        float glowRadius = 0.25F;

        poseStack.pushPose();
        // Center the beam in the block.
        poseStack.translate(0.5, 0.0, 0.5);

        // This handles the scrolling texture animation.
        float textureOffset = (float)Math.floorMod(gameTime, 40L) + partialTick;
        float vScroll = Mth.frac(-textureOffset * 0.2F - (float)Mth.floor(-textureOffset * 0.1F));

        // Get the vertex consumer for the main beam texture.
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, false));

        // Render the four sides of the inner beam.
        renderBeamFace(poseStack, consumer, color, yOffset, endY, -beamRadius, -beamRadius, beamRadius, -beamRadius, vScroll);
        renderBeamFace(poseStack, consumer, color, yOffset, endY, beamRadius, -beamRadius, beamRadius, beamRadius, vScroll);
        renderBeamFace(poseStack, consumer, color, yOffset, endY, beamRadius, beamRadius, -beamRadius, beamRadius, vScroll);
        renderBeamFace(poseStack, consumer, color, yOffset, endY, -beamRadius, beamRadius, -beamRadius, -beamRadius, vScroll);

        // Render the outer glow effect.
        VertexConsumer glowConsumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, true));
        int glowColor = (32 << 24) | (color & 0x00FFFFFF); // Set alpha to 32 for transparency.

        renderBeamFace(poseStack, glowConsumer, glowColor, yOffset, endY, -glowRadius, -glowRadius, glowRadius, -glowRadius, vScroll);
        renderBeamFace(poseStack, glowConsumer, glowColor, yOffset, endY, glowRadius, -glowRadius, glowRadius, glowRadius, vScroll);
        renderBeamFace(poseStack, glowConsumer, glowColor, yOffset, endY, glowRadius, glowRadius, -glowRadius, glowRadius, vScroll);
        renderBeamFace(poseStack, glowConsumer, glowColor, yOffset, endY, -glowRadius, glowRadius, -glowRadius, -glowRadius, vScroll);

        poseStack.popPose();
    }

    /**
     * Renders a single rectangular face of the beam.
     */
    private static void renderBeamFace(PoseStack poseStack, VertexConsumer consumer, int color, int minY, int maxY, float minX, float minZ, float maxX, float maxZ, float vScroll) {
        float textureScale = 1.0f; // How many times the texture repeats vertically.
        float minV = (float)maxY * textureScale + vScroll;
        float maxV = (float)minY * textureScale + vScroll;

        PoseStack.Pose pose = poseStack.last();
        addVertex(pose, consumer, color, maxY, minX, minZ, 1.0f, minV);
        addVertex(pose, consumer, color, minY, minX, minZ, 1.0f, maxV);
        addVertex(pose, consumer, color, minY, maxX, maxZ, 0.0f, maxV);
        addVertex(pose, consumer, color, maxY, maxX, maxZ, 0.0f, minV);
    }

    /**
     * Helper method to add a single vertex with all its data.
     */
    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, int y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, (float)y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // Full brightness
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
