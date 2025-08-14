package net.astellismodding.golfwithmates.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class GolfBallBlockEntityRender implements BlockEntityRenderer<GolfBallBlockEntity> {
    private final Font font;
    private final BlockRenderDispatcher blockRenderer;

    public GolfBallBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(GolfBallBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockState = pBlockEntity.getBlockState();
        pPoseStack.pushPose();
        //this.blockRenderer.renderSingleBlock(blockState, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);


        // Pop the pose so the block's transformations don't apply to the text
        pPoseStack.popPose();

        MutableComponent textToDisplay = pBlockEntity.getCustomName().copy()
                .append(Component.literal(": " + pBlockEntity.getPuttCounter()));

        if (textToDisplay == null || textToDisplay.getString().isEmpty()) {
            return; // Don't render if there's no name
        }

        pPoseStack.pushPose(); // Start a new transformation state for the text
        pPoseStack.translate(0.5, 1.25, 0.5);
        pPoseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        float scale = 0.025f;
        pPoseStack.scale(scale, -scale, scale);
        Matrix4f matrix4f = pPoseStack.last().pose();
        float textWidth = this.font.width(textToDisplay);

        this.font.drawInBatch(textToDisplay, -textWidth / 2, 0, 0x000000, false, matrix4f, pBufferSource, Font.DisplayMode.SEE_THROUGH, 0x40000000, pPackedLight);
        this.font.drawInBatch(textToDisplay, -textWidth / 2, 0, 0xFFFFFF, false, matrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);

        pPoseStack.popPose(); // Restore the previous transformation state
    }

    @Override
    public boolean shouldRenderOffScreen(GolfBallBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        // The maximum distance (squared) from which the renderer is active. Default is 64.
        return 2;
    }
}