package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.client.screen.widget.DirectionButton;
import com.almostreliable.energymeter.client.screen.widget.DirectionButton.BlockSide;
import com.almostreliable.energymeter.client.screen.widget.SupplyingStringWidget;
import com.almostreliable.energymeter.client.screen.widget.TabButton;
import com.almostreliable.energymeter.data.EnergyMeterLang;
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

import java.util.function.Consumer;

public class MeterScreen extends SynchronizedContainerScreen<MeterMenu> {

    private static final ResourceLocation TEXTURE = TextUtils.getRL("textures/gui/meter_screen.png");
    private static final int TEXTURE_WIDTH = 133;
    private static final int TEXTURE_HEIGHT = 127;

    private TabType currentTab = TabType.STATS;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        initTabs();
        currentTab.init.accept(this);
    }

    private void initTabs() {
        LinearLayout tabLayout = LinearLayout.horizontal().spacing(1);

        tabLayout.addChild(new TabButton(TabType.STATS, currentTab, this::onTabButtonPressed));
        tabLayout.addChild(new TabButton(TabType.CONFIG, currentTab, this::onTabButtonPressed));
        tabLayout.addChild(new TabButton(TabType.REDSTONE, currentTab, this::onTabButtonPressed));

        tabLayout.arrangeElements();
        FrameLayout.alignInRectangle(tabLayout, leftPos, topPos - TabButton.TAB_HEIGHT + 1, TEXTURE_WIDTH, TabButton.TAB_HEIGHT, 0.1f, 0);
        tabLayout.visitWidgets(this::addRenderableWidget);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initStatsTab() {
        LinearLayout labelLayout = LinearLayout.vertical().spacing(2);

        labelLayout.addChild(new StringWidget(EnergyMeterLang.ENERGY_RATE.get().append(":"), font));
        labelLayout.addChild(new SupplyingStringWidget(() -> Component.literal(String.valueOf(menu.getEnergyRate())), font));
        labelLayout.addChild(SpacerElement.height(2));
        labelLayout.addChild(new StringWidget(EnergyMeterLang.DISPLAY_MODE.get().append(":"), font));
        labelLayout.addChild(new SupplyingStringWidget(() -> Component.literal(menu.getDisplayMode().name()), font));
        labelLayout.addChild(SpacerElement.height(2));
        labelLayout.addChild(new StringWidget(EnergyMeterLang.TRANSFER_MODE.get().append(":"), font));
        labelLayout.addChild(new SupplyingStringWidget(() -> Component.literal(menu.getTransferMode().name()), font));
        labelLayout.addChild(SpacerElement.height(2));
        labelLayout.addChild(new StringWidget(EnergyMeterLang.MEASURE_MODE.get().append(":"), font));
        labelLayout.addChild(new SupplyingStringWidget(() -> Component.literal(menu.getMeasureMode().name()), font));

        labelLayout.arrangeElements();
        FrameLayout.alignInRectangle(labelLayout, leftPos, topPos, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0.2f, 0.5f);
        labelLayout.visitWidgets(this::addRenderableOnly);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initConfigTab() {
        int x = leftPos + 20;
        int y = topPos + 10;

        addRenderableWidget(new DirectionButton(x + 30, y, BlockSide.TOP, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x, y + 30, BlockSide.LEFT, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 60, y + 30, BlockSide.RIGHT, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 30, y + 60, BlockSide.BOTTOM, menu::getBlockState, this::onDirectionButtonPressed));
        addRenderableWidget(new DirectionButton(x + 60, y + 60, BlockSide.BACK, menu::getBlockState, this::onDirectionButtonPressed));
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initRedstoneTab() {

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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

    private void onTabButtonPressed(TabType tabType) {
        currentTab = tabType;
        rebuildWidgets();
    }

    private void onDirectionButtonPressed(Direction direction, boolean reverse) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "direction_button");
        tag.putInt("direction", direction.ordinal());
        tag.putBoolean("reverse", reverse);
        tag.putBoolean("shift", hasShiftDown());
        sendAction(tag);
    }

    public enum TabType {
        STATS(MeterScreen::initStatsTab),
        CONFIG(MeterScreen::initConfigTab),
        REDSTONE(MeterScreen::initRedstoneTab);

        private final Consumer<MeterScreen> init;

        TabType(Consumer<MeterScreen> init) {
            this.init = init;
        }
    }
}
