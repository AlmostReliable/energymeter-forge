package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.network.menu.DataHandler;
import com.almostreliable.energymeter.util.TypeEnums.IoSetting;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IoConfig implements INBTSerializable<CompoundTag>, DataHandler {

    private final Map<Direction, IoSetting> directionToSetting = new EnumMap<>(Direction.class);
    private final BiConsumer<Direction, IoSetting> settingChangedListener;
    private boolean changed;

    public IoConfig(BiConsumer<Direction, IoSetting> settingChangedListener) {
        this.settingChangedListener = settingChangedListener;

        for (Direction direction : Direction.values()) {
            directionToSetting.put(direction, IoSetting.OFF);
        }
    }

    public IoConfig() {
        this((direction, setting) -> {});
    }

    public IoSetting getSetting(Direction direction) {
        return directionToSetting.get(direction);
    }

    public void setSetting(Direction direction, IoSetting setting) {
        if (directionToSetting.get(direction) == setting) return;

        directionToSetting.put(direction, setting);
        changed = true;
        settingChangedListener.accept(direction, setting);
    }

    public void cycleSetting(Direction direction, boolean reverse) {
        IoSetting currentSetting = directionToSetting.get(direction);
        var ioSettingValues = IoSetting.values();

        int newSettingOrdinal = (currentSetting.ordinal() + (reverse ? -1 : 1)) % ioSettingValues.length;
        if (newSettingOrdinal < 0) {
            newSettingOrdinal = ioSettingValues.length - 1;
        }

        setSetting(direction, ioSettingValues[newSettingOrdinal]);
    }

    public void resetSetting(Direction direction) {
        setSetting(direction, IoSetting.OFF);
    }

    public void forEachOutput(Consumer<Direction> consumer) {
        for (var entry : directionToSetting.entrySet()) {
            if (entry.getValue() == IoSetting.OUT) {
                consumer.accept(entry.getKey());
            }
        }
    }

    public boolean hasInput() {
        return directionToSetting.containsValue(IoSetting.IN);
    }

    public boolean hasOutput() {
        return directionToSetting.containsValue(IoSetting.OUT);
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (var entry : directionToSetting.entrySet()) {
            tag.putString(entry.getKey().name(), entry.getValue().name());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        for (Direction direction : Direction.values()) {
            String setting = compoundTag.getString(direction.name());
            directionToSetting.put(direction, IoSetting.valueOf(setting));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        for (Direction dir : Direction.values()) {
            buffer.writeByte(directionToSetting.get(dir).ordinal());
        }
        changed = false;
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        for (Direction dir : Direction.values()) {
            // TODO: extract all occurrences of Enum#values() to a variable as it is a costly operation
            directionToSetting.put(dir, IoSetting.values()[buffer.readByte()]);
        }
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }
}
