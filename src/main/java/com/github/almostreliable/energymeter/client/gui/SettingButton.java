package com.github.almostreliable.energymeter.client.gui;

import com.github.almostreliable.energymeter.core.Constants.UI_COLORS;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.network.SettingUpdatePacket;
import com.github.almostreliable.energymeter.util.GuiUtils;
import com.github.almostreliable.energymeter.util.GuiUtils.Tooltip;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.ACCURACY;
import com.github.almostreliable.energymeter.util.TypeEnums.MODE;
import com.github.almostreliable.energymeter.util.TypeEnums.SETTING;
import com.github.almostreliable.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextColor;

public class SettingButton extends GenericButton {

    private static final String TEXTURE = "setting";
    private static final int TEXTURE_WIDTH = 63;
    private static final int TEXTURE_HEIGHT = 21;
    private final String label;
    private final Font font;
    private final SETTING setting;
    private Tooltip tooltip;
    private Tooltip tooltipLong;

    SettingButton(MeterScreen screen, int pX, int pY, SETTING setting) {
        super(screen, pX, pY, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.setting = setting;
        label = TextUtils.translateAsString(TRANSLATE_TYPE.LABEL, setting.toString().toLowerCase()).toUpperCase();
        font = Minecraft.getInstance().font;
        tooltip = setupTooltip(false);
        tooltipLong = setupTooltip(true);
    }

    private Tooltip setupTooltip(boolean longTooltip) {
        var settingKey = setting.toString().toLowerCase();
        var t = Tooltip.builder().addHeader(settingKey).addBlankLine();
        if (longTooltip) {
            t.addDescription(settingKey + "_desc_1");
            var description2 = "_desc_2";
            if (setting == SETTING.NUMBER) t.addDescription(settingKey + description2);
            if (setting == SETTING.MODE) {
                var transfer = TextUtils
                    .translate(TRANSLATE_TYPE.MODE, MODE.TRANSFER.toString().toLowerCase())
                    .append(":");
                var style1 = transfer.getStyle().withColor(TextColor.fromRgb(UI_COLORS.BLUE));
                var consumer = TextUtils
                    .translate(TRANSLATE_TYPE.MODE, MODE.CONSUMER.toString().toLowerCase())
                    .append(":");
                var style2 = consumer.getStyle().withColor(TextColor.fromRgb(UI_COLORS.PURPLE));
                t
                    .addBlankLine()
                    .addComponent(transfer.withStyle(style1))
                    .addDescription(settingKey + description2)
                    .addBlankLine()
                    .addComponent(consumer.withStyle(style2))
                    .addDescription(settingKey + "_desc_3");
            }
            if (setting == SETTING.ACCURACY) {
                var exact = TextUtils
                    .translate(TRANSLATE_TYPE.ACCURACY, ACCURACY.EXACT.toString().toLowerCase())
                    .append(":");
                var style3 = exact.getStyle().withColor(TextColor.fromRgb(UI_COLORS.ORANGE));
                var interval = TextUtils
                    .translate(TRANSLATE_TYPE.ACCURACY, ACCURACY.INTERVAL.toString().toLowerCase())
                    .append(":");
                var style4 = interval.getStyle().withColor(TextColor.fromRgb(UI_COLORS.PINK));
                t
                    .addBlankLine()
                    .addComponent(exact.withStyle(style3))
                    .addDescription(settingKey + description2)
                    .addBlankLine()
                    .addComponent(interval.withStyle(style4))
                    .addDescription(settingKey + "_desc_3");
            }
            t.addBlankLine();
        }

        var currentSetting = TextUtils
            .translate(TRANSLATE_TYPE.TOOLTIP, "current", ChatFormatting.GREEN)
            .append(TextUtils.colorize(": ", ChatFormatting.GREEN));
        switch (setting) {
            case NUMBER -> currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.NUMBER,
                container.getEntity().getNumberMode().toString().toLowerCase(),
                ChatFormatting.WHITE
            ));
            case MODE -> currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.MODE,
                container.getEntity().getMode().toString().toLowerCase(),
                ChatFormatting.WHITE
            ));
            case ACCURACY -> currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.ACCURACY,
                container.getEntity().getAccuracy().toString().toLowerCase(),
                ChatFormatting.WHITE
            ));
        }
        t.addComponent(currentSetting).addBlankLine();
        if (!longTooltip) t.addHoldAction("key.keyboard.left.shift", "action_4");
        return t.addClickAction("action_3");
    }

    @Override
    protected void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new SettingUpdatePacket(setting));
        container.getEntity().updateSetting(setting);
        screen.changeTextBoxValue(MeterEntity.REFRESH_RATE, false);
        tooltip = setupTooltip(false);
        tooltipLong = setupTooltip(true);
    }

    @Override
    public void renderButton(PoseStack stack, int mX, int mY, float partial) {
        super.renderButton(stack, mX, mY, partial);
        // label
        var pX = (width - font.width(label)) / 2 + x + 1;
        var pY = (height - font.lineHeight) / 2 + y + 1;
        GuiUtils.renderText(stack, pX, pY, 1.0f, label, UI_COLORS.WHITE);
        // tooltips
        if (isHovered) renderToolTip(stack, mX, mY);
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        screen.renderComponentTooltip(stack, (Screen.hasShiftDown() ? tooltipLong : tooltip).resolve(), mX, mY);
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
}
