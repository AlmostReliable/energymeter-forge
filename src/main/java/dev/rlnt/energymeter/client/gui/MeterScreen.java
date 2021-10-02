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

    public MeterScreen(MeterContainer container, Inventory inventory, Component name) {
        super(container, inventory, name);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    /**
     * Gets the {@link MutableComponent} for the "click to change mode" text in the tooltips.
     *
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
    public void render(PoseStack stack, int mX, int mY, float partial) {
        renderBackground(stack);
        super.render(stack, mX, mY, partial);
        renderTooltip(stack, mX, mY);
    }

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        List<Component> tooltips = new ArrayList<>();
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
    protected void renderLabels(PoseStack stack, int pX, int pY) {
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
        font.draw(stack, getStatusString(), 20, 48, getStatusColor().getColor());
        font.draw(
            stack,
            TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, menu.getEntity().getMode().toString().toLowerCase()),
            20,
            72,
            getModeColor().getColor()
        );

        stack.popPose();
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    /**
     * Gets a {@link String} representation of the current status.
     *
     * @return the current status
     */
    private String getStatusString() {
        STATUS connection = menu.getEntity().getStatus();
        return TextUtils.translateAsString(TRANSLATE_TYPE.STATUS, connection.toString().toLowerCase());
    }

    /**
     * Gets a color representing the current status.
     *
     * @return the color of the current status
     */
    private ChatFormatting getStatusColor() {
        STATUS status = menu.getEntity().getStatus();
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
    private <W extends Button> void addWidgets(List<W> widgets) {
        for (W widget : widgets) {
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
