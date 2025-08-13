package net.astellismodding.golfwithmates.init;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.block.entity.GolfCupBlockEntity;
import net.astellismodding.golfwithmates.block.entity.NameplateBlockEntity;
import net.astellismodding.golfwithmates.block.entity.renderer.NameplateBlockEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, GolfWithMates.MOD_ID);

    public static final Supplier<BlockEntityType<GolfCupBlockEntity>> GOLF_CLUP_BE =
            BLOCK_ENTITIES.register("golf_cup_be", () -> BlockEntityType.Builder.of(
                    GolfCupBlockEntity::new, ModBlocks.GOLF_CUP.get()).build(null));

    public static final Supplier<BlockEntityType<NameplateBlockEntity>> Nameplate_Block_BE =
            BLOCK_ENTITIES.register("nameplate_block", () -> BlockEntityType.Builder.of(
                    NameplateBlockEntity::new, ModBlocks.Nameplate_Block.get()).build(null));

    public static void registerHandler(IEventBus e){
        BLOCK_ENTITIES.register(e);
    }
}
