package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.component.SideConfiguration;
import com.github.almostreliable.energymeter.network.IOUpdatePacket;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import com.github.almostreliable.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.almostreliable.energymeter.core.Constants.IO_MODE_ID;
import static com.github.almostreliable.energymeter.core.Constants.IO_SIDE_ID;
import static com.github.almostreliable.energymeter.core.Constants.SIDE_CONFIG_ID;

final class IOButton extends GenericButton {

    private static final String TEXTURE = "io";
    private static final int POS_X = 141;
    private static final int POS_Y = 5;
    private static final int TEXTURE_WIDTH = 34;
    private static final int TEXTURE_HEIGHT = 34;
    private static final int BUTTON_SIZE = 17;
    private static final int ZONE_SIZE = 18;
    private static final int OVERLAY_SIZE = 17;
    private final BLOCK_SIDE side;
    private Tooltip tooltip;
    private IO_SETTING setting;

    private IOButton(MeterScreen screen, BLOCK_SIDE side) {
        super(screen, POS_X + getButtonPos(side).getA(), POS_Y + getButtonPos(side).getB(), BUTTON_SIZE, BUTTON_SIZE);
        this.side = side;
        setting = container.getTile().getSideConfig().get(screen.getMenu().getTile().getBlockState(), side);
        tooltip = setupTooltip();
    }

    /**
     * Creates an IOButton for each block side which is passed in.
     *
     * @param screen the screen to create the buttons for
     * @param sides  the sides for which the buttons should be created
     * @return a list of all buttons created
     */
    static List<IOButton> create(MeterScreen screen, BLOCK_SIDE... sides) {
        return Arrays
            .stream(sides)
            .filter(side -> side != BLOCK_SIDE.FRONT)
            .map(side -> new IOButton(screen, side))
            .collect(Collectors.toList());
    }

    /**
     * Returns the x and y positions for the texture depending on the block side.
     *
     * @param side the block side to get the positions for
     * @return the x and y position for the block side
     */
    private static Tuple<Integer, Integer> getButtonPos(BLOCK_SIDE side) {
        switch (side) {
            case TOP:
                return new Tuple<>(ZONE_SIZE, 0);
            case LEFT:
                return new Tuple<>(0, ZONE_SIZE);
            case RIGHT:
                return new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE);
            case BOTTOM:
                return new Tuple<>(ZONE_SIZE, ZONE_SIZE * 2);
            case BACK:
                return new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE * 2);
            default:
                return new Tuple<>(0, 0);
        }
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        screen.renderComponentTooltip(matrix, tooltip.resolve(), mX, mY);
    }

    @Override
    public void onClick(double mX, double mY) {
        if (isHovered) changeMode(Screen.hasShiftDown());
        super.onClick(mX, mY);
    }

    @Override
    public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
        super.renderButton(matrix, mX, mY, partial);
        renderIOOverlay(matrix);
    }

    @Override
    protected void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new IOUpdatePacket(side, setting));
        tooltip = setupTooltip();
    }

    @Override
    protected String getTexture() {
        return TEXTURE;
    }

    @Override
    protected int getTextureWidth() {
        return TEXTURE_WIDTH;
    }

    @Override
    protected int getTextureHeight() {
        return TEXTURE_HEIGHT;
    }

    private Tooltip setupTooltip() {
        return Tooltip
            .builder()
            .addHeader(SIDE_CONFIG_ID)
            .addBlankLine()
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE,
                    side.toString().toLowerCase(),
                    TextFormatting.WHITE
                )))
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING,
                    setting.toString().toLowerCase(),
                    TextFormatting.WHITE
                )))
            .addBlankLine()
            .addClickAction("action_1")
            .addShiftClickAction("action_2");
    }

    /**
     * Renders the I/O overlay for the IOButton depending on its io setting.
     *
     * @param matrix the matrix stack for the render call
     */
    private void renderIOOverlay(MatrixStack matrix) {
        int textureOffset = (setting.ordinal() - 1) * OVERLAY_SIZE;
        if (textureOffset >= 0) {
            blit(matrix, x, y, BUTTON_SIZE, textureOffset, OVERLAY_SIZE, OVERLAY_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    /**
     * Changes the mode of a block side depending on its current io setting.
     *
     * @param reset whether the field should be reset to OFF
     */
    private void changeMode(boolean reset) {
        if (reset) {
            setting = IO_SETTING.OFF;
            return;
        }

        SideConfiguration sideConfig = container.getTile().getSideConfig();

        switch (setting) {
            case OFF:
                setting = sideConfig.hasInput() ? IO_SETTING.OUT : IO_SETTING.IN;
                break;
            case IN:
                setting = sideConfig.hasMaxOutputs() ? IO_SETTING.OFF : IO_SETTING.OUT;
                break;
            case OUT:
                setting = IO_SETTING.OFF;
                break;
            default:
                throw new IllegalArgumentException("There is no IO mode called " + setting);
        }
    }
}
