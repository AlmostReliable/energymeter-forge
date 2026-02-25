package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.component.IoConfig.IoSetting;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;
import com.almostreliable.energymeter.client.screen.widget.base.ClickedOutsideListener;
import com.almostreliable.energymeter.client.screen.widget.base.LayoutPositionedWidget;
import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.util.TexRenderer;
import com.almostreliable.energymeter.util.TooltipBuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;

import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class IoConfigButton extends LayoutPositionedWidget {

    private static final int TEXTURE_WIDTH = 51;
    private static final int TEXTURE_HEIGHT = 17;
    private static final int BUTTON_SIZE = 17;
    private static final int PRIORITY_TEXT_COLOR = 15_658_734;
    private static final TexRenderer MANAGER = TexRenderer.button("io", TEXTURE_WIDTH, TEXTURE_HEIGHT);
    private static final TexRenderer OFF = MANAGER.copy().tex(0, 0, BUTTON_SIZE);
    private static final TexRenderer INPUT = MANAGER.copy().tex(BUTTON_SIZE, 0, BUTTON_SIZE);
    private static final TexRenderer OUTPUT = MANAGER.copy().tex(BUTTON_SIZE * 2, 0, BUTTON_SIZE);

    private final BlockSide blockSide;
    private final MutableComponent directionName;
    private final Supplier<IoSettingWithPriority> settingSupplier;
    private final Consumer<IoSettingWithPriority> onSettingSelected;
    private final Runnable onSettingsReset;
    private final Supplier<TransferMode> transferModeSupplier;
    private final SettingSelectorWidget settingSelectorWidget;

    private TransferMode previousTransferMode;
    private IoSettingWithPriority previousSetting;

    private IoConfigButton(
        BlockSide blockSide, MutableComponent directionName, Supplier<IoSettingWithPriority> settingSupplier,
        Consumer<IoSettingWithPriority> onSettingSelected, Runnable onSettingsReset, Supplier<TransferMode> transferModeSupplier,
        SettingSelectorWidget settingSelectorWidget
    ) {
        super(BUTTON_SIZE, BUTTON_SIZE);
        this.blockSide = blockSide;
        this.directionName = directionName;
        this.settingSupplier = settingSupplier;
        this.onSettingSelected = onSettingSelected;
        this.onSettingsReset = onSettingsReset;
        this.transferModeSupplier = transferModeSupplier;
        this.settingSelectorWidget = settingSelectorWidget;

        // set the previous fields to something different from the current value, so the cache resets
        previousTransferMode = transferModeSupplier.get().next();
        previousSetting = settingSupplier.get().next();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Layout createGroup(
        BlockState blockState, Function<Direction, IoSettingWithPriority> settingsFactory,
        BiConsumer<@Nullable Direction, IoSettingWithPriority> onSettingSelected, Supplier<TransferMode> transferModeSupplier,
        Consumer<SettingSelectorWidget> overlayWidgetConsumer
    ) {
        GridLayout layout = new GridLayout().spacing(1);
        SettingSelectorWidget settingSelectorWidget = new SettingSelectorWidget();

        for (BlockSide blockSide : BlockSide.values()) {
            Direction direction = blockSide.getDirection(blockState);
            IoConfigButton button = new IoConfigButton(
                blockSide,
                EnergyMeterLang.DIRECTIONS.get(direction).get(),
                () -> settingsFactory.apply(direction),
                setting -> onSettingSelected.accept(direction, setting),
                () -> onSettingSelected.accept(null, IoSettingWithPriority.OFF),
                transferModeSupplier,
                settingSelectorWidget
            );
            layout.addChild(button, blockSide.getRow(), blockSide.getColumn());
        }

        overlayWidgetConsumer.accept(settingSelectorWidget);
        return layout;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var settingWithPriority = settingSupplier.get();
        var transferMode = transferModeSupplier.get();

        // setting
        TexRenderer buttonIcon;
        if (settingWithPriority.isInput()) {
            buttonIcon = INPUT;
        } else if (settingWithPriority.isOutput() && transferMode != TransferMode.CONSUME) {
            buttonIcon = OUTPUT; // don't draw the output overlay in consumer mode
        } else {
            buttonIcon = OFF;
        }
        buttonIcon.target(getX(), getY()).render(guiGraphics);

        // output priority
        if (transferMode == TransferMode.TRANSFER && settingSelectorWidget.isUnbound() && settingWithPriority.isOutput()) {
            int priority = settingWithPriority.priority();
            guiGraphics.drawCenteredString(
                font,
                String.valueOf(priority),
                getX() + BUTTON_SIZE / 2 + 1,
                getY() + BUTTON_SIZE / 2 - font.lineHeight / 2 + 1,
                PRIORITY_TEXT_COLOR
            );
        }

        // refresh the tooltip if needed
        if (transferMode != previousTransferMode || !settingWithPriority.equals(previousSetting)) {
            previousTransferMode = transferMode;
            previousSetting = settingWithPriority;
            refreshTooltip(transferMode, settingWithPriority);
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (blockSide == BlockSide.FRONT && !Screen.hasShiftDown()) return;
        super.playDownSound(handler);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return settingSelectorWidget.isUnbound() && super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (blockSide == BlockSide.FRONT) {
            if (Screen.hasShiftDown()) onSettingsReset.run();
            return;
        }

        if (!Screen.hasShiftDown() && transferModeSupplier.get() == TransferMode.TRANSFER) {
            settingSelectorWidget.bind(getX(), getY(), onSettingSelected);
        } else {
            handleButtonClick(settingSupplier.get(), button);
        }
    }

    private void handleButtonClick(IoSettingWithPriority currentSetting, int button) {
        if (Screen.hasShiftDown()) {
            onSettingSelected.accept(IoSettingWithPriority.OFF);
            return;
        }

        var reverse = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        var newSetting = reverse ? currentSetting.previous() : currentSetting.next();
        if (transferModeSupplier.get() == TransferMode.CONSUME && newSetting.isOutput()) {
            handleButtonClick(newSetting, button);
            return;
        }

        onSettingSelected.accept(newSetting);
    }

    private void refreshTooltip(TransferMode transferMode, IoSettingWithPriority ioSettingWithPriority) {
        TooltipBuilder tooltipBuilder = TooltipBuilder.create()
            .header(EnergyMeterLang.BLOCK_SIDES.get(blockSide).get())
            .blankLine()
            .keyValue(EnergyMeterLang.KEY_DIRECTION.get(), directionName);

        if (blockSide != BlockSide.FRONT) {
            IoSetting setting = ioSettingWithPriority.setting();
            if (transferMode == TransferMode.CONSUME && setting == IoSetting.OUT) {
                setting = IoSetting.OFF;
            }
            tooltipBuilder.keyValue(EnergyMeterLang.KEY_CURRENT_SETTING.get(), EnergyMeterLang.IO_SETTINGS.get(setting).get());
        }

        if (transferModeSupplier.get() == TransferMode.TRANSFER && ioSettingWithPriority.isOutput()) {
            int priority = ioSettingWithPriority.priority();
            tooltipBuilder.keyValue(EnergyMeterLang.KEY_OUTPUT_PRIORITY.get(), Component.literal(String.valueOf(priority)));
        }

        tooltipBuilder.blankLine();

        if (blockSide == BlockSide.FRONT) {
            tooltipBuilder.shiftLmbAction(EnergyMeterLang.RESET_ALL_SETTINGS.get());
        } else {
            if (transferMode == TransferMode.TRANSFER) {
                tooltipBuilder.lmbAction(EnergyMeterLang.SELECT_SETTING.get())
                    .shiftLmbAction(EnergyMeterLang.RESET_SETTING.get());
            } else {
                tooltipBuilder.lmbAction(EnergyMeterLang.CYCLE_NEXT_SETTING.get())
                    .rmbAction(EnergyMeterLang.CYCLE_PREVIOUS_SETTING.get())
                    .shiftLmbAction(EnergyMeterLang.RESET_SETTING.get());
            }
        }

        setTooltip(tooltipBuilder.build());
    }

    public static final class SettingSelectorWidget extends LayoutPositionedWidget implements ClickedOutsideListener {

        private static final int Z_OFFSET = 100;
        private static final Consumer<IoSettingWithPriority> EMPTY_LISTENER = setting -> {};
        private Consumer<IoSettingWithPriority> onSettingSelected = EMPTY_LISTENER;

        private SettingSelectorWidget() {
            super(BUTTON_SIZE * 6, BUTTON_SIZE);
            disable();
        }

        @Override
        public void onClickedOutside() {
            disable();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            {
                poseStack.translate(0, 0, Z_OFFSET);

                // outline
                guiGraphics.renderOutline(getX() - 1, getY() - 1, BUTTON_SIZE * 6 + 2, BUTTON_SIZE + 2, 0xFF00_FFA2);
                // off button
                OFF.target(getX(), getY()).render(guiGraphics);
                // input button
                INPUT.target(getX() + BUTTON_SIZE, getY()).render(guiGraphics);
                // output buttons and priorities
                for (int prioIndex = 0; prioIndex < IoConfig.MAX_PRIORITY; prioIndex++) {
                    int x = getX() + BUTTON_SIZE * 2 + BUTTON_SIZE * prioIndex;
                    OUTPUT.target(x, getY()).render(guiGraphics);
                    guiGraphics.drawCenteredString(
                        font,
                        String.valueOf(prioIndex + 1),
                        x + BUTTON_SIZE / 2 + 1,
                        getY() + BUTTON_SIZE / 2 - font.lineHeight / 2 + 1,
                        PRIORITY_TEXT_COLOR
                    );
                }
            }
            poseStack.popPose();
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            // check mouseX to see which button was clicked
            if (mouseX >= getX() && mouseX <= getX() + BUTTON_SIZE) {
                // off button
                onSettingSelected.accept(IoSettingWithPriority.OFF);
            } else if (mouseX >= getX() + BUTTON_SIZE && mouseX <= getX() + BUTTON_SIZE * 2) {
                // input button
                onSettingSelected.accept(IoSettingWithPriority.IN);
            } else if (mouseX >= getX() + BUTTON_SIZE * 2 && mouseX <= getX() + BUTTON_SIZE * 6) {
                // output buttons
                int priority = (int) ((mouseX - getX() - BUTTON_SIZE * 2) / BUTTON_SIZE) + 1;
                onSettingSelected.accept(IoSettingWithPriority.priorityOutput(priority));
            }
            disable();
        }

        private void disable() {
            onSettingSelected = EMPTY_LISTENER;
            visible = false;
        }

        private void bind(int x, int y, Consumer<IoSettingWithPriority> onSettingSelected) {
            setPosition(x, y);
            this.onSettingSelected = onSettingSelected;
            visible = true;
        }

        private boolean isUnbound() {
            return !visible;
        }
    }

    public enum BlockSide {

        BOTTOM(FacingEntityBlock::getBottomDir, 3, 2),
        TOP(d -> FacingEntityBlock.getBottomDir(d).getOpposite(), 1, 2),
        FRONT(FacingEntityBlock::getFacingDir, 2, 2),
        BACK(d -> FacingEntityBlock.getFacingDir(d).getOpposite(), 3, 3),
        LEFT(FacingEntityBlock::getLeftDir, 2, 1),
        RIGHT(d -> FacingEntityBlock.getLeftDir(d).getOpposite(), 2, 3);

        private final Function<BlockState, Direction> directionFactory;
        private final int row;
        private final int column;

        BlockSide(Function<BlockState, Direction> directionFactory, int row, int column) {
            this.directionFactory = directionFactory;
            this.row = row;
            this.column = column;
        }

        private Direction getDirection(BlockState state) {
            return directionFactory.apply(state);
        }

        private int getRow() {
            return row;
        }

        private int getColumn() {
            return column;
        }
    }
}
