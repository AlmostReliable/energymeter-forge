package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public abstract class GenericButton extends Button {

    final MeterContainer container;
    final MeterScreen screen;

    GenericButton(MeterScreen screen, int pX, int pY, int width, int height) {
        super(
            screen.getGuiLeft() + pX,
            screen.getGuiTop() + pY,
            width,
            height,
            TextComponent.EMPTY,
            button -> ((GenericButton) button).clickHandler()
        );
        container = screen.getMenu();
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        // background texture
        RenderSystem.setShaderTexture(0, TextUtils.getRL("textures/gui/buttons/" + getTexture() + ".png"));
        // button texture
        blit(stack, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
    }

    /**
     * Handles the functionality which is triggered when clicking the button.
     * <p>
     * Can be overwritten by buttons to resolve individual functionality.
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
