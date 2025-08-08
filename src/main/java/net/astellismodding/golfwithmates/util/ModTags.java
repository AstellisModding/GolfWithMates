package net.astellismodding.golfwithmates.util;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(GolfWithMates.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> GOLF_CLUBS = createTag("golf_clubs");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(GolfWithMates.MOD_ID, name));
        }
    }
}