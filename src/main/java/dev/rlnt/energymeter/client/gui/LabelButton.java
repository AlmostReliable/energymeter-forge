package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.util.Tooltip;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public abstract class LabelButton extends AbstractButton {

    private final String label;
    private final Font font;

    LabelButton(MeterScreen screen, int pX, int pY, int width, int height, String label) {
        super(screen, pX, pY, width, height);
        this.label = label;
        font = Minecraft.getInstance().font;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        super.renderButton(stack, mX, mY, partial);
        // label
        int pX = (width - font.width(label)) / 2 + x + 1;
        int pY = (height - font.lineHeight) / 2 + y + 1;
        font.draw(stack, label, pX, pY, ChatFormatting.WHITE.getColor());
        // tooltips
        if (isHovered) renderToolTip(stack, mX, mY);
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        if (getTooltip() != null) screen.renderComponentTooltip(stack, getTooltip().get(), mX, mY);
    }

    protected abstract Tooltip setupTooltip();

    @Nullable
    protected abstract Tooltip getTooltip();
}
