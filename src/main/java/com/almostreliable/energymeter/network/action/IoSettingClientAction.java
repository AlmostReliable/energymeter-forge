package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.core.Constants;
import com.almostreliable.energymeter.menu.MeterMenu;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;

public final class IoSettingClientAction implements ClientAction<MeterMenu> {

    public static final ResourceLocation ID = EnergyMeter.getRL(Constants.SIDE_CONFIG_ID);
    private static final Direction[] DIRECTION_VALUES = Direction.values();
    private static final String RESET_ID = "reset";
    private static final String DIRECTION_ID = "direction";
    private static final String SETTING_ID = "setting";

    private final @Nullable Direction direction;
    private final @Nullable IoSettingWithPriority setting;
    private final boolean reset;

    private IoSettingClientAction(@Nullable Direction direction, @Nullable IoSettingWithPriority setting, boolean reset) {
        this.direction = direction;
        this.setting = setting;
        this.reset = reset;
    }

    private static IoSettingClientAction reset() {
        return new IoSettingClientAction(null, null, true);
    }

    public static IoSettingClientAction change(@Nullable Direction direction, IoSettingWithPriority setting) {
        if (direction == null) return reset();
        return new IoSettingClientAction(direction, setting, false);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(CompoundTag tag) {
        tag.putBoolean(RESET_ID, reset);

        if (!reset) {
            assert direction != null && setting != null;
            tag.putInt(DIRECTION_ID, direction.ordinal());
            tag.put(SETTING_ID, setting.serialize());
        }
    }

    @Override
    public void handleServer(MeterMenu menu, ServerPlayer player) {
        if (reset) {
            menu.getBlockEntity().getIoConfig().resetSettings();
            return;
        }

        assert direction != null && setting != null;
        menu.getBlockEntity().getIoConfig().setSetting(direction, setting);
    }

    public static IoSettingClientAction decode(CompoundTag tag) {
        boolean reset = tag.getBoolean(RESET_ID);
        if (reset) return reset();

        Direction direction = DIRECTION_VALUES[tag.getInt(DIRECTION_ID)];
        IoSettingWithPriority setting = IoSettingWithPriority.deserialize(tag.getCompound(SETTING_ID));

        return change(direction, setting);
    }
}
