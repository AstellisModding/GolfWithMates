package net.astellismodding.golfwithmates.util;

import net.minecraft.world.item.ItemStack;

public class ClubUtils {
    public static boolean isClub(ItemStack item) {
        return item.is(ModTags.Items.GOLF_CLUBS);
    }

}
