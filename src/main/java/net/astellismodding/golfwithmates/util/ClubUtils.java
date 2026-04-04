package net.astellismodding.golfwithmates.util;

import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.component.PutterPower;
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
     * Returns the speed for a given club, read from its PutterPower component.
     * Returns 0.0–1.0 (fraction of max power). Falls back to default (0) if absent.
     */
    public static double getVelocity(ItemStack item) {
        PutterPower power = item.getOrDefault(ModDataComponent.put_power, PutterPower.DEFAULT);
        return power.value();
    }

    /**
     * Returns the bounciness override for a club, if any.
     * Most clubs should return -1.0 to signal "use block default".
     * Special clubs (e.g. a rubber-tipped putter) could override.
     */
    public static double getBounciness(ItemStack item) {
        return -1.0; // -1 = use block's own bounciness from TrajectoryCalculator
    }

    /**
     * Returns the max distance scale for a club type.
     * getVelocity() returns 0.0–1.0; multiply by this so 100% power hits the
     * intended max distance on a flat grass surface.
     *
     * Putter  ≈  8 blocks   (short, precise)
     * Iron    ≈ 30 blocks   (mid-range)
     * Wedge   ≈ 15 blocks   (short, high arc)
     * Driver  ≈ 60 blocks   (long, low arc)
     */
    public static double getMaxDistance(ItemStack item) {
        return switch (getShotType(item)) {
            case PUTTER -> 6.0;
            case IRON   -> 22.0;
            case WEDGE  -> 11.0;
            case DRIVER -> 43.0;
        };
    }
}
