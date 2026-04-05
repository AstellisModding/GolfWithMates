# Golf With Mates — Claude Code Context

## Project Overview
A Minecraft golf mod built on **NeoForge**, targeting **Minecraft 1.21.x**.
Players hit a golf ball block using club items, the ball simulates a full physics path
server-side, stores the result in a BlockEntity, and (eventually) animates along that path client-side.

## Mod Package
```
net.astellismodding.golfwithmates
```

---

## Current State

### ✅ Complete
| File | Package | Notes |
|------|---------|-------|
| `PathNode.java` | `util` | position, velocity, speed, NodeType (FLIGHT/BOUNCE/ROLL/REST) |
| `ShotResult.java` | `util` | Immutable list of PathNodes + metadata, `empty()` default |
| `PhysicsUtils.java` | `util` | Pure math — no world access |
| `TrajectoryCalculator.java` | `util` | Iterative simulation, returns `ShotResult` |
| `ClubUtils.java` | `util` | Stat lookup only — `isClub`, `getShotType`, `getVelocity`, `getBounciness` |
| `ShotType.java` | `util` | PUTTER, IRON, WEDGE, DRIVER enum |
| `GolfBallBlockEntity.java` | `block/entity` | Stores `ShotResult`, puttCounter, isActive, animationTick (pinned) |
| `GolfBallBlock.java` | `block/custom` | Wires TrajectoryCalculator on swing, teleports ball to REST node |

### ✅ Complete
- Sub-block positioning — `subX`/`subZ` (0–2) stored in BlockEntity NBT, used as shot origin and set from `restNode` after simulation

### ✅ Complete
- Sub-block visual rendering — BER `ItemDisplayContext.NONE` + `translate(0.5 + offsetX, 0.5, 0.5 + offsetZ)`, dynamic VoxelShape from sub-cell

### 🔲 Next to implement (in order)
1. Hole detection in `TrajectoryCalculator` (currently stubbed `false`)
2. `ShotType` enum wiring into `simulateParabolicShot` (IRON/DRIVER/WEDGE)
3. **Animation system** — block→entity→block (see below)

---

## Architecture

### Core Principle
**Simulate first, animate later.**
Physics runs entirely server-side in one pass → produces a `ShotResult` → stored in
`GolfBallBlockEntity` → synced to client → renderer plays it back.
Physics and rendering are fully decoupled.

### Class Responsibilities

| Class | Responsibility |
|-------|---------------|
| `PhysicsUtils` | Pure math — rebound, normals, friction, gravity, launch vectors. No world access. |
| `TrajectoryCalculator` | Orchestrates simulation — calls PhysicsUtils, reads world for collisions, returns `ShotResult` |
| `PathNode` | Single point in the path — position, velocity, speed, NodeType |
| `ShotResult` | Full output of one shot — immutable list of PathNodes + metadata |
| `GolfBallBlockEntity` | Stores `ShotResult`, sub-cell position, animation tick (pinned), NBT + sync |
| `ClubUtils` | Stat lookup only — `isClub()`, `getShotType()`, `getVelocity()`, `getBounciness()` |

### Shot Types
```java
public enum ShotType {
    PUTTER,   // flat XZ, full block rebound, high friction
    IRON,     // shallow parabola, moderate distance
    WEDGE,    // steep parabola, short, high backspin friction
    DRIVER    // long flat-ish parabola, low friction on land
}
```

---

## Key Design Decisions

### Sub-Block Positioning (3×3 grid)
Each block is logically divided into a 3×3 grid on the **XZ plane only** (no Y subdivision —
ball always rests on the floor). Sub-position is two integers: `subX` and `subZ`, each 0–2.

**World position → sub-cell:**
```java
int subX = Math.min(2, (int)((worldPos.x - Math.floor(worldPos.x)) * 3));
int subZ = Math.min(2, (int)((worldPos.z - Math.floor(worldPos.z)) * 3));
```

**Sub-cell → world center** (used as start position for next shot):
```java
double preciseX = blockPos.getX() + (subX + 0.5) / 3.0;
double preciseZ = blockPos.getZ() + (subZ + 0.5) / 3.0;
```

**Stored in `GolfBallBlockEntity`** as `"SubX"` and `"SubZ"` NBT bytes. Default is `(1, 1)` — block center.

**Flow:**
1. On swing: read `subX`/`subZ` from entity → compute precise `startPos` (instead of block center)
2. After simulation: extract sub-cell from `restNode.position` → set on entity before teleport
3. NBT copy in `teleportToResult` carries sub-cell to the new entity automatically

The 3×3 grid is a starting resolution. The visual system (below) is fully dynamic — increasing
precision in the future only requires changing the grid divisor, not the rendering approach.

### Sub-Block Visual Rendering (Option 1 + 3 — dynamic, no model variants)
Use a `BlockEntityRenderer` matrix offset + dynamic `VoxelShape` computed from sub-cell at runtime.
No model variants, no block state properties, no datagen changes.

**BER offset** (visual position):
```java
// In GolfBallBlockEntityRender.render()
double offsetX = (subX - 1) / 3.0; // -0.333, 0, +0.333
double offsetZ = (subZ - 1) / 3.0;
poseStack.translate(0.5 + offsetX, 0.5, 0.5 + offsetZ);
// ItemDisplayContext.NONE — item centers at origin, +0.5 XYZ puts block bottom on floor
itemRenderer.renderStatic(ballStack, ItemDisplayContext.NONE, ...);
```

**Dynamic VoxelShape** (hitbox):
- Override `getShape()` to shift the bounding box based on sub-cell
- Pre-build 9 shapes at class load time, index by `subZ * 3 + subX`
- Requires reading the BlockEntity from the world in `getShape()` — NeoForge supports this

Both the offset and shape use the same sub-cell values, so visual and hitbox stay in sync.
Upgrading to finer grid in future = change the divisor in both places.

### Animation System (block → entity → block)
On swing the ball temporarily becomes an entity that lerps along the `ShotResult` path,
then converts back to a block at the `REST` position. Block handles persistence; entity handles motion.

**Server-side flow:**
1. Simulate shot → get `ShotResult`
2. Remove ball block, spawn `GolfBallEntity` at current position with `ShotResult` attached
3. Entity ticks along path nodes at a fixed rate
4. On reaching the `REST` node: place `GolfBallBlock` at that position, copy NBT (incl. `subX`/`subZ` computed from `restNode.position`), remove entity
5. **Fallback**: if chunk unloads mid-animation, entity death handler places the block immediately so the ball is never lost

**Client-side:**
- Entity syncs `ShotResult` via `FriendlyByteBuf` on spawn (data already serialisable — same NBT format)
- Client lerps smoothly between nodes; `NodeType` drives effects (bounce sound on `BOUNCE`, roll particles on `ROLL`)
- Server timing and client animation run independently — server places the block after a pre-calculated tick count based on path length

**Key constraint:** The `ShotResult` serialisation is already solved (PathNode NBT). The entity just reuses it.

### Simulation Loop (TrajectoryCalculator)
- **Iterative, not recursive** — old approach was heading toward stack overflow on long shots
- **Step size = 0.25 blocks** — sub-block accuracy for collision without excessive iterations
- **Hard caps**: `MAX_ITERATIONS = 2000`, `MAX_BOUNCES = 16`
- **Three collision cases per step**:
  1. Solid block in path → rebound (don't advance pos, recalculate direction)
  2. No floor beneath → airborne (advance, record FLIGHT node)
  3. Floor exists → grounded (advance, apply friction, record ROLL node)
- **Node thinning**: FLIGHT and ROLL nodes recorded every 4 steps. BOUNCE always recorded.
- **`applyGravity` boolean flag** — putter and parabolic shots share the same loop

### PathNode
- Carries `position`, `velocity`, `speed` (pre-computed scalar), `NodeType`
- `NodeType` enum: `FLIGHT`, `BOUNCE`, `ROLL`, `REST`
- Has own `toNbt()` / `fromNbt()` — serialisation lives close to the data

### ShotResult
- **Immutable** after construction (`Collections.unmodifiableList`)
- `totalBounces` derived from path at construction — never out of sync
- `ShotResult.empty()` — safe default so BlockEntity never null-checks `currentShot`
- `reachedHole` is a flag, not a NodeType — hole-in is an outcome, not a path event

### PhysicsUtils
- **No world access** — fully unit-testable
- `getFrictionCoefficient(Block)` — single lookup table, add custom course blocks here
- `getBounciness(Block)` lives in `TrajectoryCalculator` (it's simulation policy, not pure math)
- `STOP_THRESHOLD = 0.05`, `GRAVITY = 0.08` — named constants, not magic numbers
- Putter uses `calculateFlatLaunchVelocity()` which calls `calculateLaunchVelocity()` with `launchAngle=0`

### GolfBallBlockEntity
- `ShotResult currentShot` — full simulation output
- `int subX`, `int subZ` — sub-cell position (0–2 each), default `(1, 1)`
- `int animationTick`, `boolean animationDone` — pinned, leave unused until animation system
- `isActive` and `puttCounter` stay as-is
- `setChangedAndUpdate()` pattern used for all state changes that need client sync

---

## What Was Cleaned Up (don't re-introduce)

- `ClubUtils` previously had `CalculateRebound`, `GetBlockFaceNormal`, `CalculateAngleOfAttack`,
  and `calculateHitResultAbsoluteLocation` — deleted. These live in `PhysicsUtils` /
  `TrajectoryCalculator` now.
- `ClubUtils.CalculateShot()` and `ClubUtils.CalculatePath()` — deleted, replaced by
  `TrajectoryCalculator.simulatePutterShot()` / `simulateParabolicShot()`
- Old recursive `simulateShot()` — replaced by iterative loop
- Old `List<Vec3> targetPositions` in BlockEntity — replaced by `ShotResult`

---

## Lookup Tables (extend these as course blocks are added)

### Friction (`PhysicsUtils.getFrictionCoefficient`)
| Block | Coefficient |
|-------|------------|
| Ice / Packed Ice / Blue Ice | 0.98 |
| Stone / Stone Bricks | 0.90 |
| Grass Block | 0.82 |
| Dirt / Coarse Dirt | 0.78 |
| Sand / Gravel | 0.60 |
| Soul Sand | 0.40 |
| Slime Block | 0.50 |
| Default (unknown) | 0.78 |

### Bounciness (`TrajectoryCalculator.getBounciness`)
| Block | Coefficient |
|-------|------------|
| Slime Block | 0.95 |
| Stone / Stone Bricks | 0.60 |
| Grass Block | 0.30 |
| Hay Block | 0.20 |
| Sand | 0.15 |
| Soul Sand | 0.05 |
| Default | 0.40 |