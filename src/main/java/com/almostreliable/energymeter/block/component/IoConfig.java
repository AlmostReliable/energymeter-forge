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

    private final Map<Direction, IoSetting> directionToSetting = new EnumMap<>(Direction.class);
    private final Runnable changeListener;
    private boolean changed;

    public IoConfig(Runnable changeListener) {
        this.changeListener = changeListener;

        for (Direction direction : Direction.values()) {
            directionToSetting.put(direction, IoSetting.OFF);
        }
    }

    public IoConfig() {
        this(() -> {});
    }

    public IoSetting getSetting(Direction direction) {
        return directionToSetting.get(direction);
    }

    public void setSetting(Direction direction, IoSetting setting) {
        if (directionToSetting.get(direction) == setting) return;

        directionToSetting.put(direction, setting);
        changed = true;
        changeListener.run();
    }

    public void cycleSetting(Direction direction, boolean reverse) {
        IoSetting currentSetting = directionToSetting.get(direction);
        IoSetting newSetting = reverse ? currentSetting.previous() : currentSetting.next();
        setSetting(direction, newSetting);
    }

    public void resetSetting(Direction direction) {
        setSetting(direction, IoSetting.OFF);
    }

    public void forEachOutput(Consumer<Direction> consumer) {
        directionToSetting.entrySet().stream()
            .filter(e -> e.getValue().isOutput)
            .sorted((e1, e2) -> Integer.compare(e2.getValue().priority, e1.getValue().priority))
            .forEach(e -> consumer.accept(e.getKey()));
    }

    public boolean hasInput() {
        for (IoSetting setting : directionToSetting.values()) {
            if (setting.isInput) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutput() {
        for (IoSetting setting : directionToSetting.values()) {
            if (setting.isOutput) {
                return true;
            }
        }
        return false;
    }

    @Override
    @UnknownNullability
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (var entry : directionToSetting.entrySet()) {
            tag.put(entry.getKey().name(), entry.getValue().serialize());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        for (Direction direction : Direction.values()) {
            IoSetting setting = IoSetting.deserialize(compoundTag.getCompound(direction.name()));
            directionToSetting.put(direction, setting);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        for (Direction direction : Direction.values()) {
            IoSetting setting = directionToSetting.get(direction);
            buffer.writeByte(setting.getOrdinal());
            if (setting.isOutput) {
                buffer.writeByte(setting.priority);
            }
        }
        changed = false;
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        for (Direction direction : Direction.values()) {
            IoSetting setting = IoSetting.of(buffer.readByte());
            if (setting.isOutput) {
                setting = setting.withPriority(buffer.readByte());
            }
            setSetting(direction, setting);
        }
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    public static final class IoSetting {

        public static final IoSetting OFF = new IoSetting(0, "off", false, false, 0);
        public static final IoSetting IN = new IoSetting(1, "input", true, false, 0);
        public static final IoSetting OUT = new IoSetting(2, "output", false, true, 1);
        private static final IoSetting[] VALUES = {OFF, IN, OUT};

        private final int ordinal;
        private final String name;
        private final boolean isInput;
        private final boolean isOutput;
        private final int priority;

        private IoSetting(int ordinal, String name, boolean isInput, boolean isOutput, int priority) {
            this.ordinal = ordinal;
            this.name = name;
            this.isInput = isInput;
            this.isOutput = isOutput;
            this.priority = priority;
        }

        public static IoSetting of(int ordinal) {
            return VALUES[ordinal];
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("ordinal", ordinal);
            if (isOutput) {
                tag.putInt("priority", priority);
            }
            return tag;
        }

        public static IoSetting deserialize(CompoundTag tag) {
            int ordinal = tag.getInt("ordinal");
            IoSetting setting = of(ordinal);
            if (setting.isOutput()) {
                return setting.withPriority(tag.getInt("priority"));
            }
            return setting;
        }

        private IoSetting next() {
            int nextOrdinal = (ordinal + 1) % VALUES.length;
            return VALUES[nextOrdinal];
        }

        private IoSetting previous() {
            int prevOrdinal = (ordinal - 1 + VALUES.length) % VALUES.length;
            return VALUES[prevOrdinal];
        }

        public boolean isDisabled() {
            return !isInput && !isOutput;
        }

        public boolean isInput() {
            return isInput;
        }

        public boolean isOutput() {
            return isOutput;
        }

        public IoSetting withPriority(int priority) {
            if (!isOutput) {
                throw new IllegalStateException("cannot set priority on non-output setting");
            }
            return new IoSetting(ordinal, name, isInput, true, priority);
        }

        public int getOrdinal() {
            return ordinal;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }
    }
}
