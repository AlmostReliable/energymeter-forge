package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.GuiUtils;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.almostreliable.energymeter.core.Constants.*;

public class MeterScreen extends AbstractContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;
    private static final Tooltip TOOLTIP = setupTooltip();
    private final Collection<AbstractWidget> toRender = new ArrayList<>();
    private IntervalBox intervalBox;
    private ThresholdBox thresholdBox;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    private static Tooltip setupTooltip() {
        return Tooltip.builder()
            .addHeader(SIDE_CONFIG_ID)
            .addBlankLine()
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE,
                    BLOCK_SIDE.FRONT.toString().toLowerCase(),
                    ChatFormatting.WHITE
                )))
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, ChatFormatting.WHITE)));
    }

    IntervalBox getIntervalBox() {
        return intervalBox;
    }

    ThresholdBox getThresholdBox() {
        return thresholdBox;
    }

    @Override
    protected void init() {
        super.init();

        intervalBox = new IntervalBox(this, font, leftPos + 18, topPos + imageHeight + 5, 42, 8);
        addRenderable(intervalBox);

        thresholdBox = new ThresholdBox(this, font, leftPos + 81, topPos + imageHeight + 5, 42, 8);
        addRenderable(thresholdBox);

        addRenderables(IOButton.create(this, BLOCK_SIDE.values()));
        addRenderable(new SettingButton(this, 136, 64, SETTING.NUMBER));
        addRenderable(new SettingButton(this, 136, 86, SETTING.MODE));
        addRenderable(new SettingButton(this, 136, 108, SETTING.ACCURACY));
    }

    private void addRenderable(AbstractWidget widget) {
        addRenderableWidget(widget);
        toRender.add(widget);
    }

    /**
     * Convenience method to add multiple widgets at once.
     *
     * @param widgets the list of widgets to add
     */
    private void addRenderables(Iterable<? extends AbstractWidget> widgets) {
        for (var widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    @Override
    public void render(PoseStack stack, int mX, int mY, float partial) {
        renderBackground(stack);
        super.render(stack, mX, mY, partial);
        renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        // front screen tooltip
        if (isWithinRegion(mX, mY, 159, 16, 23, 16)) {
            renderComponentTooltip(stack, TOOLTIP.resolve(), mX, mY);
            return;
        }
        // widget tooltips
        for (var widget : toRender) {
            if (widget.isHoveredOrFocused() && widget.visible) {
                widget.renderToolTip(stack, mX, mY);
            }
        }
        super.renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int pX, int pY) {
        // header
        GuiUtils.renderText(stack,
            11,
            9,
            1.3f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, METER_ID),
            UI_COLORS.WHITE
        );

        // transfer rate
        GuiUtils.renderText(stack,
            11,
            26,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ':',
            UI_COLORS.GRAY
        );
        var formattedFlow = TextUtils.formatEnergy(menu.getEntity().getTransferRate(),
            menu.getEntity().getNumberMode() == NUMBER_MODE.LONG
        );
        GuiUtils.renderText(stack,
            16,
            37,
            1.0f,
            String.format("%s %s/t", formattedFlow.getA(), formattedFlow.getB()),
            UI_COLORS.MINT
        );

        // status
        GuiUtils.renderText(stack,
            11,
            50,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(stack,
            16,
            61,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, menu.getEntity().getStatus().toString().toLowerCase()),
            getStatusColor()
        );

        // mode
        GuiUtils.renderText(stack,
            11,
            74,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(stack,
            16,
            85,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.MODE, menu.getEntity().getMode().toString().toLowerCase()),
            getModeColor()
        );

        // accuracy
        GuiUtils.renderText(stack,
            11,
            98,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, ACCURACY_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(stack,
            16,
            109,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.ACCURACY,
                menu.getEntity().getAccuracy().toString().toLowerCase()
            ),
            getAccuracyColor()
        );
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    /**
     * Gets a color representing the current status.
     *
     * @return the color of the current status
     */
    private int getStatusColor() {
        var status = menu.getEntity().getStatus();
        return switch (status) {
            case DISCONNECTED -> UI_COLORS.RED;
            case CONNECTED -> UI_COLORS.YELLOW;
            case TRANSFERRING -> UI_COLORS.GREEN;
            case CONSUMING -> UI_COLORS.ROSE;
        };
    }

    /**
     * Gets a color representing the current mode.
     *
     * @return the color of the current mode
     */
    private int getModeColor() {
        return menu.getEntity().getMode() == MODE.CONSUMER ? UI_COLORS.PURPLE : UI_COLORS.BLUE;
    }

    /**
     * Gets a color representing the current accuracy mode.
     *
     * @return the color of the current accuracy mode
     */
    private int getAccuracyColor() {
        return menu.getEntity().getAccuracy() == ACCURACY.EXACT ? UI_COLORS.ORANGE : UI_COLORS.PINK;
    }

    /**
     * Checks if the mouse cursor is within a specified region.
     *
     * @param mX     mouse position on the x-axis
     * @param mY     mouse position on the y-axis
     * @param pX     left position on the x-axis
     * @param width  width to calculate the boundary on the x-axis
     * @param pY     top position on the y-axis
     * @param height height to calculate the boundary on the y-axis
     * @return true if the cursor is within the region, false otherwise
     */
    @SuppressWarnings("SameParameterValue")
    private boolean isWithinRegion(int mX, int mY, int pX, int width, int pY, int height) {
        return mX >= leftPos + pX && mX <= leftPos + pX + width && mY >= topPos + pY && mY <= topPos + pY + height;
    }
}
