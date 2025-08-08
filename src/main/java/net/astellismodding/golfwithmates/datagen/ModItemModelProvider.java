package net.astellismodding.golfwithmates.datagen;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.init.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, GolfWithMates.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.POLES_BAG.get());
        basicItem(ModItems.CUP_OF_JOE.get());

    }
}
