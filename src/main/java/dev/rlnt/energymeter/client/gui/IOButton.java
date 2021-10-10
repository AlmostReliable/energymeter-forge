package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.SideConfiguration;
import dev.rlnt.energymeter.network.IOUpdatePacket;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Tuple;

public class IOButton extends AbstractButton {

    private static final String TEXTURE = "io";
    private static final int POS_X = 132;
    private static final int POS_Y = 10;
    private static final int TEXTURE_WIDTH = 28;
    private static final int TEXTURE_HEIGHT = 24;
    private static final int BUTTON_SIZE = 16;
    private static final int ZONE_SIZE = 17;
    private static final int OVERLAY_SIZE = 12;
    private static final int OVERLAY_OFFSET = 2;
    private final BLOCK_SIDE side;
    private IO_SETTING setting;

    private IOButton(AbstractContainerScreen<?> screen, BLOCK_SIDE side) {
        super(
            screen,
            POS_X + getButtonPos(side).getA(),
            POS_Y + getButtonPos(side).getB(),
            BUTTON_SIZE,
            BUTTON_SIZE,
            false,
            IOButton::clickHandler
        );
        this.side = side;
        this.setting = ((MeterContainer) container).getEntity().getSideConfig().get(side);
    }

    /**
     * Handles the functionality which is triggered when clicking the button.
     * <p>
     * Gets the block side the button is for and translates it to a direction.
     * After that, it will send a packet to the server for synchronization.
     *
     * @param abstractButton the button which was clicked
     */
    private static void clickHandler(Button abstractButton) {
        IOButton button = (IOButton) abstractButton;
        PacketHandler.CHANNEL.sendToServer(new IOUpdatePacket(button.side, button.setting));
    }

    /**
     * Creates an {@link IOButton} for each passed in {@link BLOCK_SIDE}.
     *
     * @param sides the sides for which the buttons should be created
     * @return a list of all buttons created
     */
    static List<IOButton> create(AbstractContainerScreen<?> screen, BLOCK_SIDE... sides) {
        List<IOButton> res = new ArrayList<>();
        for (BLOCK_SIDE side : sides) {
            if (side == BLOCK_SIDE.FRONT) continue;
            res.add(new IOButton(screen, side));
        }
        return res;
    }

    /**
     * Returns the x and y positions for the texture depending on the {@link BLOCK_SIDE}.
     *
     * @param side the BLOCK_SIDE to get the positions for
     * @return the x and y position for the BLOCK_SIDE
     */
    private static Tuple<Integer, Integer> getButtonPos(BLOCK_SIDE side) {
        return switch (side) {
            case BOTTOM -> new Tuple<>(ZONE_SIZE, ZONE_SIZE * 2);
            case TOP -> new Tuple<>(ZONE_SIZE, 0);
            case BACK -> new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE * 2);
            case LEFT -> new Tuple<>(0, ZONE_SIZE);
            case RIGHT -> new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE);
            case FRONT -> new Tuple<>(0, 0);
        };
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        super.renderButton(stack, mX, mY, partial);
        // io overlay
        renderIOOverlay(stack);
        // tooltips
        if (isHovered) renderToolTip(stack, mX, mY);
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

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        List<Component> tooltips = new ArrayList<>();

        // io configuration
        tooltips.add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, SIDE_CONFIG_ID, ChatFormatting.GOLD));
        tooltips.add(new TextComponent(" "));
        // block side
        tooltips.add(
            TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(
                    TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE, side.toString().toLowerCase(), ChatFormatting.WHITE)
                )
        );
        // current mode
        tooltips.add(
            TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, ChatFormatting.GREEN)
                .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                .append(
                    TextUtils.translate(
                        TRANSLATE_TYPE.IO_SETTING,
                        setting.toString().toLowerCase(),
                        ChatFormatting.WHITE
                    )
                )
        );
        tooltips.add(new TextComponent(" "));
        // click to change mode
        tooltips.add(MeterScreen.getClickTooltip());
        // shift click to reset mode
        tooltips.add(
            TextUtils
                .colorize("> ", ChatFormatting.GRAY)
                .append(
                    TextUtils.colorize(
                        String.format(
                            "%s + %s",
                            InputConstants.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                            TextUtils.translateAsString(TRANSLATE_TYPE.TOOLTIP, "shift_click_1")
                        ),
                        ChatFormatting.AQUA
                    )
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "shift_click_2", ChatFormatting.GRAY))
        );

        screen.renderComponentTooltip(stack, tooltips, mX, mY);
    }

    @Override
    public void onClick(double mX, double mY) {
        if (isHovered) changeMode(Screen.hasShiftDown());
        super.onClick(mX, mY);
    }

    /**
     * Renders the I/O overlay for the {@link IOButton} depending on its {@link IO_SETTING}.
     *
     * @param stack the pose stack for the render call
     */
    private void renderIOOverlay(PoseStack stack) {
        int textureOffset = (setting.ordinal() - 1) * OVERLAY_SIZE;
        if (textureOffset >= 0) blit(
            stack,
            x + OVERLAY_OFFSET,
            y + OVERLAY_OFFSET,
            BUTTON_SIZE,
            textureOffset,
            OVERLAY_SIZE,
            OVERLAY_SIZE,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );
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

        SideConfiguration sideConfig = ((MeterContainer) container).getEntity().getSideConfig();

        setting =
            switch (setting) {
                case OFF -> sideConfig.hasInput() ? IO_SETTING.OUT : IO_SETTING.IN;
                case IN -> sideConfig.hasMaxOutputs() ? IO_SETTING.OFF : IO_SETTING.OUT;
                case OUT -> IO_SETTING.OFF;
            };
    }
}
