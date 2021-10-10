package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.network.SettingUpdatePacket;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums;
import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class SettingButton extends AbstractButton {

    private static final int POS_Y = 66;
    private static final int TEXTURE_WIDTH = 28;
    private static final int TEXTURE_HEIGHT = 19;
    private final String texture;
    private final TypeEnums.SETTING setting;

    SettingButton(AbstractContainerScreen<?> screen, int pX, TypeEnums.SETTING setting) {
        super(
            screen,
            pX,
            POS_Y,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT,
            true,
            button -> PacketHandler.CHANNEL.sendToServer(new SettingUpdatePacket(setting))
        );
        texture = setting.toString().toLowerCase();
        this.setting = setting;
    }

    @Override
    protected String getTexture() {
        return texture;
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
        var tooltips = new ArrayList<Component>();
        tooltips.add(
            TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, setting.toString().toLowerCase(), ChatFormatting.GOLD)
        );
        tooltips.add(new TextComponent(" "));
        tooltips.add(MeterScreen.getClickTooltip());
        screen.renderComponentTooltip(stack, tooltips, mX, mY);
    }
}
