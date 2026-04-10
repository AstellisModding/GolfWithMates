package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Pure physics math — no world access, no game logic.
 * All methods are stateless and take only what they need.
 * TrajectoryCalculator orchestrates these into a full simulation.
 */
public class PhysicsUtils {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Ball stops when speed drops below this. Avoids infinite decel loops. */
    public static final double STOP_THRESHOLD = 0.05;

    /** Gravity constant per simulation step (not per tick — applied in TrajectoryCalculator). */
    public static final double GRAVITY = 0.08;

    /**
     * The STEP_SIZE the friction and gravity constants were tuned for.
     * Changing STEP_SIZE in TrajectoryCalculator without updating this would break physics —
     * instead, pass the current STEP_SIZE into applyFriction/applyGravity and they scale automatically.
     */
    public static final double REFERENCE_STEP_SIZE = 0.25;

    // -------------------------------------------------------------------------
    // Rebound / Collision
    // -------------------------------------------------------------------------

    /**
     * Reflects an incoming velocity vector off a surface and applies energy loss.
     * Formula: V_out = (V_in - 2(V_in · N)N) * bounciness
     *
     * @param incomingVelocity  Velocity before impact.
     * @param surfaceNormal     The normal of the face that was hit (should be unit length).
     * @param bounciness        Energy retention factor, 0.0 (dead stop) to 1.0 (perfect bounce).
     * @return New velocity vector after rebound.
     */
    public static Vec3 calculateRebound(Vec3 incomingVelocity, Vec3 surfaceNormal, double bounciness) {
        double dotProduct = incomingVelocity.dot(surfaceNormal);
        Vec3 reflection = incomingVelocity.subtract(surfaceNormal.scale(2 * dotProduct));
        return reflection.scale(bounciness);
    }

    /**
     * Determines which face of a block was hit based on the impact position.
     * Works by finding which axis the impact point deviates most from block centre on.
     *
     * @param impactPos World position of the impact.
     * @param blockPos  The block that was hit.
     * @return Unit normal vector of the hit face.
     */
    public static Vec3 getBlockFaceNormal(Vec3 impactPos, BlockPos blockPos) {
        Vec3 relative = impactPos.subtract(Vec3.atCenterOf(blockPos));
        double absX = Math.abs(relative.x);
        double absY = Math.abs(relative.y);
        double absZ = Math.abs(relative.z);

        if (absX > absY && absX > absZ) return new Vec3(Math.signum(relative.x), 0, 0);
        if (absY > absX && absY > absZ) return new Vec3(0, Math.signum(relative.y), 0);
        return new Vec3(0, 0, Math.signum(relative.z));
    }

    // -------------------------------------------------------------------------
    // Friction
    // -------------------------------------------------------------------------

    /**
     * Applies ground friction scaled to the current step size.
     * Table values are tuned for REFERENCE_STEP_SIZE — passing a different stepSize
     * ensures the same energy loss per block regardless of simulation resolution.
     *
     * @param velocity     Current velocity.
     * @param surfaceBlock The block the ball is rolling on.
     * @param stepSize     The simulation step size (STEP_SIZE from TrajectoryCalculator).
     * @return New velocity after friction is applied.
     */
    public static Vec3 applyFriction(Vec3 velocity, Block surfaceBlock, double stepSize) {
        double friction = getFrictionCoefficient(surfaceBlock);
        double scaledFriction = Math.pow(friction, stepSize / REFERENCE_STEP_SIZE);
        return new Vec3(velocity.x * scaledFriction, velocity.y, velocity.z * scaledFriction);
    }

    /**
     * Friction coefficient lookup table per block type.
     * 1.0 = frictionless, 0.0 = instant stop.
     * Add new blocks here as you add course block types.
     *
     * @param block The surface block.
     * @return Friction coefficient for that surface.
     */
    public static double getFrictionCoefficient(Block block) {
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) return 0.98;
        if (block == Blocks.STONE || block == Blocks.STONE_BRICKS)                          return 0.90;
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT)                            return 0.78;
        if (block == Blocks.GRASS_BLOCK || block == Blocks.SHORT_GRASS)                     return 0.82;
        if (block == Blocks.SAND || block == Blocks.GRAVEL)                                 return 0.60;
        if (block == Blocks.SOUL_SAND)                                                       return 0.40;
        if (block == Blocks.SLIME_BLOCK)                                                     return 0.50;
        // Default — treat unknown blocks like dirt
        return 0.78;
    }

    // -------------------------------------------------------------------------
    // Parabolic flight (for drivers / wedges — wired up in TrajectoryCalculator later)
    // -------------------------------------------------------------------------

    /**
     * Computes the initial 3D velocity vector for a parabolic shot.
     * The XZ component is derived from yaw, the Y component from launch angle.
     *
     * @param speed        Scalar speed (blocks per step).
     * @param yaw          Minecraft yaw in degrees (0 = South, -90 = East).
     * @param launchAngle  Vertical launch angle in degrees (0 = flat, 90 = straight up).
     * @return 3D velocity vector.
     */
    public static Vec3 calculateLaunchVelocity(double speed, float yaw, float launchAngle) {
        double yawRad    = Math.toRadians(yaw - 90.0f);
        double pitchRad  = Math.toRadians(launchAngle);

        double horizontalSpeed = speed * Math.cos(pitchRad);
        double verticalSpeed   = speed * Math.sin(pitchRad);

        double vx = -horizontalSpeed * Math.sin(yawRad);
        double vz =  horizontalSpeed * Math.cos(yawRad);

        return new Vec3(vx, verticalSpeed, vz);
    }

    /**
     * Applies one step of gravity scaled to the current step size.
     *
     * @param velocity Current velocity.
     * @param stepSize The simulation step size (STEP_SIZE from TrajectoryCalculator).
     * @return Velocity with gravity applied to Y.
     */
    public static Vec3 applyGravity(Vec3 velocity, double stepSize) {
        return new Vec3(velocity.x, velocity.y - GRAVITY * (stepSize / REFERENCE_STEP_SIZE), velocity.z);
    }

    /**
     * For the putter — flat XZ launch, no vertical component.
     * Velocity magnitude maps directly to distance (1 speed = 1 block roughly).
     *
     * @param speed Scalar speed.
     * @param yaw   Minecraft yaw in degrees.
     * @return Flat XZ velocity vector with Y = 0.
     */
    public static Vec3 calculateFlatLaunchVelocity(double speed, float yaw) {
        return calculateLaunchVelocity(speed, yaw, 0.0f);
    }
}