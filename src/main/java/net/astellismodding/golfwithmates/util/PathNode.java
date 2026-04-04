package net.astellismodding.golfwithmates.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a single point in a golf ball's simulated flight path.
 * Carries position, velocity, speed, and the type of event at this node.
 * Produced by TrajectoryCalculator, consumed by the renderer (later).
 */
public class PathNode {

    public enum NodeType {
        /** Ball is airborne — parabolic or flat XZ travel. */
        FLIGHT,
        /** Ball just struck a solid block face and rebounded. */
        BOUNCE,
        /** Ball is on the ground and decelerating due to friction. */
        ROLL,
        /** Ball has stopped — this is always the final node. */
        REST
    }

    public final Vec3 position;
    public final Vec3 velocity;   // direction + magnitude as a single vector
    public final float speed;     // scalar cache of velocity.length() — handy for renderer later
    public final NodeType type;

    public PathNode(Vec3 position, Vec3 velocity, NodeType type) {
        this.position = position;
        this.velocity = velocity;
        this.speed = (float) velocity.length();
        this.type = type;
    }

    // -------------------------------------------------------------------------
    // NBT serialisation — needed so GolfBallBlockEntity can save/load ShotResult
    // -------------------------------------------------------------------------

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