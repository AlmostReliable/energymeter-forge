package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.util.TypeEnums.IoSetting;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
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
    private final Function<Direction, IoSetting> settingSupplier;
    private final BiConsumer<Direction, Boolean> onClick;
    private final BiConsumer<Direction, IoSetting> onSelect;

    private BlockSideButton(
        BlockSide blockSide, Direction direction, IoSettingWidget ioSettingWidget, Supplier<TransferMode> transferModeSupplier,
        Function<Direction, IoSetting> settingSupplier, BiConsumer<Direction, Boolean> onClick, BiConsumer<Direction, IoSetting> onSelect
    ) {
        super(BUTTON_SIZE, BUTTON_SIZE);
        this.blockSide = blockSide;
        this.direction = direction;
        this.ioSettingWidget = ioSettingWidget;
        this.transferModeSupplier = transferModeSupplier;
        this.settingSupplier = settingSupplier;
        this.onClick = onClick;
        this.onSelect = onSelect;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static GridLayout createAsLayout(
        int x, int y, BlockState blockState, IoSettingWidget ioSettingWidget, Supplier<TransferMode> transferModeSupplier,
        Function<Direction, IoSetting> settingSupplier, BiConsumer<Direction, Boolean> onClick, BiConsumer<Direction, IoSetting> onSelect
    ) {
        GridLayout layout = new GridLayout(x, y).spacing(1);

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
        return layout;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // button background
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // setting overlay
        IoSetting setting = settingSupplier.apply(direction);
        if (setting != IoSetting.OFF) {
            int uOffset = setting.ordinal() * BUTTON_SIZE;
            guiGraphics.blit(TEXTURE, getX(), getY(), uOffset, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (blockSide == BlockSide.FRONT || ioSettingWidget.isBound()) return;
        super.playDownSound(handler);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return !ioSettingWidget.isBound() && super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (!Screen.hasShiftDown() && transferModeSupplier.get() == TransferMode.TRANSFER) {
            ioSettingWidget.bind(getX(), getY(), this::onSettingWidgetClicked);
        } else {
            onClick.accept(direction, button == GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
    }

    private void onSettingWidgetClicked(IoSetting setting) {
        onSelect.accept(direction, setting);
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
