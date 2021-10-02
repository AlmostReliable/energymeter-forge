package dev.rlnt.energymeter.client.gui;

import static dev.rlnt.energymeter.core.Constants.*;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;

public class MeterScreen extends AbstractContainerScreen<MeterContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/meter.png");
    private static final int TEXTURE_WIDTH = 192;
    private static final int TEXTURE_HEIGHT = 91;

    public MeterScreen(final MeterContainer container, final Inventory inventory, final Component name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    /**
     * Gets the {@link MutableComponent} for the "click to change mode" text in the tooltips.
     * @return the click to change mode tooltip
     */
    static MutableComponent getClickTooltip() {
        return TextUtils
            .colorize("> ", ChatFormatting.GRAY)
            .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "click_1", ChatFormatting.AQUA))
            .append(" ")
            .append(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, "click_2", ChatFormatting.GRAY));
    }

    @Override
    protected void init() {
        super.init();
        // clickable buttons
        addWidgets(IOButton.create(this, BLOCK_SIDE.values()));
        addRenderableWidget(new SettingButton(this, 128, SETTING.NUMBER));
        addRenderableWidget(new SettingButton(this, 158, SETTING.MODE));
    }

    @Override
    public void render(final PoseStack stack, final int mX, final int mY, final float partial) {
        renderBackground(stack);
        super.render(stack, mX, mY, partial);
        renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderTooltip(final PoseStack stack, final int mX, final int mY) {
        final List<Component> tooltips = new ArrayList<>();
        if (isWithinRegion(mX, mY, 148, 17, 26, 17)) {
            // io config front face
            tooltips.add(TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, SIDE_CONFIG_ID, ChatFormatting.GOLD));
            tooltips.add(new TextComponent(" "));
            tooltips.add(
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
            );
            tooltips.add(
                TextUtils
                    .translate(TRANSLATE_TYPE.TOOLTIP, IO_MODE_ID, ChatFormatting.GREEN)
                    .append(TextUtils.colorize(": ", ChatFormatting.GREEN))
                    .append(TextUtils.translate(TRANSLATE_TYPE.IO_SETTING, IO_SCREEN_ID, ChatFormatting.WHITE))
            );
        }
        if (!tooltips.isEmpty()) {
            renderComponentTooltip(stack, tooltips, mX, mY);
            return;
        }
        super.renderTooltip(stack, mX, mY);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void renderLabels(final PoseStack stack, final int pX, final int pY) {
        // header labels
        final float yPos = 12;
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, TRANSFER_RATE_ID) + ":",
            15,
            yPos,
            ChatFormatting.GOLD.getColor()
        );
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, STATUS_ID) + ":",
            15,
            yPos * 3,
            ChatFormatting.WHITE.getColor()
        );
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, MODE_ID) + ":",
            15,
            yPos * 5,
            ChatFormatting.WHITE.getColor()
        );

        // smaller value labels
        stack.pushPose();
        stack.scale(0.98f, 0.98f, 0.98f);

        final Tuple<String, String> formattedFlow = TextUtils.formatEnergy(
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
        font.draw(stack, getConnectionString(), 20, 48, getConnectionColor().getColor());
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, menu.getEntity().getMode().toString().toLowerCase()),
            20,
            72,
            getConsumerColor().getColor()
        );

        stack.popPose();
    }

    @Override
    protected void renderBg(final PoseStack stack, final float partial, final int mX, final int mY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    /**
     * Gets a {@link String} representation of the current connection status.
     * @return the current connection status
     */
    private String getConnectionString() {
        final STATUS connection = menu.getEntity().getStatus();
        return TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, connection.toString().toLowerCase());
    }

    /**
     * Gets a color representing the current connection status.
     * @return the color of the current connection status
     */
    private ChatFormatting getConnectionColor() {
        final STATUS connection = menu.getEntity().getStatus();
        return switch (connection) {
            case DISCONNECTED -> ChatFormatting.RED;
            case CONNECTED -> ChatFormatting.YELLOW;
            case TRANSFERRING -> ChatFormatting.GREEN;
            case CONSUMING -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    /**
     * Gets a color representing the current consumer mode.
     * @return the color of the current consumer mode
     */
    private ChatFormatting getConsumerColor() {
        return menu.getEntity().getMode() == MODE.CONSUMER ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA;
    }

    /**
     * Convenience method to add multiple buttons at once.
     * @param buttons the list of buttons to add
     * @param <T> the button class
     */
    private <T extends Button> void addWidgets(final List<T> buttons) {
        for (final T button : buttons) {
            addRenderableWidget(button);
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
