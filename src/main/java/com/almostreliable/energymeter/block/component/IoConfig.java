package com.almostreliable.energymeter.block.component;

import com.almostreliable.energymeter.network.menu.DataHandler;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.util.INBTSerializable;

import org.jetbrains.annotations.UnknownNullability;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class IoConfig implements INBTSerializable<CompoundTag>, DataHandler {

    public static final int MAX_PRIORITY = 4;

    private final Map<Direction, IoSettingWithPriority> directionToSetting = new EnumMap<>(Direction.class);
    private final Runnable changeListener;
    private boolean changed;

    public IoConfig(Runnable changeListener) {
        this.changeListener = changeListener;

        for (Direction direction : Direction.values()) {
            directionToSetting.put(direction, IoSettingWithPriority.OFF);
        }
    }

    public IoConfig() {
        this(() -> {});
    }

    public IoSettingWithPriority getSetting(Direction direction) {
        return directionToSetting.get(direction);
    }

    public void setSetting(Direction direction, IoSettingWithPriority setting) {
        if (getSetting(direction).equals(setting)) return;

        directionToSetting.put(direction, setting);
        changed = true;
        changeListener.run();
    }

    public void cycleSetting(Direction direction, boolean reverse) {
        IoSettingWithPriority currentSetting = getSetting(direction);
        IoSettingWithPriority newSetting = reverse ? currentSetting.previous() : currentSetting.next();
        setSetting(direction, newSetting);
    }

    public void resetSetting(Direction direction) {
        setSetting(direction, IoSettingWithPriority.OFF);
    }

    public void forEachOutput(Consumer<Direction> consumer) {
        for (int priority = MAX_PRIORITY; priority >= 1; priority--) {
            for (Direction direction : Direction.values()) {
                IoSettingWithPriority entry = directionToSetting.get(direction);
                if (entry.setting.isOutput && entry.priority == priority) {
                    consumer.accept(direction);
                }
            }
        }
    }

    public boolean hasInput() {
        for (IoSettingWithPriority entry : directionToSetting.values()) {
            if (entry.setting.isInput) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutput() {
        for (IoSettingWithPriority entry : directionToSetting.values()) {
            if (entry.setting.isOutput) {
                return true;
            }
        }
        return false;
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        for (Direction direction : Direction.values()) {
            IoSettingWithPriority setting = directionToSetting.get(direction);
            tag.put(direction.name(), setting.serialize());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        for (Direction direction : Direction.values()) {
            CompoundTag directionTag = tag.getCompound(direction.name());
            IoSettingWithPriority setting = IoSettingWithPriority.deserialize(directionTag);
            directionToSetting.put(direction, setting);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        for (Direction direction : Direction.values()) {
            IoSettingWithPriority setting = directionToSetting.get(direction);
            setting.encode(buffer);
        }
        changed = false;
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        for (Direction direction : Direction.values()) {
            IoSettingWithPriority setting = IoSettingWithPriority.decode(buffer);
            directionToSetting.put(direction, setting);
        }
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    public record IoSettingWithPriority(IoSetting setting, int priority) {

        public static final IoSettingWithPriority OFF = new IoSettingWithPriority(IoSetting.OFF, 0);
        public static final IoSettingWithPriority IN = new IoSettingWithPriority(IoSetting.IN, 0);
        public static final IoSettingWithPriority OUT_DEFAULT = new IoSettingWithPriority(IoSetting.OUT, 1);
        private static final IoSetting[] IO_SETTINGS = IoSetting.values();

        public static IoSettingWithPriority priorityOutput(int priority) {
            return new IoSettingWithPriority(IoSetting.OUT, priority);
        }

        public boolean isDisabled() {
            return this == OFF || (!setting.isInput && !setting.isOutput);
        }

        public boolean isInput() {
            return this == IN || setting.isInput;
        }

        public boolean isOutput() {
            return this == OUT_DEFAULT || setting.isOutput;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("setting", setting.name());
            tag.putInt("priority", priority);
            return tag;
        }

        public static IoSettingWithPriority deserialize(CompoundTag tag) {
            IoSetting setting = IoSetting.valueOf(tag.getString("setting"));
            int priority = tag.getInt("priority");
            return new IoSettingWithPriority(setting, priority);
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeByte(setting.ordinal());
            buffer.writeByte(priority);
        }

        private static IoSettingWithPriority decode(FriendlyByteBuf buffer) {
            IoSetting setting = IO_SETTINGS[buffer.readByte()];
            int priority = buffer.readByte();
            return new IoSettingWithPriority(setting, priority);
        }

        private IoSettingWithPriority next() {
            int nextOrdinal = (setting.ordinal() + 1) % IO_SETTINGS.length;
            return new IoSettingWithPriority(IO_SETTINGS[nextOrdinal], priority);
        }

        private IoSettingWithPriority previous() {
            int prevOrdinal = (setting.ordinal() - 1 + IO_SETTINGS.length) % IO_SETTINGS.length;
            return new IoSettingWithPriority(IO_SETTINGS[prevOrdinal], priority);
        }
    }

    public enum IoSetting {

        OFF(false, false),
        IN(true, false),
        OUT(false, true);

        private final boolean isInput;
        private final boolean isOutput;

        IoSetting(boolean isInput, boolean isOutput) {
            this.isInput = isInput;
            this.isOutput = isOutput;
        }
    }
}
