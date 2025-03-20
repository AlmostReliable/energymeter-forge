package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.network.menu.DataHandler;
import com.almostreliable.energymeter.util.TypeEnums;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class IoConfig implements INBTSerializable<CompoundTag>, DataHandler {

    private final Map<Direction, TypeEnums.IoSetting> directionToSetting = new EnumMap<>(Direction.class);
    private final BiConsumer<Direction, TypeEnums.IoSetting> settingChangedListener;
    private boolean changed;

    public IoConfig(BiConsumer<Direction, TypeEnums.IoSetting> settingChangedListener) {
        this.settingChangedListener = settingChangedListener;

        for (Direction direction : Direction.values()) {
            directionToSetting.put(direction, TypeEnums.IoSetting.OFF);
        }
    }

    public IoConfig() {
        this((direction, setting) -> {});
    }

    public TypeEnums.IoSetting getSetting(Direction direction) {
        return directionToSetting.get(direction);
    }

    public void setSetting(Direction direction, TypeEnums.IoSetting setting) {
        if (directionToSetting.get(direction) == setting) return;

        directionToSetting.put(direction, setting);
        changed = true;
        settingChangedListener.accept(direction, setting);
    }

    public boolean hasInput() {
        return directionToSetting.containsValue(TypeEnums.IoSetting.IN);
    }

    public boolean hasOutput() {
        return directionToSetting.containsValue(TypeEnums.IoSetting.OUT);
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
            directionToSetting.put(direction, TypeEnums.IoSetting.valueOf(setting));
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
            directionToSetting.put(dir, TypeEnums.IoSetting.values()[buffer.readByte()]);
        }
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }
}
