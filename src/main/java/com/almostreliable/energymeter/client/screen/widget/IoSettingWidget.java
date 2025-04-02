package com.almostreliable.energymeter.client.screen.widget;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.util.TypeEnums.IoSetting;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class IoSettingWidget extends PositionlessWidget {

    private static final ResourceLocation TEXTURE = EnergyMeter.getRL("textures/gui/button/io.png");
    private static final int TEXTURE_WIDTH = 51;
    private static final int TEXTURE_HEIGHT = 17;
    private static final int BUTTON_SIZE = 17;
    private static final Consumer<IoSetting> DEFAULT_ON_SELECT = setting -> {};

    private Consumer<IoSetting> onSelect = DEFAULT_ON_SELECT;

    public IoSettingWidget() {
        super(BUTTON_SIZE * 6, BUTTON_SIZE);
        disable();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // background
        guiGraphics.fill(getX() - 1, getY() - 1, getX() + BUTTON_SIZE * 6 + 1, getY() + BUTTON_SIZE + 1, 0xFF00_FFA2);

        // off button
        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // input button background and overlay
        guiGraphics.blit(TEXTURE, getX() + BUTTON_SIZE, getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.blit(TEXTURE, getX() + BUTTON_SIZE, getY(), BUTTON_SIZE, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // output button backgrounds and overlays
        for (int i = 0; i < 4; i++) {
            renderOutputButton(guiGraphics, i);
        }
    }

    private void renderOutputButton(GuiGraphics guiGraphics, int index) {
        int x = getX() + BUTTON_SIZE * 2 + BUTTON_SIZE * index;

        // button background
        guiGraphics.blit(TEXTURE, x, getY(), 0, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.blit(TEXTURE, x, getY(), BUTTON_SIZE * 2, 0, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // button overlay
        guiGraphics.drawCenteredString(
            font,
            String.valueOf(index + 1),
            x + BUTTON_SIZE / 2 + 1,
            getY() + BUTTON_SIZE / 2 - font.lineHeight / 2,
            15_658_734
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        // check mouseX to see which button was clicked
        if (mouseX >= getX() && mouseX <= getX() + BUTTON_SIZE) {
            // off button
            onSelect.accept(IoSetting.OFF);
        } else if (mouseX >= getX() + BUTTON_SIZE && mouseX <= getX() + BUTTON_SIZE * 2) {
            // input button
            onSelect.accept(IoSetting.IN);
        } else if (mouseX >= getX() + BUTTON_SIZE * 2 && mouseX <= getX() + BUTTON_SIZE * 6) {
            // output buttons
            onSelect.accept(IoSetting.OUT);
        }

        disable();
    }

    private void disable() {
        visible = false;
        active = false;
        onSelect = DEFAULT_ON_SELECT;
    }

    void bind(int x, int y, Consumer<IoSetting> onSelect) {
        setPosition(x, y);
        this.onSelect = onSelect;
        visible = true;
        active = true;
    }

    boolean isBound() {
        return visible;
    }
}
