package net.astellismodding.golfwithmates.common;

import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.init.ModBlockEntities;
import net.astellismodding.golfwithmates.init.ModBlocks;
import net.astellismodding.golfwithmates.init.ModItems;
import net.astellismodding.golfwithmates.init.ModPayloads;
import net.neoforged.bus.api.IEventBus;

public class CommonEventHandler {
    public void registerHandler(IEventBus modBus){
        ModItems.registerHandlers(modBus);
        ModBlocks.registerHandlers(modBus);
        ModBlockEntities.registerHandler(modBus);
        ModDataComponent.registerHandler(modBus);
        modBus.addListener(ModPayloads::registerPayloads);

    }
}
