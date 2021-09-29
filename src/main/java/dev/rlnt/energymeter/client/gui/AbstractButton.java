package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.StringTextComponent;

public abstract class AbstractButton extends Button {

    /**
     * Holds the parent {@link Container} of the parent {@link ContainerScreen}.
     */
    final Container container;
    /**
     * Holds the parent {@link ContainerScreen} the {@link Button} is rendered in.
     */
    final ContainerScreen<?> screen;
    /**
     * Defines if the tooltips are strictly rendered after the {@link Button} texture.
     * This needs to be false when an additional render layer is needed between texture
     * and tooltips otherwise tooltips will render behind it.
     */
    private final boolean strictTooltips;

    AbstractButton(
        final ContainerScreen<?> screen,
        final int pX,
        final int pY,
        final int width,
        final int height,
        final boolean strictTooltips,
        final IPressable onPress
    ) {
        super(screen.getGuiLeft() + pX, screen.getGuiTop() + pY, width, height, StringTextComponent.EMPTY, onPress);
        container = screen.getMenu();
        this.screen = screen;
        this.strictTooltips = strictTooltips;
    }

    @Override
    public void renderButton(final MatrixStack matrix, final int mX, final int mY, final float partial) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        // background texture
        Minecraft
            .getInstance()
            .getTextureManager()
            .bind(TextUtils.getRL("textures/gui/buttons/" + getTexture() + ".png"));
        // button texture
        blit(matrix, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
        // tooltips
        if (strictTooltips && isHovered) renderToolTip(matrix, mX, mY);
    }

    /**
     * Gets the texture file name for the button as {@link String}.
     * The default location points to "textures/gui/buttons".
     * @return the texture file name
     */
    protected abstract String getTexture();

    /**
     * Gets the texture atlas width for the rendering calls.
     * @return the texture atlas width
     */
    protected abstract int getTextureWidth();

    /**
     * Gets the texture atlas height for the rendering calls.
     * @return the texture atlas height
     */
    protected abstract int getTextureHeight();
}
