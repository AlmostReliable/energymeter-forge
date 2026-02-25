package com.almostreliable.energymeter.client.screen;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.MeasureMode;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;
import com.almostreliable.energymeter.client.screen.layout.InputLayoutElement;
import com.almostreliable.energymeter.client.screen.widget.DynamicMarqueeStringWidget;
import com.almostreliable.energymeter.client.screen.widget.IoConfigButton;
import com.almostreliable.energymeter.client.screen.widget.RadioButton;
import com.almostreliable.energymeter.client.screen.widget.TabButton;
import com.almostreliable.energymeter.client.screen.widget.base.ClickedOutsideListener;
import com.almostreliable.energymeter.client.screen.widget.base.OutlinedCompositeWidget;
import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.util.NumberFormatter;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MeterScreen extends SynchronizedContainerScreen<MeterMenu> {

    private static final ResourceLocation TEXTURE = EnergyMeter.getRL("textures/gui/meter.png");
    private static final int GUI_WIDTH = 234;
    private static final int GUI_HEIGHT = 185;
    private static final int PANE_SPACING = 4;
    private static final int LEFT_PANE_WIDTH = 90;
    private static final int RIGHT_PANE_WIDTH = GUI_WIDTH - LEFT_PANE_WIDTH - PANE_SPACING;
    private static final int VERTICAL_ELEMENT_SPACING = 4;
    private static final int GLOBAL_INFO_WIDTH = 70;

    private final List<ClickedOutsideListener> clickedOutsideListeners = new ArrayList<>();

    private TabType currentTab = TabType.CONFIGURATION;
    private boolean intervalSettingsEnabled = true;
    @Nullable
    private InputLayoutElement intervalSettingsWidget;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        initGlobalInfo();
        initTabs();
        currentTab.init.accept(this);
    }

    private void initGlobalInfo() {
        var energyRateComposite = OutlinedCompositeWidget.ofElement(
            EnergyMeterLang.ENERGY_RATE.get(),
            new DynamicMarqueeStringWidget(GLOBAL_INFO_WIDTH, () -> NumberFormatter.formatEnergy(menu.getEnergyRate()).asUnitPerTick())
        );

        var totalEnergyComposite = OutlinedCompositeWidget.ofElement(
            EnergyMeterLang.TOTAL_ENERGY.get(),
            new DynamicMarqueeStringWidget(GLOBAL_INFO_WIDTH, () -> NumberFormatter.formatEnergy(menu.getTotalEnergy()).asTotalUnit())
        );

        var statusComposite = OutlinedCompositeWidget.ofElement(
            EnergyMeterLang.CONNECTION_STATUS.get(),
            new DynamicMarqueeStringWidget(
                GLOBAL_INFO_WIDTH,
                () -> EnergyMeterLang.CONNECTION_STATUSES.get(menu.getConnectionStatus()).get()
            )
        );

        var ioConfigLayout = IoConfigButton.createGroup(
            menu.getBlockState(),
            menu::getIoSetting,
            this::onIoSettingSelected,
            menu::getTransferMode,
            this::addRenderableWidget
        );
        var ioConfigComposite = OutlinedCompositeWidget.ofLayout(EnergyMeterLang.IO_SETTING.get(), ioConfigLayout);
        ioConfigComposite.setMinWidth(GLOBAL_INFO_WIDTH);

        var layout = LinearLayout.vertical().spacing(VERTICAL_ELEMENT_SPACING);
        layout.addChild(energyRateComposite);
        layout.addChild(totalEnergyComposite);
        layout.addChild(statusComposite);
        layout.addChild(ioConfigComposite);

        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, leftPos, topPos, LEFT_PANE_WIDTH, GUI_HEIGHT);

        layout.visitWidgets(this::addRenderableWidget);
    }

    private void initTabs() {
        LinearLayout layout = LinearLayout.horizontal().spacing(1);

        for (TabType tabType : TabType.values()) {
            layout.addChild(new TabButton(tabType, currentTab, this::onTabButtonClicked));
        }

        layout.arrangeElements();
        FrameLayout.alignInRectangle(
            layout,
            leftPos + LEFT_PANE_WIDTH + PANE_SPACING,
            topPos - TabButton.TAB_HEIGHT + 1,
            RIGHT_PANE_WIDTH,
            TabButton.TAB_HEIGHT,
            0.1f,
            0
        );

        layout.visitWidgets(this::addRenderableWidget);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initConfigTab() {
        var transferModeRadioButtons = RadioButton.createGroup(
            90,
            EnergyMeterLang.TRANSFER_MODES,
            menu::getTransferMode,
            this::onTransferModeSelected
        );
        var transferModeComposite = OutlinedCompositeWidget.ofLayout(EnergyMeterLang.TRANSFER_MODE.get(), transferModeRadioButtons);

        var measureModeRadioButtons = RadioButton.createGroup(
            90,
            EnergyMeterLang.MEASURE_MODES,
            menu::getMeasureMode,
            this::onMeasureModeSelected
        );
        var measureModeComposite = OutlinedCompositeWidget.ofLayout(EnergyMeterLang.MEASURE_MODE.get(), measureModeRadioButtons);

        var modesLayout = LinearLayout.vertical().spacing(VERTICAL_ELEMENT_SPACING);
        modesLayout.addChild(transferModeComposite);
        modesLayout.addChild(measureModeComposite);
        var modesComposite = OutlinedCompositeWidget.ofLayout(EnergyMeterLang.MODES.get(), modesLayout, 0xFFBF_BFBF);

        var settingsLayout = LinearLayout.vertical().spacing(1);
        settingsLayout.addChild(new InputLayoutElement(
            InputLayoutElement.TextBoxType.TRANSFER_LIMIT,
            110,
            font,
            EnergyMeterLang.TRANSFER_LIMIT.get().append(":"),
            () -> String.valueOf(menu.getTransferLimit()),
            this::onTextValueUpdated
        ));
        settingsLayout.addChild(new InputLayoutElement(
            InputLayoutElement.TextBoxType.ZERO_TOLERANCE,
            110,
            font,
            EnergyMeterLang.ZERO_TOLERANCE.get().append(":"),
            () -> String.valueOf(menu.getZeroTolerance()),
            this::onTextValueUpdated
        ));
        intervalSettingsWidget = new InputLayoutElement(
            InputLayoutElement.TextBoxType.MEASURE_INTERVAL,
            110,
            font,
            EnergyMeterLang.INTERVAL.get().append(":"),
            () -> String.valueOf(menu.getMeasureInterval()),
            this::onTextValueUpdated
        );
        settingsLayout.addChild(intervalSettingsWidget);
        var settingsComposite = OutlinedCompositeWidget.ofLayout(EnergyMeterLang.SETTINGS.get(), settingsLayout);

        var layout = LinearLayout.vertical().spacing(VERTICAL_ELEMENT_SPACING);
        layout.addChild(modesComposite);
        layout.addChild(settingsComposite);

        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, leftPos + LEFT_PANE_WIDTH + PANE_SPACING, topPos, RIGHT_PANE_WIDTH, GUI_HEIGHT);

        layout.visitWidgets(this::addRenderableWidget);
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void initRedstoneTab() {
        var layout = LinearLayout.vertical().spacing(VERTICAL_ELEMENT_SPACING);
        layout.addChild(new StringWidget(Component.literal("Work in progress!"), font));

        layout.arrangeElements();
        FrameLayout.centerInRectangle(layout, leftPos + LEFT_PANE_WIDTH + PANE_SPACING, topPos, RIGHT_PANE_WIDTH, GUI_HEIGHT);

        layout.visitWidgets(this::addRenderableOnly);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // don't render title and inventory labels
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (intervalSettingsWidget != null) {
            var enabled = menu.getMeasureMode() == MeasureMode.INTERVAL;
            if (enabled != intervalSettingsEnabled) {
                intervalSettingsWidget.setEnabled(enabled);
                intervalSettingsEnabled = enabled;
            }
        }

        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        if (listener instanceof OutlinedCompositeWidget composite) {
            for (var child : composite.children()) {
                if (child instanceof ClickedOutsideListener clickedOutsideListener) {
                    clickedOutsideListeners.add(clickedOutsideListener);
                }
            }
        }
        if (listener instanceof ClickedOutsideListener clickedOutsideListener) {
            clickedOutsideListeners.add(clickedOutsideListener);
        }
        return super.addWidget(listener);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        clickedOutsideListeners.clear();
        intervalSettingsWidget = null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var clickedOutsideListener : clickedOutsideListeners) {
            clickedOutsideListener.receiveClickOutside(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onTabButtonClicked(TabType tabType) {
        currentTab = tabType;
        rebuildWidgets();
    }

    private void onIoSettingSelected(@Nullable Direction direction, IoSettingWithPriority setting) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "io_setting");
        if (direction == null) {
            tag.putString("reset", "");
        } else {
            tag.putInt("direction", direction.ordinal());
            tag.put("setting", setting.serialize());
        }
        sendAction(tag);
    }

    private void onTransferModeSelected(TransferMode mode) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "transfer_mode");
        tag.putInt("value", mode.ordinal());
        sendAction(tag);
    }

    private void onMeasureModeSelected(MeasureMode mode) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "measure_mode");
        tag.putInt("value", mode.ordinal());
        sendAction(tag);
    }

    private void onTextValueUpdated(InputLayoutElement.TextBoxType textBox, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", "text_value");
        tag.putInt("text_box", textBox.ordinal());
        tag.putInt("value", value);
        sendAction(tag);
    }

    public enum TabType {
        CONFIGURATION(MeterScreen::initConfigTab),
        REDSTONE(MeterScreen::initRedstoneTab);

        private final Consumer<MeterScreen> init;

        TabType(Consumer<MeterScreen> init) {
            this.init = init;
        }
    }
}
