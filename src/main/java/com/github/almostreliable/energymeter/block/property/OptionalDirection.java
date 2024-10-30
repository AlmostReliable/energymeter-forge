package com.github.almostreliable.energymeter.block.property;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.Nullable;

public enum OptionalDirection implements StringRepresentable {

    NONE(null),
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST);

    @Nullable
    private final Direction direction;
    private final String name;

    OptionalDirection(@Nullable Direction direction) {
        this.direction = direction;
        this.name = direction == null ? "none" : direction.toString().toLowerCase();
    }

    static OptionalDirection fromDirection(Direction direction) {
        for (var value : values()) {
            if (value.direction == direction) {
                return value;
            }
        }
        return NONE;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
