package net.astellismodding.golfwithmates.init;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GolfWithMates.MOD_ID);

    public static final Supplier<CreativeModeTab> Sonic_Items_Tab = CREATIVE_MODE_TAB.register("golfwithmates_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOLF_CLUB_PUTTER.get()))
                    .title(Component.translatable("creativetab.golfwithmates.golfwithmates_items"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.GOLF_FLAG);
                        output.accept(ModBlocks.GOLF_CUP);
                        output.accept(ModBlocks.GOLF_GREENZONE);
                        output.accept(ModBlocks.GOLF_FLAG_POLE);
                        output.accept(ModBlocks.GOLF_BALL);
                        output.accept(ModItems.GOLF_CLUB_PUTTER);
                        output.accept(ModItems.GOLF_CLUB_IRON);
                        output.accept(ModItems.GOLF_CLUB_WEDGE);
                        output.accept(ModItems.GOLF_CLUB_DRIVER);
                    })).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
