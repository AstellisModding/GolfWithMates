package net.astellismodding.golfwithmates.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ClubUtils {
    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

    //todo Feat: Rebound - notes in this file
    //this is basically a cast
    //we can have a stamina value, this is how many blocks it needs to travel yet
    //each block it travels it will check what block its on/in, turf is 0 friction, scruff is 1 friction, friction will devide distance by 3
    //we can transfer the stamina value on rebound
    //we can calculate new vector on rebound and then run this function
    public static Vec3 CalculateHitResultLocation(float x, float z, float r, double v, int driveType) {
        double power = 16 * v * driveType;
        float rot = r;

        double angleRadians = Math.toRadians(rot);
        double deltaX = power * Math.cos(angleRadians);
        double deltaZ = power * Math.sin(angleRadians);
        double newX = x - Math.floor(deltaX);
        double newZ = z - Math.floor(deltaZ);

        return new Vec3(Math.round(newX),0f, Math.round(newZ));
    }
}
