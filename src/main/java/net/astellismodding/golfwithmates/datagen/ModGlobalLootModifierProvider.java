package net.astellismodding.golfwithmates.datagen;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.init.ModItems;
import net.astellismodding.golfwithmates.loot.AddItemModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {

    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, GolfWithMates.MOD_ID);
    }

    @Override
    protected void start() {
        this.add("cup_of_joe_from_zombie",
                new AddItemModifier(new LootItemCondition[] {
                        new LootTableIdCondition.Builder(ResourceLocation.withDefaultNamespace("entities/zombie")).build()
                }, ModItems.CUP_OF_JOE.get()));
    }
}
