package dev.rlnt.energymeter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.rlnt.energymeter.network.PacketHandler;
import dev.rlnt.energymeter.network.SettingUpdatePacket;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums;
import dev.rlnt.energymeter.util.TypeEnums.TRANSLATE_TYPE;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class SettingButton extends AbstractButton {

    private static final int POS_Y = 66;
    private static final int TEXTURE_WIDTH = 28;
    private static final int TEXTURE_HEIGHT = 19;
    private final String texture;
    private final TypeEnums.SETTING setting;

    SettingButton(final ContainerScreen<?> screen, final int pX, final TypeEnums.SETTING setting) {
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
    public void renderToolTip(final MatrixStack matrix, final int mX, final int mY) {
        final List<ITextComponent> tooltips = new ArrayList<>();
        tooltips.add(
            TextUtils.translate(TRANSLATE_TYPE.TOOLTIP, setting.toString().toLowerCase(), TextFormatting.GOLD)
        );
        tooltips.add(new StringTextComponent(" "));
        tooltips.add(MeterScreen.getClickTooltip());
        screen.renderComponentTooltip(matrix, tooltips, mX, mY);
    }
}
