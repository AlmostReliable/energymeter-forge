package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

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
    private SideConfiguration sideConfig;

    private IOButton(final ContainerScreen<?> screen, final BLOCK_SIDE side) {
        super(
            screen,
            POS_X + getButtonPos(side).getA(),
            POS_Y + getButtonPos(side).getB(),
            BUTTON_SIZE,
            BUTTON_SIZE,
            false,
            button -> PacketHandler.CHANNEL.sendToServer(new IOUpdatePacket(((IOButton) button).sideConfig, side))
        );
        this.side = side;
        syncSideConfig();
    }

    /**
     * Creates an {@link IOButton} for each passed in {@link BLOCK_SIDE}.
     * @param sides the sides for which the buttons should be created
     * @return a list of all buttons created
     */
    static List<IOButton> create(final ContainerScreen<?> screen, final BLOCK_SIDE... sides) {
        final List<IOButton> res = new ArrayList<>();
        for (final BLOCK_SIDE side : sides) {
            if (side == BLOCK_SIDE.FRONT) continue;
            res.add(new IOButton(screen, side));
        }
        return res;
    }

    /**
     * Returns the x and y positions for the texture depending on the {@link BLOCK_SIDE}.
     * @param side the BLOCK_SIDE to get the positions for
     * @return the x and y position for the BLOCK_SIDE
     */
    private static Tuple<Integer, Integer> getButtonPos(final BLOCK_SIDE side) {
        switch (side) {
            case BOTTOM:
                return new Tuple<>(ZONE_SIZE, ZONE_SIZE * 2);
            case TOP:
                return new Tuple<>(ZONE_SIZE, 0);
            case BACK:
                return new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE * 2);
            case LEFT:
                return new Tuple<>(0, ZONE_SIZE);
            case RIGHT:
                return new Tuple<>(ZONE_SIZE * 2, ZONE_SIZE);
            default:
                return new Tuple<>(0, 0);
        }
    }

    @Override
    public void renderButton(final MatrixStack matrix, final int mX, final int mY, final float partial) {
        super.renderButton(matrix, mX, mY, partial);
        // io overlay
        renderIOOverlay(matrix);
        // tooltips
        if (isHovered) renderToolTip(matrix, mX, mY);
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
    public void renderToolTip(final MatrixStack matrix, final int mX, final int mY) {
        final List<ITextComponent> tooltips = new ArrayList<>();

        // io configuration
        tooltips.add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, SIDE_CONFIG_ID, TextFormatting.GOLD));
        tooltips.add(new StringTextComponent(" "));
        // block side
        tooltips.add(
            TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_SIDE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(
                    TextUtils.translate(TRANSLATE_TYPE.BLOCK_SIDE, side.toString().toLowerCase(), TextFormatting.WHITE)
                )
        );
        // current mode
        tooltips.add(
            TextUtils
                .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, TextFormatting.GREEN)
                .append(TextUtils.colorize(": ", TextFormatting.GREEN))
                .append(
                    TextUtils.translate(
                        TRANSLATE_TYPE.IO_SETTING,
                        sideConfig.get(side).toString().toLowerCase(),
                        TextFormatting.WHITE
                    )
                )
        );
        tooltips.add(new StringTextComponent(" "));
        // click to change mode
        tooltips.add(MeterScreen.getClickTooltip());
        // shift click to reset mode
        tooltips.add(
            TextUtils
                .colorize("> ", TextFormatting.GRAY)
                .append(
                    TextUtils.colorize(
                        String.format(
                            "%s + %s",
                            InputMappings.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                            TextUtils.translateAsString(TRANSLATE_TYPE.TOOLTIP, "shift_click_1")
                        ),
                        TextFormatting.AQUA
                    )
                )
                .append(" ")
                .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "shift_click_2", TextFormatting.GRAY))
        );

        screen.renderComponentTooltip(matrix, tooltips, mX, mY);
    }

    @Override
    public void onClick(final double mX, final double mY) {
        if (isHovered) {
            changeMode(Screen.hasShiftDown());
            syncSideConfig();
        }
        super.onClick(mX, mY);
    }

    /**
     * Retrieves the {@link SideConfiguration} from the parent {@link Container} so it's
     * always synchronized with the server.
     */
    private void syncSideConfig() {
        sideConfig = ((MeterContainer) container).getTile().getSideConfig();
    }

    /**
     * Renders the I/O overlay for the {@link IOButton} depending on its {@link IO_SETTING}.
     * @param matrix the matrix stack for the render call
     */
    private void renderIOOverlay(final MatrixStack matrix) {
        final int textureOffset = (sideConfig.get(side).ordinal() - 1) * OVERLAY_SIZE;
        if (textureOffset >= 0) blit(
            matrix,
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
     * @param reset whether the field should be reset to OFF
     */
    private void changeMode(final boolean reset) {
        if (reset) {
            sideConfig.set(side, IO_SETTING.OFF);
            return;
        }

        IO_SETTING setting = sideConfig.get(side);
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
        sideConfig.set(side, setting);
    }
}
