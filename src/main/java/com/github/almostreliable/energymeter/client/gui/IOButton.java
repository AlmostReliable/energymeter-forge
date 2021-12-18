package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.core.Constants;
import com.github.almostreliable.energymeter.network.IOUpdatePacket;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import com.github.almostreliable.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class IOButton extends GenericButton {

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
        setting = container.getEntity().getSideConfig().get(side);
        tooltip = setupTooltip();
    }

    /**
     * Returns the x and y positions for the texture depending on the {@link BLOCK_SIDE}.
     *
     * @param side the BLOCK_SIDE to resolve the positions for
     * @return the x and y position for the BLOCK_SIDE
     */
    private static Tuple<Integer, Integer> getButtonPos(BLOCK_SIDE side) {
        return switch (side) {
            case TOP -> new Tuple<>(ZONE_SIZE, 0);
            case LEFT -> new Tuple<>(0, ZONE_SIZE);
            case RIGHT -> new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE);
            case BOTTOM -> new Tuple<>(ZONE_SIZE, ZONE_SIZE * 2);
            case BACK -> new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE * 2);
            case FRONT -> new Tuple<>(0, 0);
        };
    }

    private Tooltip setupTooltip() {
        return Tooltip.builder()
            // header
            .addHeader(Constants.SIDE_CONFIG_ID).addBlankLine()
            // block side
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, Constants.IO_SIDE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE,
                    side.toString().toLowerCase(),
                    ChatFormatting.WHITE
                )))
            // current mode
            .addComponent(TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, Constants.IO_MODE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING,
                    setting.toString().toLowerCase(),
                    ChatFormatting.WHITE
                ))).addBlankLine()
            // action
            .addClickAction("action_1").addShiftClickAction("action_2");
    }

    /**
     * Creates an io button for each passed in {@link BLOCK_SIDE}.
     *
     * @param sides the sides for which the buttons should be created
     * @return a list of all buttons created
     */
    static List<IOButton> create(MeterScreen screen, BLOCK_SIDE... sides) {
        return Arrays
            .stream(sides)
            .filter(side -> side != BLOCK_SIDE.FRONT)
            .map(side -> new IOButton(screen, side))
            .collect(Collectors.toList());
    }

    @Override
    protected void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new IOUpdatePacket(side, setting));
        tooltip = setupTooltip();
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        super.renderButton(stack, mX, mY, partial);
        // io overlay
        renderIOOverlay(stack);
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

    /**
     * Renders the I/O overlay for the button depending on its {@link IO_SETTING}.
     *
     * @param stack the pose stack for the render call
     */
    private void renderIOOverlay(PoseStack stack) {
        var textureOffset = (setting.ordinal() - 1) * OVERLAY_SIZE;
        if (textureOffset >= 0) {
            blit(stack, x, y, BUTTON_SIZE, textureOffset, OVERLAY_SIZE, OVERLAY_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        screen.renderComponentTooltip(stack, tooltip.resolve(), mX, mY);
    }

    @Override
    public void onClick(double mX, double mY) {
        if (isHovered) changeMode(Screen.hasShiftDown());
        super.onClick(mX, mY);
    }

    /**
     * Changes the mode of a {@link BLOCK_SIDE} depending on its current {@link IO_SETTING}.
     *
     * @param reset whether the field should be reset to OFF
     */
    private void changeMode(boolean reset) {
        if (reset) {
            setting = IO_SETTING.OFF;
            return;
        }

        var sideConfig = container.getEntity().getSideConfig();

        setting = switch (setting) {
            case OFF -> sideConfig.hasInput() ? IO_SETTING.OUT : IO_SETTING.IN;
            case IN -> sideConfig.hasMaxOutputs() ? IO_SETTING.OFF : IO_SETTING.OUT;
            case OUT -> IO_SETTING.OFF;
        };
    }
}
