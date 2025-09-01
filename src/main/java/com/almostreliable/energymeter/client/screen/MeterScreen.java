package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.client.screen.layout.HeaderValueLayoutElement;
import com.almostreliable.energymeter.client.screen.layout.InputLayoutElement;
import com.almostreliable.energymeter.client.screen.widget.BlockSideButton;
import com.almostreliable.energymeter.client.screen.widget.TabButton;
import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.NumberFormatter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Consumer;

public class MeterScreen extends SynchronizedContainerScreen<MeterMenu> {

    private static final ResourceLocation TEXTURE = EnergyMeter.getRL("textures/gui/meter_screen.png");
    private static final int TEXTURE_WIDTH = 133;
    private static final int TEXTURE_HEIGHT = 127;

    private TabType currentTab = TabType.STATISTICS;

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
        LinearLayout layout = LinearLayout.horizontal().spacing(1);

        for (TabType tabType : TabType.values()) {
            layout.addChild(new TabButton(tabType, currentTab, this::onTabButtonClicked));
        }

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, leftPos, topPos - TabButton.TAB_HEIGHT + 1, TEXTURE_WIDTH, TabButton.TAB_HEIGHT, 0.5f, 0);
        layout.visitWidgets(this::addRenderableWidget);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initStatsTab() {
        LinearLayout layout = LinearLayout.vertical().spacing(4);

        layout.addChild(new HeaderValueLayoutElement(
            EnergyMeterLang.ENERGY_RATE.get(),
            () -> NumberFormatter.formatEnergy(menu.getEnergyRate()).asUnitPerTick(),
            font
        ));
        layout.addChild(new HeaderValueLayoutElement(
            EnergyMeterLang.TOTAL_ENERGY.get(),
            () -> NumberFormatter.formatEnergy(menu.getTotalEnergy()).asTotalUnit(),
            font
        ));
        layout.addChild(new HeaderValueLayoutElement(
            EnergyMeterLang.CONNECTION_STATUS.get(),
            () -> menu.getConnectionStatus().name(),
            font
        ));

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, leftPos, topPos, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0.3f, 0.5f);
        layout.visitWidgets(this::addRenderableOnly);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initConfigTab() {
        LinearLayout layout = LinearLayout.vertical().spacing(4);

        layout.addChild(new HeaderValueLayoutElement(
            EnergyMeterLang.TRANSFER_MODE.get(),
            () -> menu.getTransferMode().name(),
            font
        ));
        layout.addChild(new HeaderValueLayoutElement(
            EnergyMeterLang.MEASURE_MODE.get(),
            () -> menu.getMeasureMode().name(),
            font
        ));

        layout.addChild(Button.builder(Component.literal("Transfer Mode"), this::onTransferModeButtonClicked)
            .pos(leftPos + 20, topPos + 120)
            .width(100)
            .build());

        layout.addChild(new InputLayoutElement(EnergyMeterLang.INTERVAL.get().append(":"), font)
            .set(String.valueOf(menu.getMeasureInterval())));
        layout.addChild(new InputLayoutElement(EnergyMeterLang.ZERO_TOLERANCE.get().append(":"), font)
            .set(String.valueOf(menu.getZeroTolerance())));
        layout.addChild(new InputLayoutElement(EnergyMeterLang.TRANSFER_LIMIT.get().append(":"), font)
            .set(String.valueOf(menu.getTransferLimit())));

        layout.arrangeElements();
        FrameLayout.alignInRectangle(layout, leftPos, topPos, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0.3f, 0.5f);
        layout.visitWidgets(renderable -> {
            if (renderable instanceof AbstractButton) {
                addRenderableWidget(renderable);
                return;
            }
            addRenderableOnly(renderable);
        });
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initIoTab() {
        BlockSideButton.create(
            leftPos + 20,
            topPos + 10,
            menu.getBlockState(),
            menu::getTransferMode,
            menu::getIoSetting,
            this::onBlockSideButtonClicked,
            this::onIoSettingSelected
        ).forEach(this::addRenderableWidget);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initRedstoneTab() {

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // don't render title and inventory labels
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void onTabButtonClicked(TabType tabType) {
        currentTab = tabType;
        rebuildWidgets();
    }

    private void onBlockSideButtonClicked(Direction direction, boolean reverse) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "io_setting");
        tag.putInt("direction", direction.ordinal());
        tag.putBoolean("reverse", reverse);
        tag.putBoolean("shift", hasShiftDown());
        sendAction(tag);
    }

    private void onIoSettingSelected(Direction direction, IoSettingWithPriority setting) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "io_setting");
        tag.putInt("direction", direction.ordinal());
        tag.put("setting", setting.serialize());
        sendAction(tag);
    }

    private void onTransferModeButtonClicked(Button ignoredButton) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "setting_changed");
        tag.putString("setting", "transfer_mode");
        sendAction(tag);
    }

    public enum TabType {
        STATISTICS(MeterScreen::initStatsTab),
        CONFIGURATION(MeterScreen::initConfigTab),
        IO(MeterScreen::initIoTab),
        REDSTONE(MeterScreen::initRedstoneTab);

        private final Consumer<MeterScreen> init;

        TabType(Consumer<MeterScreen> init) {
            this.init = init;
        }
    }
}
