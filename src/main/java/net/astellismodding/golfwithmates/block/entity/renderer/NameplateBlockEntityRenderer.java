package net.astellismodding.golfwithmates.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.astellismodding.golfwithmates.block.entity.NameplateBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class NameplateBlockEntityRenderer  implements BlockEntityRenderer<NameplateBlockEntity> {
    private final Font font;

    public NameplateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Get the font renderer from the context
        this.font = context.getFont();
    }

    @Override
    public void render(NameplateBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // Get the custom name from the block entity
        Component textToDisplay = pBlockEntity.getCustomName();
        if (textToDisplay == null) {
            return; // Don't render if there's no name
        }

        // --- Positioning and Scaling ---
        pPoseStack.pushPose(); // Start a new transformation state

        // 1. Center the text above the block. (0.5, 0.5, 0.5) is the block's center.
        // We move it up to 1.25 on the Y-axis to float above the block.
        pPoseStack.translate(0.5, 1.25, 0.5);

        // 2. Make the text face the player
        pPoseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // 3. Scale the text down to a reasonable size.
        // The negative Y-scale is to flip the text right-side up, as it's rendered upside down by default.
        float scale = 0.025f;
        pPoseStack.scale(scale, -scale, scale);

        // --- Rendering ---
        Matrix4f matrix4f = pPoseStack.last().pose();
        float textWidth = this.font.width(textToDisplay);

        // Render a semi-transparent background for readability
        this.font.drawInBatch(textToDisplay, -textWidth / 2, 0, 0x000000, false, matrix4f, pBufferSource, Font.DisplayMode.SEE_THROUGH, 0x40000000, pPackedLight);

        // Render the actual text
        this.font.drawInBatch(textToDisplay, -textWidth / 2, 0, 0xFFFFFF, false, matrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);

        pPoseStack.popPose(); // Restore the previous transformation state
    }

    @Override
    public boolean shouldRenderOffScreen(NameplateBlockEntity pBlockEntity) {
        // This makes the nameplate render from further away.
        return true;
    }

    @Override
    public int getViewDistance() {
        // The maximum distance (squared) from which the renderer is active. Default is 64.
        return 256;
    }
}
