package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.core.Constants.UI_COLORS;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.network.IntervalUpdatePacket;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.util.GuiUtils;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MeterScreen extends AbstractContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;
    private final Tooltip tooltip;
    private TextBox textBox;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        tooltip = setupTooltip();
    }

    private static Tooltip setupTooltip() {
        return Tooltip.builder()
            // header
            .addHeader(Constants.SIDE_CONFIG_ID)
            .addBlankLine()
            // screen info
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, Constants.IO_SIDE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE,
                    BLOCK_SIDE.FRONT.toString().toLowerCase(),
                    ChatFormatting.WHITE
                )))
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, Constants.IO_MODE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, Constants.IO_SCREEN_ID, ChatFormatting.WHITE)));
    }

    /**
     * Checks if the current value of the text box is valid and syncs it if it changed.
     * <p>
     * Will replace the text with the previous value if invalid.
     */
    void validateTextBox() {
        var oldValue = menu.getEntity().getInterval();
        int value;
        try {
            value = Integer.parseInt(textBox.getValue());
        } catch (NumberFormatException e) {
            changeTextBoxValue(oldValue, false);
            return;
        }

        if (value != oldValue) changeTextBoxValue(value, true);
    }

    /**
     * Changes the text of the text box to the specified integer.
     * Automatically replaces the specified value with the minimum amount if it's lower.
     * <p>
     * When a true boolean is passed, the new value will be synced to the server.
     *
     * @param value the value to place in the text box
     * @param sync  whether the value should be synced to the server
     */
    void changeTextBoxValue(int value, boolean sync) {
        textBox.setValue(String.valueOf(Math.max(value, MeterEntity.REFRESH_RATE)));
        if (sync) {
            PacketHandler.CHANNEL.sendToServer(new IntervalUpdatePacket(Math.max(value, MeterEntity.REFRESH_RATE)));
        }
    }

    @Override
    protected void init() {
        super.init();
        // clickable buttons
        addRenderableWidgets(IOButton.create(this, BLOCK_SIDE.values()));
        addRenderableWidget(new SettingButton(this, 136, 64, SETTING.NUMBER));
        addRenderableWidget(new SettingButton(this, 136, 86, SETTING.MODE));
        addRenderableWidget(new SettingButton(this, 136, 108, SETTING.ACCURACY));
        // text box
        textBox = new TextBox(this, font, leftPos + 75, topPos + 109, 42, 8);
        addWidget(textBox);
    }

    @Override
    public void render(PoseStack stack, int mX, int mY, float partial) {
        renderBackground(stack);
        super.render(stack, mX, mY, partial);
        if (menu.getEntity().getAccuracy() == ACCURACY.INTERVAL) textBox.render(stack, mX, mY, partial);
        renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        // front screen tooltip
        if (isWithinRegion(mX, mY, 159, 16, 23, 16)) {
            renderComponentTooltip(stack, tooltip.resolve(), mX, mY);
            return;
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
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, Constants.METER_ID),
            UI_COLORS.WHITE
        );

        // transfer rate
        GuiUtils.renderText(stack,
            11,
            26,
            1.1f,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, Constants.TRANSFER_RATE_ID) + ':',
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
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, Constants.STATUS_ID) + ':',
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
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, Constants.MODE_ID) + ':',
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
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, Constants.ACCURACY_ID) + ':',
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

    /**
     * Convenience method to addComponent multiple widgets at once.
     *
     * @param widgets the list of widgets to addComponent
     * @param <W>     the button class
     */
    private <W extends Button> void addRenderableWidgets(Iterable<W> widgets) {
        for (var widget : widgets) {
            addRenderableWidget(widget);
        }
    }
}
