package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryCalculator {

    public static Vec3 calculateHitResult(Vec3 initialPos, float yaw, double velocity, int driveType) {
        double power = 16 * velocity * driveType;
        float leftYaw = yaw - 90.0f;
        double angleRadians = Math.toRadians(leftYaw);
        double deltaX = -power * Math.sin(angleRadians);
        double deltaZ = power * Math.cos(angleRadians);
        return new Vec3(Math.round(initialPos.x + deltaX), initialPos.y, Math.round(initialPos.z + deltaZ));
    }

    public static List<Vec3> calculatePath(Vec3 start, Vec3 end, int velocity) {
        List<Vec3> path = new ArrayList<>();
        Vec3 direction = end.subtract(start).normalize();
        Vec3 current = start;

        for (int i = 0; i < velocity; i++) {
            current = current.add(direction);
            path.add(current);
        }

        return path;
    }

    public static Vec3 calculateNextRebound(Vec3 currentVelocity, BlockPos hitBlock, Vec3 impactPoint, double bounciness) {
        Vec3 surfaceNormal = PhysicsUtils.getBlockFaceNormal(impactPoint, hitBlock);
        return PhysicsUtils.calculateRebound(currentVelocity, surfaceNormal, bounciness);
    }

    public static void simulateShot(Vec3 initialPosition, float yaw, double velocity, int driveType, Level world) {
        Vec3 endPosition = calculateHitResult(initialPosition, yaw, velocity, driveType);
        List<Vec3> path = calculatePath(initialPosition, endPosition, (int) (16 * velocity));

        for (Vec3 point : path) {
            BlockPos pos = new BlockPos((int) point.x, (int) point.y, (int) point.z);
            if (!world.getBlockState(pos).isAir()) {
                // Collision detected – rebound
                Vec3 reboundVelocity = calculateNextRebound(
                        endPosition.subtract(initialPosition).normalize(),
                        pos,
                        point,
                        0.8 // Example bounciness
                );

                // Recurse with new trajectory
                simulateShot(point, PhysicsUtils.calculateAngleOfAttack(
                        new BlockPos(initialPosition), new BlockPos(point)
                ), reboundVelocity.length(), 1, world);
                return;
            }
        }
        // Ball reached end with no collision
    }
}