package net.astellismodding.golfwithmates.util;

import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.component.PutterPower;
import net.minecraft.world.item.ItemStack;

public class ClubUtils {

    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

    /**
     * Returns the ShotType for a given club item based on its item tag.
     * Falls back to PUTTER for unknown clubs.
     */
    public static ShotType getShotType(ItemStack item) {
        if (item.is(ModTags.Items.GOLF_CLUBS_DRIVER)) return ShotType.DRIVER;
        if (item.is(ModTags.Items.GOLF_CLUBS_WEDGE))  return ShotType.WEDGE;
        if (item.is(ModTags.Items.GOLF_CLUBS_IRON))   return ShotType.IRON;
        return ShotType.PUTTER;
    }

    /**
     * Returns the effective vertical launch angle in degrees, scaled by raw power.
     * At 0% power the angle is at its per-club floor; at 100% it is the full loft.
     * This prevents low-power shots from launching as steeply as a full swing.
     *
     * @param type     Club shot type.
     * @param rawPower 0.0–1.0 raw power before the curve is applied.
     * @return Effective launch angle in degrees.
     */
    public static float getLaunchAngle(ShotType type, double rawPower) {
        float max = switch (type) {
            case DRIVER -> 12.0f;
            case IRON   -> 20.0f;
            case WEDGE  -> 45.0f;
            case PUTTER ->  0.0f;
        };
        float min = switch (type) {
            case DRIVER -> 12.0f;
            case IRON   -> 20.0f;
            case WEDGE  -> 45.0f;
            case PUTTER ->  0.0f;
        };
        return min + (max - min) * (float) rawPower;
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
     * Applies a power curve to raw power input (0.0–1.0) based on club type.
     *
     * Putter: linear — precise 1:1 control.
     * All others: logarithmic — punchy at low power, diminishing returns near max.
     * Uses ln(1 + power*(e−1)) which maps 0→0 and 1→1, so max distance is unchanged.
     *
     * @param rawPower 0.0–1.0 from the PutterPower component.
     * @param type     Club shot type.
     * @return Curved power, still in 0.0–1.0 range.
     */
    public static double applyPowerCurve(double rawPower, ShotType type) {
        if (type == ShotType.PUTTER) return rawPower;
        return Math.log1p(rawPower * (Math.E - 1.0));
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
            case IRON   -> 8.0;
            case WEDGE  -> 4.0;
            case DRIVER -> 12.0;
        };
    }
}
