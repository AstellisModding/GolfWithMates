package net.astellismodding.golfwithmates.item.custom;

import net.astellismodding.golfwithmates.component.ModDataComponent;
import net.astellismodding.golfwithmates.component.PutterPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GolfClubPutterItem extends Item {

    private float powerLevel = 0f;

    public GolfClubPutterItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(ModDataComponent.put_power, new PutterPower(1)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.golfwithmates.golf_club_putter.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
