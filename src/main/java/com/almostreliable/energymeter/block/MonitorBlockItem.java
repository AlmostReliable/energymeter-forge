package com.almostreliable.energymeter.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class MonitorBlockItem extends BlockItem {

    public MonitorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(
        ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        builder.accept(Component.literal("Work in progress!").withStyle(ChatFormatting.DARK_RED));
    }
}
