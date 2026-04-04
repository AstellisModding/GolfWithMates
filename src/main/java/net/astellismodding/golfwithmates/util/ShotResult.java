package net.astellismodding.golfwithmates.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The complete output of a single golf shot simulation.
 * Produced once by TrajectoryCalculator on the server, stored in GolfBallBlockEntity,
 * synced to the client, and consumed by the renderer.
 *
 * Immutable after construction — if a new shot is hit, replace the whole ShotResult.
 */
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

    // -------------------------------------------------------------------------
    // Convenience getters
    // -------------------------------------------------------------------------

    /**
     * The position where the ball came to rest. Always the last node.
     * Returns null if the path is somehow empty (should never happen in practice).
     */
    public PathNode getRestNode() {
        if (path.isEmpty()) return null;
        return path.get(path.size() - 1);
    }

    /**
     * Total number of nodes — useful for animation progress tracking later.
     */
    public int getPathLength() {
        return path.size();
    }

    // -------------------------------------------------------------------------
    // NBT serialisation
    // -------------------------------------------------------------------------

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

    /**
     * Returns an empty ShotResult — used to clear state between shots
     * without null-checking everywhere in the BE.
     */
    public static ShotResult empty() {
        return new ShotResult(new ArrayList<>(), false);
    }
}