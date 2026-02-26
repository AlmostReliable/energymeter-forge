package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.client.screen.layout.InputLayoutElement.TextBoxType;
import com.almostreliable.energymeter.menu.MeterMenu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TextValueClientAction implements ClientAction<MeterMenu> {

    public static final ResourceLocation ID = EnergyMeter.getRL("text_value");
    private static final TextBoxType[] TEXT_BOX_VALUES = TextBoxType.values();
    private static final String TEXT_BOX_ID = "text_box";
    private static final String VALUE_ID = "value";

    private final TextBoxType textBox;
    private final int value;

    public TextValueClientAction(TextBoxType textBox, int value) {
        this.textBox = textBox;
        this.value = value;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(CompoundTag tag) {
        tag.putInt(TEXT_BOX_ID, textBox.ordinal());
        tag.putInt(VALUE_ID, value);
    }

    @Override
    public void handleServer(MeterMenu menu, ServerPlayer player) {
        textBox.updateValue(menu.getBlockEntity(), value);
    }

    public static TextValueClientAction decode(CompoundTag tag) {
        int ordinal = tag.getInt(TEXT_BOX_ID);
        int value = tag.getInt(VALUE_ID);
        return new TextValueClientAction(TEXT_BOX_VALUES[ordinal], value);
    }
}
