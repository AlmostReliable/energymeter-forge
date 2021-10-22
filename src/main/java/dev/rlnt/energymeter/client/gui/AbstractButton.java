package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractButton extends Button {

    /**
     * Holds the parent {@link AbstractContainerMenu} of the parent {@link AbstractContainerScreen}.
     */
    final MeterContainer container;
    /**
     * Holds the parent {@link AbstractContainerScreen} the {@link Button} is rendered in.
     */
    final MeterScreen screen;

    AbstractButton(MeterScreen screen, int pX, int pY, int width, int height) {
        super(
            screen.getGuiLeft() + pX,
            screen.getGuiTop() + pY,
            width,
            height,
            TextComponent.EMPTY,
            button -> ((AbstractButton) button).clickHandler()
        );
        container = screen.getMenu();
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        // background texture
        RenderSystem.setShaderTexture(0, TextUtils.getRL("textures/gui/buttons/" + getTexture() + ".png"));
        // button texture
        blit(stack, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
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
