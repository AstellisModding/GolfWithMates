package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class PhysicsUtils {

    public static Vec3 calculateRebound(Vec3 incomingVelocity, Vec3 surfaceNormal, double bounciness) {
        double dotProduct = incomingVelocity.dot(surfaceNormal);
        Vec3 reflectionVector = surfaceNormal.scale(2 * dotProduct);
        return incomingVelocity.subtract(reflectionVector).scale(bounciness);
    }

    public static Vec3 getBlockFaceNormal(Vec3 impactPos, BlockPos blockPos) {
        Vec3 relativeImpact = impactPos.subtract(Vec3.atCenterOf(blockPos));
        double absX = Math.abs(relativeImpact.x);
        double absY = Math.abs(relativeImpact.y);
        double absZ = Math.abs(relativeImpact.z);

        if (absX > absY && absX > absZ) return new Vec3(Math.signum(relativeImpact.x), 0, 0);
        if (absY > absX && absY > absZ) return new Vec3(0, Math.signum(relativeImpact.y), 0);
        return new Vec3(0, 0, Math.signum(relativeImpact.z));
    }

    public static float calculateAngleOfAttack(BlockPos start, BlockPos end) {
        double dx = end.getX() - start.getX();
        double dz = end.getZ() - start.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx));
    }
}