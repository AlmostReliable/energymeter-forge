package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.client.screen.widget.DirectionButton;
import com.almostreliable.energymeter.client.screen.widget.DirectionButton.BlockSide;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.TextUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MeterScreen extends SynchronizedContainerScreen<MeterMenu> {

    private static final ResourceLocation TEXTURE = TextUtils.getRL("textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;

    public MeterScreen(MeterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int x = leftPos + 140;
        int y = topPos + 75;

        addRenderableWidget(new DirectionButton(x + 30, y, BlockSide.TOP, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x, y + 30, BlockSide.LEFT, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 60, y + 30, BlockSide.RIGHT, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 30, y + 60, BlockSide.BOTTOM, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 60, y + 60, BlockSide.BACK, menu::getBlockState, this::onDirectionButtonPressed));

        var layout = LinearLayout.vertical().spacing(2);

        layout.addChild(new StringWidget(Component.literal("Energy Rate:"), font));
        layout.addChild(new StringWidget(Component.literal(String.valueOf(menu.getEnergyRate())), font));
        layout.addChild(SpacerElement.height(2));
        layout.addChild(new StringWidget(Component.literal("Display Mode:"), font));
        layout.addChild(new StringWidget(Component.literal(menu.getDisplayMode().name()), font));
        layout.addChild(SpacerElement.height(2));
        layout.addChild(new StringWidget(Component.literal("Transfer Mode:"), font));
        layout.addChild(new StringWidget(Component.literal(menu.getTransferMode().name()), font));
        layout.addChild(SpacerElement.height(2));
        layout.addChild(new StringWidget(Component.literal("Measure Mode:"), font));
        layout.addChild(new StringWidget(Component.literal(menu.getMeasureMode().name()), font));

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, leftPos, topPos, TEXTURE_WIDTH - 64, TEXTURE_HEIGHT, 0.2f, 0.5f);
        layout.visitWidgets(this::addRenderableOnly);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        int y = 10;

        for (Direction direction : Direction.values()) {
            String text = direction.getName() + ": " + menu.getIoConfig().getSetting(direction).name();
            guiGraphics.drawString(font, text, -80, y, 15_658_734);
            y += 20;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void onDirectionButtonPressed(Direction direction, boolean reverse) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "direction_button");
        tag.putInt("direction", direction.ordinal());
        tag.putBoolean("reverse", reverse);
        tag.putBoolean("shift", hasShiftDown());
        sendAction(tag);
    }
}
