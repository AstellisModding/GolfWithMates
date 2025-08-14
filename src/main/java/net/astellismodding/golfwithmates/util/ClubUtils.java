package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClubUtils {

    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

    //todo Feat: Rebound - notes in this file
    public static Vec3 CalculateHitResultLocation(float x, float z, float r, double v, int driveType) {
        double power = 16 * v * driveType;
        float rot = r;

        double angleRadians = Math.toRadians(rot);
        double deltaX = power * Math.cos(angleRadians);
        double deltaZ = power * Math.sin(angleRadians);
        double newX = x - Math.floor(deltaX);
        double newZ = z - Math.floor(deltaZ);

        return new Vec3(Math.round(newX),0f, Math.round(newZ));
    }



    /**
     * Calculates the new velocity vector after a rebound.
     *
     * @param incomingVelocity The velocity of the object before impact.
     * @param surfaceNormal    The normal vector of the surface that was hit.
     * @param bounciness       A factor from 0.0 to 1.0 that represents how much energy is retained.
     * @return The new velocity vector after the bounce.
     */
    public static Vec3 CalculateRebound(Vec3 incomingVelocity, Vec3 surfaceNormal, double bounciness) {
        // Using the reflection formula: V_out = V_in - 2 * (V_in . N) * N
        double dotProduct = incomingVelocity.dot(surfaceNormal);
        Vec3 reflectionVector = surfaceNormal.scale(2 * dotProduct);
        Vec3 newVelocity = incomingVelocity.subtract(reflectionVector);

        // Apply bounciness to simulate energy loss
        return newVelocity.scale(bounciness);
    }




    /**
     * Determines the normal vector of the block face that was hit.
     * Note: This is a simplified approach. For perfect accuracy, using a RayCast's BlockHitResult is best.
     *
     * @param impactPos The precise Vec3 position of the impact.
     * @param blockPos  The BlockPos of the block that was hit.
     * @return The normal vector of the face that was hit (e.g., (0, 1, 0) for the top face).
     */
    public static Vec3 GetBlockFaceNormal(Vec3 impactPos, BlockPos blockPos) {
        // Find the vector from the center of the block to the impact point.
        Vec3 relativeImpact = impactPos.subtract(Vec3.atCenterOf(blockPos));

        double absX = Math.abs(relativeImpact.x);
        double absY = Math.abs(relativeImpact.y);
        double absZ = Math.abs(relativeImpact.z);

        // Check which component of the relative vector is the largest.
        // This tells us which face is closest to the impact point.
        if (absX > absY && absX > absZ) {
            // Collision is on an X-face (East/West)
            return new Vec3(Math.signum(relativeImpact.x), 0, 0);
        } else if (absY > absX && absY > absZ) {
            // Collision is on a Y-face (Top/Bottom)
            return new Vec3(0, Math.signum(relativeImpact.y), 0);
        } else {
            // Collision is on a Z-face (North/South)
            return new Vec3(0, 0, Math.signum(relativeImpact.z));
        }
    }




    public static float CalculateAngleOfAttack(BlockPos StartLoc, BlockPos HitLoc) {
        double deltaX = HitLoc.getX() - StartLoc.getX();
        double deltaZ = HitLoc.getZ() - StartLoc.getZ();
        double angleRadians = Math.atan2(deltaZ, deltaX);
        return (float) Math.toDegrees(angleRadians);
    }
}
