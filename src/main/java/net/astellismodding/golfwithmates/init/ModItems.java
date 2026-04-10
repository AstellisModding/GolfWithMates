package net.astellismodding.golfwithmates.init;

import net.astellismodding.golfwithmates.GolfWithMates;
import net.astellismodding.golfwithmates.component.PutterPower;
import net.astellismodding.golfwithmates.item.ModItemDataComponents;
import net.astellismodding.golfwithmates.item.custom.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GolfWithMates.MOD_ID);

    public static final DeferredItem<Item> GOLF_CLUB_PUTTER = ITEMS.register("golf_club_putter",
            () -> new GolfClubPutterItem(new Item.Properties().component(ModItemDataComponents.PUTTER_POWER.get(), PutterPower.DEFAULT)));

    public static final DeferredItem<Item> GOLF_CLUB_IRON = ITEMS.register("golf_club_iron",
            () -> new GolfClubIronItem(new Item.Properties().component(ModItemDataComponents.PUTTER_POWER.get(), PutterPower.DEFAULT)));

    public static final DeferredItem<Item> GOLF_CLUB_WEDGE = ITEMS.register("golf_club_wedge",
            () -> new GolfClubWedgeItem(new Item.Properties().component(ModItemDataComponents.PUTTER_POWER.get(), PutterPower.DEFAULT)));

    public static final DeferredItem<Item> GOLF_CLUB_DRIVER = ITEMS.register("golf_club_driver",
            () -> new GolfClubDriverItem(new Item.Properties().component(ModItemDataComponents.PUTTER_POWER.get(), PutterPower.DEFAULT)));


    public static void registerHandlers(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
