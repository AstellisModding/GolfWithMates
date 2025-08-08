package net.astellismodding.golfwithmates.datagen;

import net.astellismodding.golfwithmates.init.ModBlocks;
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ASTELLIS_BLOCK.get())
                .pattern("   ")
                .pattern("B B")
                .pattern(" B ")
                .define('B', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER)).save(recipeOutput);

    }
}
