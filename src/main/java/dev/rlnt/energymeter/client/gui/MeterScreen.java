package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.network.IntervalUpdatePacket;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.Tooltip;
import dev.rlnt.energymeter.util.TypeEnums.*;
import java.util.List;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class MeterScreen extends ContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 192;
    private static final int TEXTURE_HEIGHT = 112;
    private final Tooltip tooltip;
    private TextBox textBox;

    public MeterScreen(MeterContainer container, PlayerInventory inventory, ITextComponent name) {
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
        textBox.setValue(String.valueOf(Math.max(value, MeterTile.REFRESH_RATE)));
        if (sync) PacketHandler.CHANNEL.sendToServer(new IntervalUpdatePacket(Math.max(value, MeterTile.REFRESH_RATE)));
    }

    /**
     * Checks if the current value of the text box is valid and syncs it if it changed.
     * <p>
     * Will replace the text with the previous value if invalid.
     */
    void validateTextBox() {
        int oldValue = menu.getTile().getInterval();
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
        addButtons(IOButton.create(this, BLOCK_SIDE.values()));
        addButton(new SettingButton(this, 128, 66, SETTING.NUMBER, "FE"));
        addButton(new SettingButton(this, 158, 66, SETTING.MODE, "M"));
        addButton(new ResetButton(this, 128, 87));
        // text box
        textBox = new TextBox(this, font, leftPos + 69, topPos + 87, 48, 14);
        addWidget(textBox);
    }

    @Override
    public void render(MatrixStack matrix, int mX, int mY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mX, mY, partial);
        textBox.render(matrix, mX, mY, partial);
        renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrix, int mX, int mY) {
        if (isWithinRegion(mX, mY, 148, 17, 26, 17)) {
            renderComponentTooltip(matrix, tooltip.get(), mX, mY);
            return;
        }
        super.renderTooltip(matrix, mX, mY);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void renderLabels(MatrixStack matrix, int pX, int pY) {
        // header labels
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ":",
            15,
            12,
            TextFormatting.GOLD.getColor()
        );
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ":",
            15,
            38,
            TextFormatting.WHITE.getColor()
        );
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ":",
            15,
            61,
            TextFormatting.WHITE.getColor()
        );
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, INTERVAL_ID) + ":",
            15,
            87,
            TextFormatting.WHITE.getColor()
        );

        // smaller value labels
        matrix.pushPose();
        matrix.scale(0.98f, 0.98f, 0.98f);

        Tuple<String, String> formattedFlow = TextUtils.formatEnergy(
            menu.getTile().getTransferRate(),
            menu.getTile().getNumberMode() == NUMBER_MODE.LONG
        );
        font.draw(
            matrix,
            String.format("%s %s/t", formattedFlow.getA(), formattedFlow.getB()),
            20,
            24,
            TextFormatting.WHITE.getColor()
        );
        font.draw(matrix, getConnectionString(), 20, 49, getConnectionColor().getColor());
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, menu.getTile().getMode().toString().toLowerCase()),
            20,
            73,
            getConsumerColor().getColor()
        );

        matrix.popPose();
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

    private Tooltip setupTooltip() {
        return Tooltip
            .builder()
            // header
            .addHeader(SIDE_CONFIG_ID)
            .addBlankLine()
            // screen info
            .add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, TextFormatting.GREEN)
                    .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                    .append(
                        TextUtils.translate(
                            TRANSLATE_TYPE.BLOCK_SIDE,
                            BLOCK_SIDE.FRONT.toString().toLowerCase(),
                            TextFormatting.WHITE
                        )
                    )
            )
            .add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, TextFormatting.GREEN)
                    .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                    .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, TextFormatting.WHITE))
            );
    }

    /**
     * Gets a {@link String} representation of the current connection status.
     *
     * @return the current connection status
     */
    private String getConnectionString() {
        STATUS connection = menu.getTile().getStatus();
        return TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, connection.toString().toLowerCase());
    }

    /**
     * Gets a color representing the current connection status.
     *
     * @return the color of the current connection status
     */
    private TextFormatting getConnectionColor() {
        STATUS connection = menu.getTile().getStatus();
        switch (connection) {
            case DISCONNECTED:
                return TextFormatting.RED;
            case CONNECTED:
                return TextFormatting.YELLOW;
            case TRANSFERRING:
                return TextFormatting.GREEN;
            case CONSUMING:
                return TextFormatting.LIGHT_PURPLE;
            default:
                throw new IllegalStateException("There is no connection status with value: " + connection);
        }
    }

    /**
     * Gets a color representing the current consumer mode.
     *
     * @return the color of the current consumer mode
     */
    private TextFormatting getConsumerColor() {
        return menu.getTile().getMode() == MODE.CONSUMER ? TextFormatting.LIGHT_PURPLE : TextFormatting.AQUA;
    }

    /**
     * Convenience method to add multiple buttons at once.
     *
     * @param buttons the list of buttons to add
     * @param <T>     the button class
     */
    private <T extends Widget> void addButtons(List<T> buttons) {
        for (T button : buttons) {
            addButton(button);
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
