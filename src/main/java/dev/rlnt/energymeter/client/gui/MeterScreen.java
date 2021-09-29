package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class MeterScreen extends ContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 192;
    private static final int TEXTURE_HEIGHT = 91;

    public MeterScreen(final MeterContainer container, final PlayerInventory inventory, final ITextComponent name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    /**
     * Gets the {@link ITextComponent} for the "click to change mode" text in the tooltips.
     * @return the click to change mode tooltip
     */
    static ITextComponent getClickTooltip() {
        return TextUtils
            .colorize("> ", TextFormatting.GRAY)
            .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "click_1", TextFormatting.AQUA))
            .append(" ")
            .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "click_2", TextFormatting.GRAY));
    }

    @Override
    protected void init() {
        super.init();
        // clickable buttons
        addButtons(IOButton.create(this, BLOCK_SIDE.values()));
        addButton(new SettingButton(this, 128, SETTING.NUMBER));
        addButton(new SettingButton(this, 158, SETTING.MODE));
    }

    @Override
    public void render(final MatrixStack matrix, final int mX, final int mY, final float partial) {
        renderBackground(matrix);
        super.render(matrix, mX, mY, partial);
        renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderTooltip(final MatrixStack matrix, final int mX, final int mY) {
        final List<ITextComponent> tooltips = new ArrayList<>();
        if (isWithinRegion(mX, mY, 148, 17, 26, 17)) {
            // io config front face
            tooltips.add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, SIDE_CONFIG_ID, TextFormatting.GOLD));
            tooltips.add(new StringTextComponent(" "));
            tooltips.add(
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
            );
            tooltips.add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, TextFormatting.GREEN)
                    .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                    .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, TextFormatting.WHITE))
            );
        }
        if (!tooltips.isEmpty()) {
            renderComponentTooltip(matrix, tooltips, mX, mY);
            return;
        }
        super.renderTooltip(matrix, mX, mY);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void renderLabels(final MatrixStack matrix, final int pX, final int pY) {
        // header labels
        final float yPos = 12;
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ":",
            15,
            yPos,
            TextFormatting.GOLD.getColor()
        );
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ":",
            15,
            yPos * 3,
            TextFormatting.WHITE.getColor()
        );
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ":",
            15,
            yPos * 5,
            TextFormatting.WHITE.getColor()
        );

        // smaller value labels
        matrix.pushPose();
        matrix.scale(0.98f, 0.98f, 0.98f);

        final Tuple<String, String> formattedFlow = TextUtils.formatEnergy(
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
        font.draw(matrix, getConnectionString(), 20, 48, getConnectionColor().getColor());
        font.draw(
            matrix,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, menu.getTile().getMode().toString().toLowerCase()),
            20,
            72,
            getConsumerColor().getColor()
        );

        matrix.popPose();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(final MatrixStack matrix, final float partial, final int pX, final int pY) {
        if (minecraft == null) return;
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    /**
     * Gets a {@link String} representation of the current connection status.
     * @return the current connection status
     */
    private String getConnectionString() {
        final STATUS connection = menu.getTile().getStatus();
        return TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, connection.toString().toLowerCase());
    }

    /**
     * Gets a color representing the current connection status.
     * @return the color of the current connection status
     */
    private TextFormatting getConnectionColor() {
        final STATUS connection = menu.getTile().getStatus();
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
     * @return the color of the current consumer mode
     */
    private TextFormatting getConsumerColor() {
        return menu.getTile().getMode() == MODE.CONSUMER ? TextFormatting.LIGHT_PURPLE : TextFormatting.AQUA;
    }

    /**
     * Convenience method to add multiple buttons at once.
     * @param buttons the list of buttons to add
     * @param <T> the button class
     */
    private <T extends Widget> void addButtons(final List<T> buttons) {
        for (final T button : buttons) {
            addButton(button);
        }
    }

    /**
     * Checks if the mouse cursor is within a specified region.
     * @param mX mouse position on the x-axis
     * @param mY mouse position on the y-axis
     * @param pX left position on the x-axis
     * @param widht width to calculate the boundary on the x-axis
     * @param pY top position on the y-axis
     * @param height height to calculate the boundary on the y-axis
     * @return true if the curser is within the region, false otherwise
     */
    @SuppressWarnings("SameParameterValue")
    private boolean isWithinRegion(
        final int mX,
        final int mY,
        final int pX,
        final int widht,
        final int pY,
        final int height
    ) {
        return (mX >= leftPos + pX && mX <= leftPos + pX + widht && mY >= topPos + pY && mY <= topPos + pY + height);
    }
}
