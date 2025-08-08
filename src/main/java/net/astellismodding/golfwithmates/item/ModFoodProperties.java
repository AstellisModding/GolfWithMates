package net.astellismodding.golfwithmates.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties CUP_OF_JOE = new FoodProperties.Builder().nutrition(1).saturationModifier(0.5f)
            .alwaysEdible().fast().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000),1.0f).build();
}
