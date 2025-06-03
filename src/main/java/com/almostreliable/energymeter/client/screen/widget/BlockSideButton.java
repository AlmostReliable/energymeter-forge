package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.component.IoConfig.IoSetting;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.data.EnergyMeterLang;
import com.almostreliable.energymeter.util.TooltipBuilder;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BlockSideButton extends PositionlessWidget {

    private static final ResourceLocation TEXTURE = EnergyMeter.getRL("textures/gui/button/io.png");
    private static final int TEXTURE_WIDTH = 51;
    private static final int TEXTURE_HEIGHT = 17;
    private static final int BUTTON_SIZE = 17;

    private final BlockSide blockSide;
    private final Direction direction;
    private final IoSettingWidget ioSettingWidget;
    private final Supplier<TransferMode> transferModeSupplier;
    private final Function<Direction, IoSettingWithPriority> settingSupplier;
    private final BiConsumer<Direction, Boolean> onClick;
    private final BiConsumer<Direction, IoSettingWithPriority> onSelect;

    private TransferMode previousTransferMode;
    private IoSettingWithPriority previousSetting;

    private BlockSideButton(
        BlockSide blockSide, Direction direction, IoSettingWidget ioSettingWidget, Supplier<TransferMode> transferModeSupplier,
        Function<Direction, IoSettingWithPriority> settingSupplier, BiConsumer<Direction, Boolean> onClick,
        BiConsumer<Direction, IoSettingWithPriority> onSelect
    ) {
        super(BUTTON_SIZE, BUTTON_SIZE);
        this.blockSide = blockSide;
        this.direction = direction;
        this.ioSettingWidget = ioSettingWidget;
        this.transferModeSupplier = transferModeSupplier;
        this.settingSupplier = settingSupplier;
        this.onClick = onClick;
        this.onSelect = onSelect;

        // set the previous fields to something different from the current value, so the cache resets
        previousTransferMode = transferModeSupplier.get().next();
        previousSetting = settingSupplier.apply(direction).next();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Collection<AbstractWidget> create(
        int x, int y, BlockState blockState, Supplier<TransferMode> transferModeSupplier,
        Function<Direction, IoSettingWithPriority> settingSupplier, BiConsumer<Direction, Boolean> onClick,
        BiConsumer<Direction, IoSettingWithPriority> onSelect
    ) {
        GridLayout layout = new GridLayout(x, y).spacing(1);
        IoSettingWidget ioSettingWidget = new IoSettingWidget();

        for (BlockSide blockSide : BlockSide.values()) {
            Direction direction = blockSide.getDirection(blockState);
            BlockSideButton button = new BlockSideButton(
                blockSide,
                direction,
                ioSettingWidget,
                transferModeSupplier,
                settingSupplier,
                onClick,
                onSelect
            );
            layout.addChild(button, blockSide.getRow(), blockSide.getColumn());
        }

        layout.arrangeElements();

        List<AbstractWidget> widgets = new ArrayList<>();
        layout.visitWidgets(widgets::add);
        widgets.add(ioSettingWidget);
        return widgets;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // button background
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // setting overlay
        IoSettingWithPriority setting = settingSupplier.apply(direction);
        if (!setting.isDisabled()) {
            int uOffset = setting.setting().ordinal() * BUTTON_SIZE;
            guiGraphics.blit(TEXTURE, getX(), getY(), uOffset, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        // output priority
        TransferMode transferMode = transferModeSupplier.get();
        if (transferMode == TransferMode.TRANSFER && ioSettingWidget.isUnbound() && setting.isOutput()) {
            int priority = setting.priority();
            guiGraphics.drawCenteredString(
                font,
                String.valueOf(priority),
                getX() + BUTTON_SIZE / 2 + 1,
                getY() + BUTTON_SIZE / 2 - font.lineHeight / 2 + 1,
                15_658_734
            );
        }

        // refresh the tooltip if needed
        if (transferMode != previousTransferMode || !setting.equals(previousSetting)) {
            previousTransferMode = transferMode;
            previousSetting = setting;
            refreshTooltip(transferMode, setting);
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
        return ioSettingWidget.isUnbound() && super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (blockSide == BlockSide.FRONT) {
            if (Screen.hasShiftDown()) {
                // TODO: reset all settings
            }
            return;
        }

        if (!Screen.hasShiftDown() && transferModeSupplier.get() == TransferMode.TRANSFER) {
            ioSettingWidget.bind(getX(), getY(), this::onSettingWidgetClicked);
        } else {
            onClick.accept(direction, button == GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
    }

    private void onSettingWidgetClicked(IoSettingWithPriority setting) {
        onSelect.accept(direction, setting);
    }

    private void refreshTooltip(TransferMode transferMode, IoSettingWithPriority ioSettingWithPriority) {
        IoSetting setting = ioSettingWithPriority.setting();
        int priority = ioSettingWithPriority.priority();

        TooltipBuilder tooltipBuilder = TooltipBuilder.create()
            .header(EnergyMeterLang.BLOCK_SIDES.get(blockSide).get())
            .blankLine()
            .keyValue(EnergyMeterLang.DIRECTION.get(), EnergyMeterLang.DIRECTIONS.get(direction).get());

        if (blockSide != BlockSide.FRONT) {
            tooltipBuilder.keyValue(EnergyMeterLang.CURRENT_SETTING.get(), EnergyMeterLang.IO_SETTINGS.get(setting).get());
        }

        if (transferModeSupplier.get() == TransferMode.TRANSFER && ioSettingWithPriority.isOutput()) {
            tooltipBuilder.keyValue(EnergyMeterLang.OUTPUT_PRIORITY.get(), Component.literal(String.valueOf(priority)));
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

    public static final class IoSettingWidget extends PositionlessWidget {

        private static final Consumer<IoSettingWithPriority> DEFAULT_ON_SELECT = setting -> {};
        private Consumer<IoSettingWithPriority> onSelect = DEFAULT_ON_SELECT;

        private IoSettingWidget() {
            super(BUTTON_SIZE * 6, BUTTON_SIZE);
            disable();
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // background
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + BUTTON_SIZE * 6 + 1, getY() + BUTTON_SIZE + 1, 0xFF00_FFA2);

            // off button
            guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            // input button background and overlay
            guiGraphics.blit(TEXTURE, getX() + BUTTON_SIZE, getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            guiGraphics.blit(
                TEXTURE,
                getX() + BUTTON_SIZE,
                getY(),
                BUTTON_SIZE,
                0,
                BUTTON_SIZE,
                BUTTON_SIZE,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );

            // output button backgrounds and overlays
            for (int i = 0; i < IoConfig.MAX_PRIORITY; i++) {
                renderOutputButton(guiGraphics, i);
            }
        }

        private void renderOutputButton(GuiGraphics guiGraphics, int index) {
            int x = getX() + BUTTON_SIZE * 2 + BUTTON_SIZE * index;

            // button background
            guiGraphics.blit(TEXTURE, x, getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            // button overlay
            guiGraphics.blit(TEXTURE, x, getY(), BUTTON_SIZE * 2, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            // button priority
            guiGraphics.drawCenteredString(
                font,
                String.valueOf(index + 1),
                x + BUTTON_SIZE / 2 + 1,
                getY() + BUTTON_SIZE / 2 - font.lineHeight / 2 + 1,
                15_658_734
            );
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            // check mouseX to see which button was clicked
            if (mouseX >= getX() && mouseX <= getX() + BUTTON_SIZE) {
                // off button
                onSelect.accept(IoSettingWithPriority.OFF);
            } else if (mouseX >= getX() + BUTTON_SIZE && mouseX <= getX() + BUTTON_SIZE * 2) {
                // input button
                onSelect.accept(IoSettingWithPriority.IN);
            } else if (mouseX >= getX() + BUTTON_SIZE * 2 && mouseX <= getX() + BUTTON_SIZE * 6) {
                // output buttons
                int priority = (int) ((mouseX - getX() - BUTTON_SIZE * 2) / BUTTON_SIZE) + 1;
                onSelect.accept(IoSettingWithPriority.priorityOutput(priority));
            }

            disable();
        }

        private void disable() {
            onSelect = DEFAULT_ON_SELECT;
            visible = false;
        }

        private void bind(int x, int y, Consumer<IoSettingWithPriority> onSelect) {
            setPosition(x, y);
            this.onSelect = onSelect;
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
