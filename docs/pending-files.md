# Pending Files — Spec for Claude Code

These two files have NOT been written yet. This doc captures exactly what they need to contain
so Claude Code can write them without re-litigating decisions already made.

---

## 1. ClubUtils.java

**Package:** `net.astellismodding.golfwithmates.util`

**Purpose:** Stat lookup only. No physics, no math, no world access.

**The user already deleted all the old physics methods from this file.**
It currently only has `isClub()`. Add the following:

```java
package net.astellismodding.golfwithmates.util;

import net.minecraft.world.item.ItemStack;

public class ClubUtils {

    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

    /**
     * Returns the ShotType for a given club item.
     * Wire this to item tags or NBT data as club items are created.
     */
    public static ShotType getShotType(ItemStack item) {
        // TODO: check item tags to determine club type
        // Placeholder — default to PUTTER until items are created
        return ShotType.PUTTER;
    }

    /**
     * Returns the base speed/power for a given club.
     * Higher = ball travels further.
     */
    public static double getVelocity(ItemStack item) {
        // TODO: read from item data / NBT or tag-based lookup
        return 1.0;
    }

    /**
     * Returns the bounciness override for a club, if any.
     * Most clubs should return -1.0 to signal "use block default".
     * Special clubs (e.g. a rubber-tipped putter) could override.
     */
    public static double getBounciness(ItemStack item) {
        return -1.0; // -1 = use block's own bounciness from TrajectoryCalculator
    }
}
```

**ShotType enum** — write this as its own file or nested in ClubUtils, dealer's choice:

```java
public enum ShotType {
    PUTTER,   // flat XZ, full block rebound, high friction
    IRON,     // shallow parabola (~15-25 degrees), moderate distance
    WEDGE,    // steep parabola (~45 degrees), short, high friction on land
    DRIVER    // long flat-ish parabola (~10-15 degrees), low friction on land
}
```

**Launch angles to use in TrajectoryCalculator when wiring ShotType:**
| ShotType | launchAngle |
|----------|------------|
| PUTTER | 0.0f (uses simulatePutterShot, not parabolic) |
| IRON | 20.0f |
| WEDGE | 45.0f |
| DRIVER | 12.0f |

---

## 2. GolfBallBlockEntity.java (update, not rewrite)

**Package:** `net.astellismodding.golfwithmates.block.entity`

**What to change:**

### Replace the position list fields
```java
// REMOVE these:
private List<Vec3> targetPositions = new ArrayList<>();
private transient Vec3[] cachedPositionsArray;
private transient boolean isCacheDirty = true;

// ADD these:
private ShotResult currentShot = ShotResult.empty();
private int animationTick = 0;       // pinned — leave unused for now
private boolean animationDone = false; // pinned — leave unused for now
```

### Replace the position setters/getters
```java
// REMOVE: addTargetPosition(), setTargetPositions(), getPositionsForRendering()

// ADD:
public void setShotResult(ShotResult result) {
    this.currentShot = result;
    this.animationTick = 0;
    this.animationDone = false;
    setChangedAndUpdate();
}

public ShotResult getShotResult() {
    return this.currentShot;
}
```

### Update saveAdditional
```java
// REMOVE the TargetPositions NBT block
// ADD:
if (!currentShot.path.isEmpty()) {
    tag.put("ShotResult", currentShot.toNbt());
}
```

### Update loadAdditional
```java
// REMOVE the TargetPositions loading block
// ADD:
if (tag.contains("ShotResult")) {
    this.currentShot = ShotResult.fromNbt(tag.getCompound("ShotResult"));
} else {
    this.currentShot = ShotResult.empty();
}
```

### Keep everything else as-is
- `customName`, `puttCounter`, `isActive` — unchanged
- `setChangedAndUpdate()` — unchanged
- `getUpdatePacket()`, `getUpdateTag()` — unchanged
- `setPuttCounter()`, `IncrementPuttCounter()`, `setCustomName()` — unchanged
