package net.astellismodding.golfwithmates.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GolfCupBlockEntityRenderer implements BlockEntityRenderer<GolfCupBlockEntity> {

    // Hole bounds — match cup model element positions (x=4..12, z=4..12 out of 16)
    private static final float HOLE_X0 = 4f  / 16f;
    private static final float HOLE_X1 = 12f / 16f;
    private static final float HOLE_Z0 = 4f  / 16f;
    private static final float HOLE_Z1 = 12f / 16f;

    // Element 6 interior box bounds (the dark cup floor in the model)
    private static final float INT_X0 = 4f  / 16f;
    private static final float INT_X1 = 12f / 16f;
    private static final float INT_Y1 = 2f  / 16f;   // floor height
    private static final float INT_Z0 = 4f  / 16f;
    private static final float INT_Z1 = 12f / 16f;

    public GolfCupBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GolfCupBlockEntity pBlockentity, float vPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        String disguiseName = pBlockentity.getDisguiseBlock();
        int light = getLightLevel(pBlockentity.getLevel(), pBlockentity.getBlockPos());
        BlockState cupState = pBlockentity.getBlockState();

        ItemStack ballStack = pBlockentity.inventory.getStackInSlot(0);
        if (!ballStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 1.15f, 0.5f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockentity.getRenderingRotation()));
            itemRenderer.renderStatic(ballStack, ItemDisplayContext.FIXED,
                    light, OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource,
                    pBlockentity.getLevel(), 1);
            pPoseStack.popPose();
        }

        if (disguiseName.isEmpty()) {
            Minecraft.getInstance().getBlockRenderer()
                    .renderSingleBlock(cupState, pPoseStack, pBufferSource, light, OverlayTexture.NO_OVERLAY);

            return;
        }

        // ── Disguised ─────────────────────────────────────────────────────────
        ResourceLocation rl = ResourceLocation.tryParse(disguiseName);
        if (rl == null) {
            Minecraft.getInstance().getBlockRenderer()
                    .renderSingleBlock(cupState, pPoseStack, pBufferSource, light, OverlayTexture.NO_OVERLAY);
            return;
        }

        Block disguiseBlock = BuiltInRegistries.BLOCK.get(rl);
        BlockState disguiseState = disguiseBlock.defaultBlockState();
        BakedModel disguiseModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(disguiseState);
        RandomSource random = RandomSource.create(42L);

        VertexConsumer consumer = pBufferSource.getBuffer(RenderType.solid());
        PoseStack.Pose pose = pPoseStack.last();

        // Step 1: disguise sides + bottom
        for (Direction dir : new Direction[]{
                Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (BakedQuad quad : disguiseModel.getQuads(disguiseState, dir, random)) {
                consumer.putBulkData(pose, quad, 1f, 1f, 1f, 1f, light, OverlayTexture.NO_OVERLAY);
            }
        }

        // Step 2: disguise top as a frame — hole centre left empty
        List<BakedQuad> upQuads = disguiseModel.getQuads(disguiseState, Direction.UP, random);
        TextureAtlasSprite topSprite = upQuads.isEmpty()
                ? disguiseModel.getParticleIcon()
                : upQuads.get(0).getSprite();

        // left strip, front strip, right strip, back strip
        emitTopQuad(consumer, pose, topSprite, 0f,      0f,      HOLE_X0, HOLE_Z1, 1.001f, light);
        emitTopQuad(consumer, pose, topSprite, HOLE_X0, 0f,      1f,      HOLE_Z0, 1.001f, light);
        emitTopQuad(consumer, pose, topSprite, HOLE_X1, HOLE_Z0, 1f,      HOLE_Z1, 1.001f, light);
        emitTopQuad(consumer, pose, topSprite, 0f,      HOLE_Z1, 1f,      1f,      1.001f, light);

        // Step 3: plain grey interior (element 6 — floor + 4 inner walls)
        renderGreyInterior(consumer, pose, light);
    }

    /**
     * Emits a single upward-facing quad at the given y.
     * Winding: NE → NW → SW → SE  (matches Minecraft FaceBakery UP convention).
     * UV sampled in 0..1 space so the sprite maps continuously across the full block top.
     */
    private void emitTopQuad(VertexConsumer consumer, PoseStack.Pose pose,
                              TextureAtlasSprite sprite,
                              float x0, float z0, float x1, float z1,
                              float y, int light) {
        float u0 = sprite.getU(x0), u1 = sprite.getU(x1);
        float v0 = sprite.getV(z0), v1 = sprite.getV(z1);
        v(consumer, pose, x1, y, z0, u1, v0, 0, 1, 0, light);
        v(consumer, pose, x0, y, z0, u0, v0, 0, 1, 0, light);
        v(consumer, pose, x0, y, z1, u0, v1, 0, 1, 0, light);
        v(consumer, pose, x1, y, z1, u1, v1, 0, 1, 0, light);
    }

    /**
     * Renders the cup interior (element 6: the dark floor visible through the hole)
     * as plain grey using white_concrete texture tinted grey.
     */
    private void renderGreyInterior(VertexConsumer consumer, PoseStack.Pose pose, int light) {
        TextureAtlasSprite s = Minecraft.getInstance()
                .getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(ResourceLocation.withDefaultNamespace("block/white_concrete"));

        float x0 = INT_X0, x1 = INT_X1;
        float y0 = 0f,     y1 = 1.0f;   // full block height — covers all inner wall faces
        float yf = INT_Y1;               // floor height
        float z0 = INT_Z0, z1 = INT_Z1;
        float gTop   = 0.55f; // rim — lighter (near opening)
        float gBot   = 0.12f; // deep — darker
        float gFloor = 0.18f; // floor

        // Floor (UP face) — visible from above through the hole
        // NE → NW → SW → SE
        vg(consumer, pose, s, x1, yf, z0, 1f, 0f, 0, 1, 0, gFloor, light);
        vg(consumer, pose, s, x0, yf, z0, 0f, 0f, 0, 1, 0, gFloor, light);
        vg(consumer, pose, s, x0, yf, z1, 0f, 1f, 0, 1, 0, gFloor, light);
        vg(consumer, pose, s, x1, yf, z1, 1f, 1f, 0, 1, 0, gFloor, light);

        // 4 inner walls — gradient: light at rim (y1), dark at depth (y0)
        // North inner wall — faces +Z (south-facing), at z=z0
        vg(consumer, pose, s, x1, y1, z0, 1f, 0f,  0, 0, 1, gTop, light);
        vg(consumer, pose, s, x0, y1, z0, 0f, 0f,  0, 0, 1, gTop, light);
        vg(consumer, pose, s, x0, y0, z0, 0f, 1f,  0, 0, 1, gBot, light);
        vg(consumer, pose, s, x1, y0, z0, 1f, 1f,  0, 0, 1, gBot, light);

        // South inner wall — faces -Z (north-facing), at z=z1
        vg(consumer, pose, s, x0, y1, z1, 1f, 0f,  0, 0, -1, gTop, light);
        vg(consumer, pose, s, x1, y1, z1, 0f, 0f,  0, 0, -1, gTop, light);
        vg(consumer, pose, s, x1, y0, z1, 0f, 1f,  0, 0, -1, gBot, light);
        vg(consumer, pose, s, x0, y0, z1, 1f, 1f,  0, 0, -1, gBot, light);

        // West inner wall — faces +X (east-facing), at x=x0
        vg(consumer, pose, s, x0, y1, z0, 0f, 0f,  1, 0, 0, gTop, light);
        vg(consumer, pose, s, x0, y1, z1, 1f, 0f,  1, 0, 0, gTop, light);
        vg(consumer, pose, s, x0, y0, z1, 1f, 1f,  1, 0, 0, gBot, light);
        vg(consumer, pose, s, x0, y0, z0, 0f, 1f,  1, 0, 0, gBot, light);

        // East inner wall — faces -X (west-facing), at x=x1
        vg(consumer, pose, s, x1, y1, z1, 0f, 0f, -1, 0, 0, gTop, light);
        vg(consumer, pose, s, x1, y1, z0, 1f, 0f, -1, 0, 0, gTop, light);
        vg(consumer, pose, s, x1, y0, z0, 1f, 1f, -1, 0, 0, gBot, light);
        vg(consumer, pose, s, x1, y0, z1, 0f, 1f, -1, 0, 0, gBot, light);
    }

    /** Vertex helper — full white (used for disguise top frame). */
    private void v(VertexConsumer c, PoseStack.Pose pose,
                   float x, float y, float z, float u, float v,
                   float nx, float ny, float nz, int light) {
        c.addVertex(pose, x, y, z).setColor(1f, 1f, 1f, 1f)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
    }

    /** Vertex helper — grey tinted (used for cup interior). */
    private void vg(VertexConsumer c, PoseStack.Pose pose,
                    TextureAtlasSprite sprite,
                    float x, float y, float z, float su, float sv,
                    float nx, float ny, float nz, float grey, int light) {
        c.addVertex(pose, x, y, z).setColor(grey, grey, grey, 1f)
                .setUv(sprite.getU(su), sprite.getV(sv))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
    }

    private int getLightLevel(Level level, BlockPos blockPos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, blockPos);
        int sLight = level.getBrightness(LightLayer.SKY, blockPos);
        return LightTexture.pack(bLight, sLight);
    }
}
