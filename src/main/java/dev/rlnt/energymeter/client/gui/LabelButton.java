package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.rlnt.energymeter.util.Tooltip;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

public abstract class LabelButton extends AbstractButton {

    private final String label;
    private final FontRenderer font;

    LabelButton(MeterScreen screen, int pX, int pY, int width, int height, String label) {
        super(screen, pX, pY, width, height);
        this.label = label;
        font = Minecraft.getInstance().font;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
        super.renderButton(matrix, mX, mY, partial);
        // label
        int pX = (width - font.width(label)) / 2 + x + 1;
        int pY = (height - font.lineHeight) / 2 + y + 1;
        font.draw(matrix, label, pX, pY, TextFormatting.WHITE.getColor());
        // tooltips
        if (isHovered) renderToolTip(matrix, mX, mY);
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        if (getTooltip() != null) screen.renderComponentTooltip(matrix, getTooltip().get(), mX, mY);
    }

    protected abstract Tooltip setupTooltip();

    @Nullable
    protected abstract Tooltip getTooltip();
}
