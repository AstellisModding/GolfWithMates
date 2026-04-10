package net.astellismodding.golfwithmates.datagen;

import net.astellismodding.golfwithmates.init.ModBlocks;
import net.astellismodding.golfwithmates.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLF_CLUB_PUTTER.get())
                .pattern("  A")
                .pattern(" A ")
                .pattern("B  ")
                .define('A', Items.STICK)
                .define('B', Items.IRON_NUGGET)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLF_CLUB_IRON.get())
                .pattern("  A")
                .pattern(" A ")
                .pattern("BB ")
                .define('A', Items.STICK)
                .define('B', Items.IRON_NUGGET)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLF_CLUB_WEDGE.get())
                .pattern("  A")
                .pattern("BA ")
                .pattern("B  ")
                .define('A', Items.STICK)
                .define('B', Items.IRON_NUGGET)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLF_CLUB_DRIVER.get())
                .pattern("  A")
                .pattern(" A ")
                .pattern("B  ")
                .define('A', Items.STICK)
                .define('B', Items.IRON_INGOT)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GOLF_BALL.get())
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.CLAY_BALL)
                .define('B', Items.BONE_MEAL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .unlockedBy("has_bone_meal", has(Items.BONE_MEAL))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GOLF_CUP.get())
                .pattern("ABA")
                .pattern("A A")
                .pattern("   ")
                .define('A', Items.STICK)
                .define('B', Items.FLOWER_POT)
                .unlockedBy("has_stick", has(Items.STICK))
                .unlockedBy("has_flower_pot", has(Items.FLOWER_POT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GOLF_FLAG.get())
                .pattern("   ")
                .pattern(" AB")
                .pattern(" A ")
                .define('A', Items.IRON_INGOT)
                .define('B', Items.PAPER)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GOLF_FLAG_POLE.get())
                .pattern("   ")
                .pattern(" A ")
                .pattern(" A ")
                .define('A', Items.IRON_INGOT)
                .unlockedBy("has_stick", has(Items.IRON_INGOT))
                .save(recipeOutput);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GOLF_GREENZONE.get())
                .pattern("   ")
                .pattern(" A ")
                .pattern("   ")
                .define('A', Items.GRASS_BLOCK)
                .unlockedBy("has_grass_block", has(Items.GRASS_BLOCK))
                .save(recipeOutput);






    }
}
