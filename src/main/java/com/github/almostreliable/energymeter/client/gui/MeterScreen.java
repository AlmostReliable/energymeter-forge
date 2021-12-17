package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.util.GuiUtils;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.almostreliable.energymeter.core.Constants.*;

public class MeterScreen extends ContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;
    private static final Tooltip TOOLTIP = setupTooltip();
    private final Collection<Widget> renderables = new ArrayList<>();
    private IntervalBox intervalBox;
    private ThresholdBox thresholdBox;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterContainer container, PlayerInventory inventory, ITextComponent name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    private static Tooltip setupTooltip() {
        return Tooltip.builder()
            // header
            .addHeader(SIDE_CONFIG_ID)
            .addBlankLine()
            // screen info
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE,
                    BLOCK_SIDE.FRONT.toString().toLowerCase(),
                    TextFormatting.WHITE
                )))
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, TextFormatting.WHITE)));
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
        // interval box
        intervalBox = new IntervalBox(this, font, leftPos + 18, topPos + imageHeight + 5, 42, 8);
        addRenderable(intervalBox);
        // threshold box
        thresholdBox = new ThresholdBox(this, font, leftPos + 81, topPos + imageHeight + 5, 42, 8);
        addRenderable(thresholdBox);
        // clickable buttons
        addRenderables(IOButton.create(this, BLOCK_SIDE.values()));
        addRenderable(new SettingButton(this, 136, 64, SETTING.NUMBER));
        addRenderable(new SettingButton(this, 136, 86, SETTING.MODE));
        addRenderable(new SettingButton(this, 136, 108, SETTING.ACCURACY));
    }

    private void addRenderable(Widget widget) {
        addButton(widget);
        renderables.add(widget);
    }

    /**
     * Convenience method to add multiple buttons at once.
     *
     * @param widgets the list of buttons to add
     * @param <T>     the button class
     */
    private <T extends Widget> void addRenderables(Iterable<T> widgets) {
        for (T widget : widgets) {
            addRenderable(widget);
        }
    }

    @Override
    public void render(MatrixStack matrix, int mX, int mY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mX, mY, partial);
        renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrix, int mX, int mY) {
        // front screen tooltip
        if (isWithinRegion(mX, mY, 159, 16, 23, 16)) {
            renderComponentTooltip(matrix, TOOLTIP.resolve(), mX, mY);
            return;
        }
        // widget tooltips
        for (Widget widget : renderables) {
            if (widget.isHovered() && widget.visible) {
                widget.renderToolTip(matrix, mX, mY);
            }
        }
        super.renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderLabels(MatrixStack matrix, int pX, int pY) {
        // header
        GuiUtils.renderText(matrix,
            11,
            9,
            1.3f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, METER_ID),
            UI_COLORS.WHITE
        );

        // transfer rate
        GuiUtils.renderText(matrix,
            11,
            26,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ':',
            UI_COLORS.GRAY
        );
        Tuple<String, String> formattedFlow = TextUtils.formatEnergy(menu.getTile().getTransferRate(),
            menu.getTile().getNumberMode() == NUMBER_MODE.LONG
        );
        GuiUtils.renderText(matrix,
            16,
            37,
            1.0f,
            String.format("%s %s/t", formattedFlow.getA(), formattedFlow.getB()),
            UI_COLORS.MINT
        );

        // status
        GuiUtils.renderText(matrix,
            11,
            50,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(matrix,
            16,
            61,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, menu.getTile().getStatus().toString().toLowerCase()),
            getStatusColor()
        );

        // mode
        GuiUtils.renderText(matrix,
            11,
            74,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(matrix,
            16,
            85,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.MODE, menu.getTile().getMode().toString().toLowerCase()),
            getModeColor()
        );

        // accuracy
        GuiUtils.renderText(matrix,
            11,
            98,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, ACCURACY_ID) + ':',
            UI_COLORS.GRAY
        );
        GuiUtils.renderText(matrix,
            16,
            109,
            1.0f,
            TextUtils.translateAsString(TRANSLATE_TYPE.ACCURACY, menu.getTile().getAccuracy().toString().toLowerCase()),
            getAccuracyColor()
        );
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partial, int pX, int pY) {
        // background texture
        if (minecraft == null) return;
        //noinspection deprecation
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    /**
     * Gets a color representing the current connection status.
     *
     * @return the color of the current connection status
     */
    private int getStatusColor() {
        STATUS connection = menu.getTile().getStatus();
        switch (connection) {
            case DISCONNECTED:
                return UI_COLORS.RED;
            case CONNECTED:
                return UI_COLORS.YELLOW;
            case TRANSFERRING:
                return UI_COLORS.GREEN;
            case CONSUMING:
                return UI_COLORS.ROSE;
            default:
                throw new IllegalStateException("There is no connection status with value: " + connection);
        }
    }

    /**
     * Gets a color representing the current consumer mode.
     *
     * @return the color of the current consumer mode
     */
    private int getModeColor() {
        return menu.getTile().getMode() == MODE.CONSUMER ? UI_COLORS.PURPLE : UI_COLORS.BLUE;
    }

    /**
     * Gets a color representing the current accuracy mode.
     *
     * @return the color of the current accuracy mode
     */
    private int getAccuracyColor() {
        return menu.getTile().getAccuracy() == ACCURACY.EXACT ? UI_COLORS.ORANGE : UI_COLORS.PINK;
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
