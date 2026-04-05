# Written Files — Full Source

These are the four files designed and approved in the planning session.
Copy these directly into your mod's `util` package.

---

## PathNode.java

```java
package net.astellismodding.golfwithmates.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class PathNode {

    public enum NodeType {
        FLIGHT,
        BOUNCE,
        ROLL,
        REST
    }

    public final Vec3 position;
    public final Vec3 velocity;
    public final float speed;
    public final NodeType type;

    public PathNode(Vec3 position, Vec3 velocity, NodeType type) {
        this.position = position;
        this.velocity = velocity;
        this.speed = (float) velocity.length();
        this.type = type;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        CompoundTag pos = new CompoundTag();
        pos.putDouble("x", position.x);
        pos.putDouble("y", position.y);
        pos.putDouble("z", position.z);
        tag.put("pos", pos);

        CompoundTag vel = new CompoundTag();
        vel.putDouble("x", velocity.x);
        vel.putDouble("y", velocity.y);
        vel.putDouble("z", velocity.z);
        tag.put("vel", vel);

        tag.putString("type", type.name());
        return tag;
    }

    public static PathNode fromNbt(CompoundTag tag) {
        CompoundTag pos = tag.getCompound("pos");
        Vec3 position = new Vec3(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z"));

        CompoundTag vel = tag.getCompound("vel");
        Vec3 velocity = new Vec3(vel.getDouble("x"), vel.getDouble("y"), vel.getDouble("z"));

        NodeType type = NodeType.valueOf(tag.getString("type"));
        return new PathNode(position, velocity, type);
    }
}
```

---

## ShotResult.java

```java
package net.astellismodding.golfwithmates.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShotResult {

    public final List<PathNode> path;
    public final boolean reachedHole;
    public final int totalBounces;

    public ShotResult(List<PathNode> path, boolean reachedHole) {
        this.path = Collections.unmodifiableList(new ArrayList<>(path));
        this.reachedHole = reachedHole;
        this.totalBounces = (int) path.stream()
                .filter(n -> n.type == PathNode.NodeType.BOUNCE)
                .count();
    }

    public PathNode getRestNode() {
        if (path.isEmpty()) return null;
        return path.get(path.size() - 1);
    }

    public int getPathLength() {
        return path.size();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag nodeList = new ListTag();
        for (PathNode node : path) {
            nodeList.add(node.toNbt());
        }
        tag.put("path", nodeList);
        tag.putBoolean("reachedHole", reachedHole);
        return tag;
    }

    public static ShotResult fromNbt(CompoundTag tag) {
        ListTag nodeList = tag.getList("path", Tag.TAG_COMPOUND);
        List<PathNode> path = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            path.add(PathNode.fromNbt(nodeList.getCompound(i)));
        }
        boolean reachedHole = tag.getBoolean("reachedHole");
        return new ShotResult(path, reachedHole);
    }

    public static ShotResult empty() {
        return new ShotResult(new ArrayList<>(), false);
    }
}
```

---

## PhysicsUtils.java

```java
package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class PhysicsUtils {

    public static final double STOP_THRESHOLD = 0.05;
    public static final double GRAVITY = 0.08;

    public static Vec3 calculateRebound(Vec3 incomingVelocity, Vec3 surfaceNormal, double bounciness) {
        double dotProduct = incomingVelocity.dot(surfaceNormal);
        Vec3 reflection = incomingVelocity.subtract(surfaceNormal.scale(2 * dotProduct));
        return reflection.scale(bounciness);
    }

    public static Vec3 getBlockFaceNormal(Vec3 impactPos, BlockPos blockPos) {
        Vec3 relative = impactPos.subtract(Vec3.atCenterOf(blockPos));
        double absX = Math.abs(relative.x);
        double absY = Math.abs(relative.y);
        double absZ = Math.abs(relative.z);

        if (absX > absY && absX > absZ) return new Vec3(Math.signum(relative.x), 0, 0);
        if (absY > absX && absY > absZ) return new Vec3(0, Math.signum(relative.y), 0);
        return new Vec3(0, 0, Math.signum(relative.z));
    }

    public static Vec3 applyFriction(Vec3 velocity, Block surfaceBlock) {
        double friction = getFrictionCoefficient(surfaceBlock);
        return new Vec3(velocity.x * friction, velocity.y, velocity.z * friction);
    }

    public static double getFrictionCoefficient(Block block) {
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) return 0.98;
        if (block == Blocks.STONE || block == Blocks.STONE_BRICKS)                          return 0.90;
        if (block == Blocks.GRASS_BLOCK || block == Blocks.SHORT_GRASS)                     return 0.82;
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT)                            return 0.78;
        if (block == Blocks.SAND || block == Blocks.GRAVEL)                                 return 0.60;
        if (block == Blocks.SLIME_BLOCK)                                                     return 0.50;
        if (block == Blocks.SOUL_SAND)                                                       return 0.40;
        return 0.78;
    }

    public static Vec3 calculateLaunchVelocity(double speed, float yaw, float launchAngle) {
        double yawRad   = Math.toRadians(yaw - 90.0f);
        double pitchRad = Math.toRadians(launchAngle);

        double horizontalSpeed = speed * Math.cos(pitchRad);
        double verticalSpeed   = speed * Math.sin(pitchRad);

        double vx = -horizontalSpeed * Math.sin(yawRad);
        double vz =  horizontalSpeed * Math.cos(yawRad);
        double vy =  verticalSpeed;

        return new Vec3(vx, vy, vz);
    }

    public static Vec3 applyGravity(Vec3 velocity) {
        return new Vec3(velocity.x, velocity.y - GRAVITY, velocity.z);
    }

    public static Vec3 calculateFlatLaunchVelocity(double speed, float yaw) {
        return calculateLaunchVelocity(speed, yaw, 0.0f);
    }
}
```

---

## TrajectoryCalculator.java

```java
package net.astellismodding.golfwithmates.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryCalculator {

    private static final double STEP_SIZE     = 0.25;
    private static final int    MAX_ITERATIONS = 2000;
    private static final int    MAX_BOUNCES    = 16;

    // --- Entry points ---

    public static ShotResult simulatePutterShot(Vec3 startPos, float yaw, double speed, Level world) {
        Vec3 initialVelocity = PhysicsUtils.calculateFlatLaunchVelocity(speed, yaw);
        return simulate(startPos, initialVelocity, world, false);
    }

    public static ShotResult simulateParabolicShot(Vec3 startPos, float yaw, double speed, float launchAngle, Level world) {
        Vec3 initialVelocity = PhysicsUtils.calculateLaunchVelocity(speed, yaw, launchAngle);
        return simulate(startPos, initialVelocity, world, true);
    }

    // --- Core simulation loop ---

    private static ShotResult simulate(Vec3 startPos, Vec3 initialVelocity, Level world, boolean applyGravity) {
        List<PathNode> nodes = new ArrayList<>();
        nodes.add(new PathNode(startPos, initialVelocity, PathNode.NodeType.FLIGHT));

        Vec3 pos = startPos;
        Vec3 vel = initialVelocity;

        int     iterations  = 0;
        int     bounceCount = 0;
        boolean grounded    = false;

        while (iterations < MAX_ITERATIONS && vel.length() > PhysicsUtils.STOP_THRESHOLD) {
            iterations++;

            Vec3 stepVel = vel.normalize().scale(STEP_SIZE);
            Vec3 nextPos = pos.add(stepVel);

            if (applyGravity && !grounded) {
                vel = PhysicsUtils.applyGravity(vel);
            }

            BlockPos nextBlockPos  = BlockPos.containing(nextPos);
            BlockPos belowBlockPos = nextBlockPos.below();

            BlockState nextBlock  = world.getBlockState(nextBlockPos);
            BlockState belowBlock = world.getBlockState(belowBlockPos);

            // CASE 1: Solid block in path — rebound
            if (!nextBlock.isAir() && nextBlock.isSolid()) {
                if (bounceCount >= MAX_BOUNCES) {
                    nodes.add(new PathNode(pos, Vec3.ZERO, PathNode.NodeType.REST));
                    break;
                }
                Vec3 surfaceNormal = PhysicsUtils.getBlockFaceNormal(nextPos, nextBlockPos);
                double bounciness  = getBounciness(nextBlock.getBlock());
                vel = PhysicsUtils.calculateRebound(vel, surfaceNormal, bounciness);

                nodes.add(new PathNode(pos, vel, PathNode.NodeType.BOUNCE));
                bounceCount++;
                grounded = false;
                continue; // don't advance pos
            }

            // CASE 2: No floor — airborne
            if (belowBlock.isAir() || !belowBlock.isSolid()) {
                pos = nextPos;
                grounded = false;
                if (iterations % 4 == 0) {
                    nodes.add(new PathNode(pos, vel, PathNode.NodeType.FLIGHT));
                }
                continue;
            }

            // CASE 3: Floor present — rolling
            pos = nextPos;
            grounded = true;
            vel = PhysicsUtils.applyFriction(vel, belowBlock.getBlock());
            if (iterations % 4 == 0) {
                nodes.add(new PathNode(pos, vel, PathNode.NodeType.ROLL));
            }
        }

        if (nodes.isEmpty() || nodes.get(nodes.size() - 1).type != PathNode.NodeType.REST) {
            nodes.add(new PathNode(pos, Vec3.ZERO, PathNode.NodeType.REST));
        }

        // TODO: hole detection — check if pos is inside a hole block
        return new ShotResult(nodes, false);
    }

    // --- Bounciness lookup ---

    private static double getBounciness(Block block) {
        if (block == Blocks.SLIME_BLOCK)                                    return 0.95;
        if (block == Blocks.STONE || block == Blocks.STONE_BRICKS)         return 0.60;
        if (block == Blocks.GRASS_BLOCK)                                    return 0.30;
        if (block == Blocks.HAY_BLOCK)                                      return 0.20;
        if (block == Blocks.SAND)                                           return 0.15;
        if (block == Blocks.SOUL_SAND)                                      return 0.05;
        return 0.40;
    }
}
```
