package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.rlnt.energymeter.core.Constants.UI_COLORS;
import dev.rlnt.energymeter.meter.MeterTile;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.network.SettingUpdatePacket;
import dev.rlnt.energymeter.util.GuiUtils;
import dev.rlnt.energymeter.util.GuiUtils.Tooltip;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.ACCURACY;
import dev.rlnt.energymeter.util.TypeEnums.MODE;
import dev.rlnt.energymeter.util.TypeEnums.SETTING;
import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

class SettingButton extends GenericButton {

    private static final String TEXTURE = "setting";
    private static final int TEXTURE_WIDTH = 63;
    private static final int TEXTURE_HEIGHT = 21;
    private final String label;
    private final FontRenderer font;
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
        String settingKey = setting.toString().toLowerCase();
        Tooltip t = Tooltip.builder().addHeader(settingKey).addBlankLine();
        if (longTooltip) {
            t.addDescription(settingKey + "_desc_1");
            String description2 = "_desc_2";
            if (setting == SETTING.NUMBER) t.addDescription(settingKey + description2);
            if (setting == SETTING.MODE) {
                IFormattableTextComponent transfer = TextUtils
                    .translate(TRANSLATE_TYPE.MODE, MODE.TRANSFER.toString().toLowerCase())
                    .append(":");
                Style style1 = transfer.getStyle().withColor(Color.fromRgb(UI_COLORS.BLUE));
                IFormattableTextComponent consumer = TextUtils
                    .translate(TRANSLATE_TYPE.MODE, MODE.CONSUMER.toString().toLowerCase())
                    .append(":");
                Style style2 = consumer.getStyle().withColor(Color.fromRgb(UI_COLORS.PURPLE));
                t
                    .addBlankLine()
                    .addComponent(transfer.withStyle(style1))
                    .addDescription(settingKey + description2)
                    .addBlankLine()
                    .addComponent(consumer.withStyle(style2))
                    .addDescription(settingKey + "_desc_3");
            }
            if (setting == SETTING.ACCURACY) {
                IFormattableTextComponent exact = TextUtils
                    .translate(TRANSLATE_TYPE.ACCURACY, ACCURACY.EXACT.toString().toLowerCase())
                    .append(":");
                Style style3 = exact.getStyle().withColor(Color.fromRgb(UI_COLORS.ORANGE));
                IFormattableTextComponent interval = TextUtils
                    .translate(TRANSLATE_TYPE.ACCURACY, ACCURACY.INTERVAL.toString().toLowerCase())
                    .append(":");
                Style style4 = interval.getStyle().withColor(Color.fromRgb(UI_COLORS.PINK));
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

        IFormattableTextComponent currentSetting = TextUtils
            .translate(TRANSLATE_TYPE.TOOLTIP, "current", TextFormatting.GREEN)
            .append(TextUtils.colorize(": ", TextFormatting.GREEN));
        switch (setting) {
            case NUMBER:
                currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.NUMBER,
                    container.getTile().getNumberMode().toString().toLowerCase(),
                    TextFormatting.WHITE
                ));
                break;
            case MODE:
                currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.MODE,
                    container.getTile().getMode().toString().toLowerCase(),
                    TextFormatting.WHITE
                ));
                break;
            case ACCURACY:
                currentSetting.append(TextUtils.translate(TRANSLATE_TYPE.ACCURACY,
                    container.getTile().getAccuracy().toString().toLowerCase(),
                    TextFormatting.WHITE
                ));
                break;
        }
        t.addComponent(currentSetting).addBlankLine();
        if (!longTooltip) t.addHoldAction("key.keyboard.left.shift", "action_4");
        return t.addClickAction("action_3");
    }

    @Override
    protected void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new SettingUpdatePacket(setting));
        container.getTile().updateSetting(setting);
        screen.changeTextBoxValue(MeterTile.REFRESH_RATE, false);
        tooltip = setupTooltip(false);
        tooltipLong = setupTooltip(true);
    }

    @Override
    public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
        super.renderButton(matrix, mX, mY, partial);
        // label
        int pX = (width - font.width(label)) / 2 + x + 1;
        int pY = (height - font.lineHeight) / 2 + y + 1;
        GuiUtils.renderText(matrix, pX, pY, 1.0f, label, UI_COLORS.WHITE);
        // tooltips
        if (isHovered) renderToolTip(matrix, mX, mY);
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        screen.renderComponentTooltip(matrix, (Screen.hasShiftDown() ? tooltipLong : tooltip).resolve(), mX, mY);
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
