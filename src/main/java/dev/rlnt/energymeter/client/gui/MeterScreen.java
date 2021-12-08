package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.network.IntervalUpdatePacket;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.util.GuiUtils;
import dev.rlnt.energymeter.util.GuiUtils.Tooltip;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import static dev.rlnt.energymeter.core.Constants.*;

public class MeterScreen extends ContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 199;
    private static final int TEXTURE_HEIGHT = 129;
    private final Tooltip tooltip;
    private TextBox textBox;

    @SuppressWarnings("AssignmentToSuperclassField")
    public MeterScreen(MeterContainer container, PlayerInventory inventory, ITextComponent name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        tooltip = setupTooltip();
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

    @Override
    protected void init() {
        super.init();
        // clickable buttons
        addButtons(IOButton.create(this, BLOCK_SIDE.values()));
        addButton(new SettingButton(this, 136, 64, SETTING.NUMBER));
        addButton(new SettingButton(this, 136, 86, SETTING.MODE));
        addButton(new SettingButton(this, 136, 108, SETTING.ACCURACY));
        // text box
        textBox = new TextBox(this, font, leftPos + 75, topPos + 109, 42, 8);
        addWidget(textBox);
    }

    @Override
    public void render(MatrixStack matrix, int mX, int mY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mX, mY, partial);
        if (menu.getTile().getAccuracy() == ACCURACY.INTERVAL) textBox.render(matrix, mX, mY, partial);
        renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrix, int mX, int mY) {
        // front screen tooltip
        if (isWithinRegion(mX, mY, 159, 16, 23, 16)) {
            renderComponentTooltip(matrix, tooltip.resolve(), mX, mY);
            return;
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

    /**
     * Convenience method to add multiple buttons at once.
     *
     * @param multipleButtons the list of buttons to add
     * @param <T>             the button class
     */
    private <T extends Widget> void addButtons(Iterable<T> multipleButtons) {
        for (T button : multipleButtons) {
            addButton(button);
        }
    }
}
