package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full golf ball simulation.
 * Calls PhysicsUtils for math, reads the world for collision, produces a ShotResult.
 *
 * All simulation runs on the SERVER only — never call this client-side.
 * The resulting ShotResult is synced to the client via GolfBallBlockEntity.
 */
public class TrajectoryCalculator {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Sub-block step size. Smaller = more accurate collision, more iterations. 0.25 is a good balance.
     *  this effects animation and distance of shots, keep at 0.1 for now unless for a good reason
     *  follow up with distance checks and animation quality after editing
     * */
    private static final double STEP_SIZE = 0.1;

    /** Hard cap on iterations — prevents infinite loops on pathological inputs. */
    private static final int MAX_ITERATIONS = 10000;

    /**
     * Max consecutive bounces before we force the ball to rest.
     * Prevents the ball from pinballing forever in a corner.
     */
    private static final int MAX_BOUNCES = 16;

    // -------------------------------------------------------------------------
    // Entry points
    // -------------------------------------------------------------------------

    /**
     * Simulates a putter shot — flat XZ plane, full block rebound, friction deceleration.
     *
     * @param startPos  World position the ball is hit from.
     * @param yaw       Player facing yaw in degrees (Minecraft convention).
     * @param speed     Initial speed (roughly: speed N = travels up to N blocks).
     * @param world     Server-side Level for block lookups.
     * @return Complete ShotResult with full path of PathNodes.
     */
    public static ShotResult simulatePutterShot(Vec3 startPos, float yaw, double speed, Level world) {
        Vec3 initialVelocity = PhysicsUtils.calculateFlatLaunchVelocity(speed, yaw);
        return simulate(startPos, initialVelocity, world, false);
    }

    /**
     * Simulates a parabolic shot — drivers, irons, wedges.
     * Gravity is applied each step. Bounces on landing.
     *
     * @param startPos    World position the ball is hit from.
     * @param yaw         Player facing yaw in degrees.
     * @param speed       Initial scalar speed.
     * @param launchAngle Vertical launch angle in degrees (e.g. 15 for driver, 45 for wedge).
     * @param world       Server-side Level for block lookups.
     * @return Complete ShotResult with full path of PathNodes.
     */
    public static ShotResult simulateParabolicShot(Vec3 startPos, float yaw, double speed, float launchAngle, Level world) {
        Vec3 initialVelocity = PhysicsUtils.calculateLaunchVelocity(speed, yaw, launchAngle);
        return simulate(startPos, initialVelocity, world, true);
    }

    // -------------------------------------------------------------------------
    // Core simulation loop
    // -------------------------------------------------------------------------

    /**
     * Iterative simulation engine shared by all shot types.
     * Walks the ball forward in STEP_SIZE increments, checks for collisions,
     * applies physics, and records a PathNode at each meaningful event.
     *
     * @param startPos        Starting world position.
     * @param initialVelocity Initial velocity vector from PhysicsUtils.
     * @param world           Server-side Level.
     * @param applyGravity    True for parabolic shots, false for putter.
     * @return ShotResult containing the complete path.
     */
    private static ShotResult simulate(Vec3 startPos, Vec3 initialVelocity, Level world, boolean applyGravity) {
        List<PathNode> nodes = new ArrayList<>();
        nodes.add(new PathNode(startPos, initialVelocity, PathNode.NodeType.FLIGHT));

        Vec3 pos = startPos;
        Vec3 vel = initialVelocity;

        int iterations   = 0;
        int bounceCount  = 0;
        int optimizer = 4;
        boolean grounded = false;

        while (iterations < MAX_ITERATIONS && vel.length() > PhysicsUtils.STOP_THRESHOLD) {
            iterations++;

            // --- Compute the next candidate position ---
            Vec3 stepVel  = vel.scale(STEP_SIZE);
            Vec3 nextPos  = pos.add(stepVel);

            // --- Apply gravity if airborne ---
            if (applyGravity && !grounded) {
                vel = PhysicsUtils.applyGravity(vel, STEP_SIZE);
            }

            // --- Block collision checks ---
            BlockPos nextBlockPos  = BlockPos.containing(nextPos);
            BlockPos belowBlockPos = nextBlockPos.below();

            BlockState nextBlock  = world.getBlockState(nextBlockPos);
            BlockState belowBlock = world.getBlockState(belowBlockPos);

            // CASE 1: Solid block in path — distinguish landing from above vs side collision
            if (!nextBlock.isAir() && nextBlock.isSolid()) {
                if (bounceCount >= MAX_BOUNCES) {
                    nodes.add(new PathNode(pos, Vec3.ZERO, PathNode.NodeType.REST));
                    break;
                }

                boolean landingFromAbove = pos.y >= nextBlockPos.getY() + 1.0;

                if (landingFromAbove) {
                    // Snap to the top surface — prevents the ball hovering above the floor
                    double landY = nextBlockPos.getY() + 1.0;
                    double bounciness = getBounciness(nextBlock.getBlock());
                    double reboundY   = Math.abs(vel.y) * bounciness;

                    pos = new Vec3(nextPos.x, landY, nextPos.z);

                    if (reboundY > PhysicsUtils.STOP_THRESHOLD) {
                        vel = new Vec3(vel.x, reboundY, vel.z);
                        nodes.add(new PathNode(pos, vel, PathNode.NodeType.BOUNCE));
                        bounceCount++;
                        grounded = false;
                    } else {
                        // Not enough energy to bounce — settle and roll
                        vel = new Vec3(vel.x, 0, vel.z);
                        vel = PhysicsUtils.applyFriction(vel, nextBlock.getBlock(), STEP_SIZE);
                        grounded = true;
                        if (iterations % optimizer == 0) {
                            nodes.add(new PathNode(pos, vel, PathNode.NodeType.ROLL));
                        }
                    }
                } else {
                    // Side collision — rebound off the face
                    Vec3 surfaceNormal = PhysicsUtils.getBlockFaceNormal(nextPos, nextBlockPos);
                    double bounciness  = getBounciness(nextBlock.getBlock());
                    vel = PhysicsUtils.calculateRebound(vel, surfaceNormal, bounciness);
                    nodes.add(new PathNode(pos, vel, PathNode.NodeType.BOUNCE));
                    bounceCount++;
                    grounded = false;
                    // Don't advance pos — recalculate direction from same position
                }
                continue;
            }

            // CASE 2: No floor beneath, OR ball moving upward after a bounce — airborne
            if (belowBlock.isAir() || !belowBlock.isSolid() || vel.y > PhysicsUtils.STOP_THRESHOLD) {
                pos = nextPos;
                grounded = false;

                if (iterations % optimizer == 0) {
                    nodes.add(new PathNode(pos, vel, PathNode.NodeType.FLIGHT));
                }
                continue;
            }

            // CASE 3: Floor exists and ball is not rising — rolling, apply friction
            pos = nextPos;
            grounded = true;
            vel = PhysicsUtils.applyFriction(vel, belowBlock.getBlock(), STEP_SIZE);

            // Record a ROLL node every 4 steps
            if (iterations % optimizer == 0) {
                nodes.add(new PathNode(pos, vel, PathNode.NodeType.ROLL));
            }
        }

        // --- Always end with a REST node at the final position ---
        if (nodes.isEmpty() || nodes.get(nodes.size() - 1).type != PathNode.NodeType.REST) {
            nodes.add(new PathNode(pos, Vec3.ZERO, PathNode.NodeType.REST));
        }

        // TODO: hole detection — roll over hole putts dedtion if requested
        return new ShotResult(nodes, false);
    }

    // -------------------------------------------------------------------------
    // Bounciness lookup
    // -------------------------------------------------------------------------

    /**
     * Per-block bounciness coefficient.
     * 1.0 = perfect bounce (no energy loss), 0.0 = dead stop on impact.
     * Add custom course blocks here as the mod grows.
     *
     * @param block The block that was hit.
     * @return Bounciness coefficient.
     */
    private static double getBounciness(Block block) {
        if (block == net.minecraft.world.level.block.Blocks.SLIME_BLOCK)   return 0.95;
        if (block == net.minecraft.world.level.block.Blocks.HAY_BLOCK)     return 0.20;
        if (block == net.minecraft.world.level.block.Blocks.STONE
                || block == net.minecraft.world.level.block.Blocks.STONE_BRICKS)  return 0.60;
        if (block == net.minecraft.world.level.block.Blocks.GRASS_BLOCK)   return 0.30;
        if (block == net.minecraft.world.level.block.Blocks.SAND)          return 0.15;
        if (block == net.minecraft.world.level.block.Blocks.SOUL_SAND)     return 0.05;
        // Default
        return 0.40;
    }
}