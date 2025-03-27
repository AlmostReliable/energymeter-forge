package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.block.FacingEntityBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DirectionButton extends AbstractButton {

    private final BlockSide blockSide;
    private final Supplier<BlockState> blockStateSupplier;
    private final BiConsumer<Direction, Boolean> onPress;

    public DirectionButton(
        int x, int y, BlockSide blockSide, Supplier<BlockState> blockStateSupplier, BiConsumer<Direction, Boolean> onPress
    ) {
        super(x, y, 30, 30, Component.empty());
        this.blockSide = blockSide;
        this.blockStateSupplier = blockStateSupplier;
        this.onPress = onPress;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        Direction directionFromBlockSide = blockSide.getDirection(blockStateSupplier.get());
        onPress.accept(directionFromBlockSide, button == GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    @Override
    public void onPress() {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    /**
     * Yoinked from NeoForge's {@link ExtendedButton}.
     */
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), width, height);

        Font font = Minecraft.getInstance().font;
        FormattedText buttonText = font.ellipsize(getMessage(), width - 6);
        guiGraphics.drawCenteredString(
            font,
            Language.getInstance().getVisualOrder(buttonText),
            getX() + width / 2,
            getY() + (height - 8) / 2,
            getFGColor()
        );
    }

    @Override
    public Component getMessage() {
        return Component.literal(String.valueOf(blockSide.name().charAt(0)));
    }

    public enum BlockSide {

        BOTTOM(FacingEntityBlock::getBottomDir),
        TOP(d -> FacingEntityBlock.getBottomDir(d).getOpposite()),
        FRONT(FacingEntityBlock::getFacingDir),
        BACK(d -> FacingEntityBlock.getFacingDir(d).getOpposite()),
        LEFT(FacingEntityBlock::getLeftDir),
        RIGHT(d -> FacingEntityBlock.getLeftDir(d).getOpposite());

        private final Function<BlockState, Direction> directionFactory;

        BlockSide(Function<BlockState, Direction> directionFactory) {
            this.directionFactory = directionFactory;
        }

        private Direction getDirection(BlockState state) {
            return directionFactory.apply(state);
        }
    }
}
