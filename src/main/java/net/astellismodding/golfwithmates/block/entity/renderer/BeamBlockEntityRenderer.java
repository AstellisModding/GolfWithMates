package net.astellismodding.golfwithmates.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.astellismodding.golfwithmates.block.entity.GolfBallBlockEntity;
import net.astellismodding.golfwithmates.block.entity.NameplateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import com.mojang.math.Axis;
import net.astellismodding.golfwithmates.block.entity.BeamBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

public class BeamBlockEntityRenderer implements BlockEntityRenderer<BeamBlockEntity> {

    public BeamBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }
    // You'll need to define the location of your beam texture.
    // You can use the vanilla one or create your own.
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    @Override
    public void render(BeamBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        long gameTime = blockEntity.getLevel().getGameTime();
        int color = Color.RED.getRGB();
        float beamRadius = 0.05f;
        Vec3[] targetPositions = blockEntity.getPositionsForRendering();

        if (!blockEntity.isActive() || targetPositions == null || targetPositions.length == 0) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        // Get the block's world position as the starting point
        BlockPos blockPos = blockEntity.getBlockPos();
        Vec3 blockWorldPos = Vec3.atCenterOf(blockPos).add(0, 0.5, 0); // Slightly above center

        // Start from the block's world position
        Vec3 currentWorldPos = blockWorldPos;

        // Draw connected lines between all points in sequence
        for (Vec3 nextWorldPos : targetPositions) {
            if (nextWorldPos != null) {
                // Convert world coordinates to relative coordinates for rendering
                Vec3 startPosRelative = currentWorldPos.subtract(Vec3.atLowerCornerOf(blockPos));
                Vec3 endPosRelative = nextWorldPos.subtract(Vec3.atLowerCornerOf(blockPos));

                renderBeamBetween(poseStack, bufferSource, partialTick, level.getGameTime(),
                        startPosRelative, endPosRelative, color, beamRadius);

                // Move to the next point for the next segment
                currentWorldPos = nextWorldPos;
            }
        }
    }

    /**
     * Renders a beam between two 3D vectors.
     *
     * @param poseStack    The current pose stack.
     * @param bufferSource The buffer source.
     * @param partialTick  The partial tick for smooth animation.
     * @param gameTime     The current game time for texture animation.
     * @param startPos     The starting position of the beam, relative to the current pose.
     * @param endPos       The ending position of the beam, relative to the current pose.
     * @param color        The ARGB color of the beam.
     * @param radius       The radius (thickness) of the beam.
     */
    private static void renderBeamBetween(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime, Vec3 startPos, Vec3 endPos, int color, float radius) {
        // 1. Calculate the difference vector and the length of the beam.
        Vec3 difference = endPos.subtract(startPos);
        float length = (float) difference.length();

        // Don't render a zero-length beam.
        if (length < 1.0E-5F) {
            return;
        }

        // 2. Calculate the rotation needed to align the Y-axis with our beam's direction.
        Vec3 direction = difference.normalize();
        Vec3 upVector = new Vec3(0, 1, 0); // The direction a normal beam points (straight up).

        // The axis of rotation is the cross product of the "up" vector and our target direction.
        Vec3 rotationAxis = upVector.cross(direction).normalize();

        // The angle of rotation is the arccosine of the dot product.
        float angle = (float) Math.acos(upVector.dot(direction));

        // Handle the edge case where the direction is parallel to the Y-axis (dot product is 1 or -1)
        // In this case, the cross product is a zero vector, which can't be normalized.
        if (Math.abs(upVector.dot(direction)) > 0.99999) {
            rotationAxis = new Vec3(1, 0, 0); // Use any perpendicular axis
        }

        // 3. Create the rotation quaternion.
        Quaternionf rotation = new Quaternionf().rotateAxis(angle, (float)rotationAxis.x, (float)rotationAxis.y, (float)rotationAxis.z);
        // --- Start Rendering Transformations ---
        poseStack.pushPose();

        // 4. Translate to the starting position of the beam.
        poseStack.translate(startPos.x, startPos.y, startPos.z);

        // 5. Apply the rotation.
        poseStack.mulPose(rotation);

        // 6. Now that the coordinate system is aligned, render a standard vertical beam.
        // This beam will be drawn along the new Y-axis, from y=0 to y=length.
        float textureOffset = (float) Math.floorMod(gameTime, 40L) + partialTick;
        float vScroll = Mth.frac(-textureOffset * 0.5F - (float) Mth.floor(-textureOffset * 0.1F));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.beaconBeam(BEAM_TEXTURE, false)); // true for transparency

        renderBeamSegment(poseStack, consumer, color, 0, length, radius, vScroll);

        poseStack.popPose();
        // --- End Rendering Transformations ---
    }

    /**
     * Renders the four faces of a vertical beam segment.
     */
    private static void renderBeamSegment(PoseStack poseStack, VertexConsumer consumer, int color, float yOffset, float height, float radius, float vScroll) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float endY = yOffset + height;
        float u0 = 0.0F;
        float u1 = 1.0F;
        // Calculate texture coordinates for the scrolling effect.
        float v0 = -1.0F + vScroll;
        float v1 = height * (2.5F) + v0;

        // Render the four rectangular faces of the beam.
        // Face 1
        addVertex(pose, consumer, color, -radius, yOffset, radius, u1, v0);
        addVertex(pose, consumer, color, -radius, endY, radius, u1, v1);
        addVertex(pose, consumer, color, radius, endY, radius, u0, v1);
        addVertex(pose, consumer, color, radius, yOffset, radius, u0, v0);

        // Face 2
        addVertex(pose, consumer, color, -radius, yOffset, -radius, u1, v0);
        addVertex(pose, consumer, color, -radius, endY, -radius, u1, v1);
        addVertex(pose, consumer, color, -radius, endY, radius, u0, v1);
        addVertex(pose, consumer, color, -radius, yOffset, radius, u0, v0);

        // Face 3
        addVertex(pose, consumer, color, radius, yOffset, -radius, u1, v0);
        addVertex(pose, consumer, color, radius, endY, -radius, u1, v1);
        addVertex(pose, consumer, color, -radius, endY, -radius, u0, v1);
        addVertex(pose, consumer, color, -radius, yOffset, -radius, u0, v0);

        // Face 4
        addVertex(pose, consumer, color, radius, yOffset, radius, u1, v0);
        addVertex(pose, consumer, color, radius, endY, radius, u1, v1);
        addVertex(pose, consumer, color, radius, endY, -radius, u0, v1);
        addVertex(pose, consumer, color, radius, yOffset, -radius, u0, v0);
    }

    /**
     * Helper method to add a vertex to the consumer with all necessary data.
     */
    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, int color, float x, float y, float z, float u, float v) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        // Use full alpha if it's not specified in the color.
        if (a == 0) a = 255;

        consumer.addVertex(pose , x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // Full brightness lightmap value
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(BeamBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(BeamBlockEntity blockEntity, Vec3 cameraPos) {
        // Always render if beam is active
        return blockEntity.isActive();
    }

    @Override
    public AABB getRenderBoundingBox(BeamBlockEntity blockEntity) {
        // Create bounding box that encompasses all target positions
        if (!blockEntity.isActive() || blockEntity.getPositionsForRendering() == null) {
            return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        Vec3[] positions = blockEntity.getPositionsForRendering();
        BlockPos blockPos = blockEntity.getBlockPos();
        Vec3 blockWorldPos = Vec3.atCenterOf(blockPos);

        // Start with block position
        double minX = blockWorldPos.x;
        double minY = blockWorldPos.y;
        double minZ = blockWorldPos.z;
        double maxX = blockWorldPos.x;
        double maxY = blockWorldPos.y;
        double maxZ = blockWorldPos.z;

        // Expand to include all target positions
        for (Vec3 pos : positions) {
            if (pos != null) {
                minX = Math.min(minX, pos.x);
                minY = Math.min(minY, pos.y);
                minZ = Math.min(minZ, pos.z);
                maxX = Math.max(maxX, pos.x);
                maxY = Math.max(maxY, pos.y);
                maxZ = Math.max(maxZ, pos.z);
            }
        }

        // Add some padding
        return new AABB(minX - 2, minY - 2, minZ - 2, maxX + 2, maxY + 2, maxZ + 2);
    }
}