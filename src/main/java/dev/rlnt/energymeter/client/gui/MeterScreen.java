package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import dev.rlnt.energymeter.network.IntervalUpdatePacket;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.Tooltip;
import dev.rlnt.energymeter.util.TypeEnums.*;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;

public class MeterScreen extends AbstractContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 192;
    private static final int TEXTURE_HEIGHT = 112;
    private final Tooltip tooltip;
    private TextBox textBox;

    public MeterScreen(MeterContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        tooltip = setupTooltip();
    }

    TextBox getTextBox() {
        return textBox;
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
        if (sync) PacketHandler.CHANNEL.sendToServer(
            new IntervalUpdatePacket(Math.max(value, MeterEntity.REFRESH_RATE))
        );
    }

    /**
     * Checks if the current value of the text box is valid and syncs it if it changed.
     * <p>
     * Will replace the text with the previous value if invalid.
     */
    void validateTextBox() {
        int oldValue = menu.getEntity().getInterval();
        int value;
        try {
            value = Integer.parseInt(textBox.getValue());
        } catch (NumberFormatException e) {
            changeTextBoxValue(oldValue, false);
            return;
        }

        if (value != oldValue) changeTextBoxValue(value, true);
    }

    @Override
    protected void init() {
        super.init();
        // clickable buttons
        addRenderableWidgets(IOButton.create(this, BLOCK_SIDE.values()));
        addRenderableWidget(new SettingButton(this, 128, 66, SETTING.NUMBER, "FE"));
        addRenderableWidget(new SettingButton(this, 158, 66, SETTING.MODE, "M"));
        addRenderableWidget(new ResetButton(this, 128, 87));
        // text box
        textBox = new TextBox(this, font, leftPos + 69, topPos + 87, 48, 14);
        addRenderableWidget(textBox);
    }

    @Override
    public void render(PoseStack stack, int mX, int mY, float partial) {
        renderBackground(stack);
        super.render(stack, mX, mY, partial);
        textBox.render(stack, mX, mY, partial);
        renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        if (isWithinRegion(mX, mY, 148, 17, 26, 17)) {
            renderComponentTooltip(stack, tooltip.get(), mX, mY);
            return;
        }
        super.renderTooltip(stack, mX, mY);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void renderLabels(PoseStack stack, int pX, int pY) {
        // header labels
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ":",
            15,
            12,
            ChatFormatting.GOLD.getColor()
        );
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ":",
            15,
            38,
            ChatFormatting.WHITE.getColor()
        );
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ":",
            15,
            61,
            ChatFormatting.WHITE.getColor()
        );
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, INTERVAL_ID) + ":",
            15,
            87,
            ChatFormatting.WHITE.getColor()
        );

        // smaller value labels
        stack.pushPose();
        stack.scale(0.98f, 0.98f, 0.98f);

        Tuple<String, String> formattedFlow = TextUtils.formatEnergy(
            menu.getEntity().getTransferRate(),
            menu.getEntity().getNumberMode() == NUMBER_MODE.LONG
        );
        font.draw(
            stack,
            String.format("%s %s/t", formattedFlow.getA(), formattedFlow.getB()),
            20,
            24,
            ChatFormatting.WHITE.getColor()
        );
        font.draw(stack, getStatusString(), 20, 49, getStatusColor().getColor());
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, menu.getEntity().getMode().toString().toLowerCase()),
            20,
            73,
            getModeColor().getColor()
        );

        stack.popPose();
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    private Tooltip setupTooltip() {
        return Tooltip
            .builder()
            // header
            .addHeader(SIDE_CONFIG_ID)
            .addBlankLine()
            // screen info
            .add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, ChatFormatting.GREEN)
                    .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                    .append(
                        TextUtils.translate(
                            TRANSLATE_TYPE.BLOCK_SIDE,
                            BLOCK_SIDE.FRONT.toString().toLowerCase(),
                            ChatFormatting.WHITE
                        )
                    )
            )
            .add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, ChatFormatting.GREEN)
                    .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                    .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, ChatFormatting.WHITE))
            );
    }

    /**
     * Gets a {@link String} representation of the current status.
     *
     * @return the current status
     */
    private String getStatusString() {
        var connection = menu.getEntity().getStatus();
        return TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, connection.toString().toLowerCase());
    }

    /**
     * Gets a color representing the current status.
     *
     * @return the color of the current status
     */
    private ChatFormatting getStatusColor() {
        var status = menu.getEntity().getStatus();
        return switch (status) {
            case DISCONNECTED -> ChatFormatting.RED;
            case CONNECTED -> ChatFormatting.YELLOW;
            case TRANSFERRING -> ChatFormatting.GREEN;
            case CONSUMING -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    /**
     * Gets a color representing the current mode.
     *
     * @return the color of the current mode
     */
    private ChatFormatting getModeColor() {
        return menu.getEntity().getMode() == MODE.CONSUMER ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA;
    }

    /**
     * Convenience method to add multiple widgets at once.
     *
     * @param widgets the list of widgets to add
     * @param <W>     the button class
     */
    private <W extends Button> void addRenderableWidgets(List<W> widgets) {
        for (var widget : widgets) {
            addRenderableWidget(widget);
        }
    }

    /**
     * Checks if the mouse cursor is within a specified region.
     *
     * @param mX     mouse position on the x-axis
     * @param mY     mouse position on the y-axis
     * @param pX     left position on the x-axis
     * @param widht  width to calculate the boundary on the x-axis
     * @param pY     top position on the y-axis
     * @param height height to calculate the boundary on the y-axis
     * @return true if the curser is within the region, false otherwise
     */
    @SuppressWarnings("SameParameterValue")
    private boolean isWithinRegion(int mX, int mY, int pX, int widht, int pY, int height) {
        return (mX >= leftPos + pX && mX <= leftPos + pX + widht && mY >= topPos + pY && mY <= topPos + pY + height);
    }
}
