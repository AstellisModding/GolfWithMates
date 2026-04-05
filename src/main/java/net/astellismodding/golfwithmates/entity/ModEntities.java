package net.astellismodding.golfwithmates.entity;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.entity.custom.AppaEntity;
import net.astellismodding.golfwithmates.entity.custom.GolfBallEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
          DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, GolfWithMates.MOD_ID);

    public static final Supplier<EntityType<AppaEntity>> APPA =
            ENTITY_TYPES.register("appa", () -> EntityType.Builder.of(AppaEntity::new, MobCategory.CREATURE)
                    .sized(1.0f,0.35f).build("appa"));

    public static final Supplier<EntityType<GolfBallEntity>> GOLF_BALL_ENTITY =
            ENTITY_TYPES.register("golf_ball_entity", () -> EntityType.Builder
                    .<GolfBallEntity>of(GolfBallEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .build("golf_ball_entity"));



    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);

    }


}
