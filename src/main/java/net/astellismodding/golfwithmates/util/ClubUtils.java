package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClubUtils {

    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

    /**
     * Calculates a target location on the XZ plane that is 90 degrees to the left of a given direction.
     * Assumes the rotation is a standard Minecraft yaw value.
     *
     * @param initialX   The starting X coordinate.
     * @param initialY   The starting Y coordinate.
     * @param initialZ   The starting Z coordinate.
     * @param facingYaw  The initial direction in degrees (Minecraft yaw, where 0 is South). The calculation will be offset 90 degrees left of this.
     * @param velocity   The velocity multiplier.
     * @param driveType  An additional multiplier for power (e.g., club type).
     * @return A new Vec3 representing the final rounded location on the Y=0 plane.
     */
    public static Vec3 calculateHitResultAbsoluteLocation(double initialX,double initialY, double initialZ, float facingYaw, double velocity, int driveType) {
        // 1. Calculate the total power of the hit
        double power = 16 * velocity * driveType;

        // 2. Adjust the angle to be 90 degrees to the left of the facing direction
        //    Subtracting 90 degrees from the yaw achieves this.
        float leftYaw = facingYaw - 90.0f;

        // 3. Convert the new 'left' yaw from degrees to radians
        double angleRadians = Math.toRadians(leftYaw);

        // 4. Calculate the change in X and Z using the adjusted angle
        double deltaX = -power * Math.sin(angleRadians);
        double deltaZ = power * Math.cos(angleRadians);

        // 5. Calculate the new, precise location by adding the change
        double newX = initialX + deltaX;
        double newZ = initialZ + deltaZ;

        // 6. Return the location rounded to the nearest whole block coordinate
        return new Vec3(Math.round(newX), initialY, Math.round(newZ));
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
