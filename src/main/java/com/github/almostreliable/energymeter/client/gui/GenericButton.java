package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.StringTextComponent;

public abstract class GenericButton extends Button {

    /**
     * Holds the parent {@link Container} of the parent {@link ContainerScreen}.
     */
    final MeterContainer container;
    /**
     * Holds the parent {@link ContainerScreen} the {@link Button} is rendered in.
     */
    final MeterScreen screen;

    GenericButton(MeterScreen screen, int pX, int pY, int width, int height) {
        super(
            screen.getGuiLeft() + pX,
            screen.getGuiTop() + pY,
            width,
            height,
            StringTextComponent.EMPTY,
            button -> ((GenericButton) button).clickHandler()
        );
        container = screen.getMenu();
        this.screen = screen;
    }

    @Override
    public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
        Minecraft
            .getInstance()
            .getTextureManager()
            .bind(TextUtils.getRL("textures/gui/buttons/" + getTexture() + ".png"));
        // button texture
        blit(matrix, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
    }

    /**
     * Handles the functionality which is triggered when clicking the button.
     * <p>
     * Can be overwritten by buttons to get individual functionality.
     */
    protected abstract void clickHandler();

    /**
     * Gets the texture file name for the button as {@link String}.
     * The default location points to "textures/gui/buttons".
     *
     * @return the texture file name
     */
    protected abstract String getTexture();

    /**
     * Gets the texture atlas width for the rendering calls.
     *
     * @return the texture atlas width
     */
    protected abstract int getTextureWidth();

    /**
     * Gets the texture atlas height for the rendering calls.
     *
     * @return the texture atlas height
     */
    protected abstract int getTextureHeight();
}
