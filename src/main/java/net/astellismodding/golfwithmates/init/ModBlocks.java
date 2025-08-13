package net.astellismodding.golfwithmates.init;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.block.custom.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GolfWithMates.MOD_ID);

    //BASIC BLOCKS REGISTERED HERE
    public static final DeferredBlock<Block> ASTELLIS_BLOCK = registerBlock("astellis_block",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of().strength(3f).sound(SoundType.AMETHYST)));
    public static final DeferredBlock<Block> GOLF_GREENZONE = registerBlock("golf_greenzone",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of().strength(3f).sound(SoundType.GRASS)));

    //CUSTOM BLOCKS REGISTERED HERE
    public static final DeferredBlock<Block> GOLF_FLAG = registerBlock("golf_flag",
            () -> new GolfFlagBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> GOLF_FLAG_POLE = registerBlock("golf_flag_pole",
            () -> new GolfFlagPoleBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> GOLF_HOLE = registerBlock("golf_hole",
            () -> new GolfHoleBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<FallingBlock> GOLF_BALL = registerBlock("golf_ball",
            () -> new GolfBallBlock(BlockBehaviour.Properties.of().noCollission()));

    public static final DeferredBlock<Block> GOLF_CUP = registerBlock("golf_cup",
            () -> new GolfCupBlock(BlockBehaviour.Properties.of().noOcclusion()));

    public static final DeferredBlock<Block> Nameplate_Block = registerBlock("nameplate_block",
            () -> new NameplateBlock(BlockBehaviour.Properties.of().noOcclusion()));



    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }


    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void registerHandlers(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }


}
