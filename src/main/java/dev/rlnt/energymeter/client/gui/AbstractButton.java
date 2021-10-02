package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.util.TextUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractButton extends Button {

    /**
     * Holds the parent {@link AbstractContainerMenu} of the parent {@link AbstractContainerScreen}.
     */
    final AbstractContainerMenu container;
    /**
     * Holds the parent {@link AbstractContainerScreen} the {@link Button} is rendered in.
     */
    final AbstractContainerScreen<?> screen;
    /**
     * Defines if the tooltips are strictly rendered after the {@link Button} texture.
     * This needs to be false when an additional render layer is needed between texture
     * and tooltips otherwise tooltips will render behind it.
     */
    private final boolean strictTooltips;

    AbstractButton(
        AbstractContainerScreen<?> screen,
        int pX,
        int pY,
        int width,
        int height,
        boolean strictTooltips,
        OnPress onPress
    ) {
        super(screen.getGuiLeft() + pX, screen.getGuiTop() + pY, width, height, TextComponent.EMPTY, onPress);
        container = screen.getMenu();
        this.screen = screen;
        this.strictTooltips = strictTooltips;
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        // background texture
        RenderSystem.setShaderTexture(0, TextUtils.getRL("textures/gui/buttons/" + getTexture() + ".png"));
        // button texture
        blit(stack, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
        // tooltips
        if (strictTooltips && isHovered) renderToolTip(stack, mX, mY);
    }

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
