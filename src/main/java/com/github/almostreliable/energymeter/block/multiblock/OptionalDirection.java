package com.github.almostreliable.energymeter.block.multiblock;

import net.minecraft.core.BlockPos;
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

    public static OptionalDirection fromDirection(@Nullable Direction direction) {
        for (var value : values()) {
            if (value.direction == direction) {
                return value;
            }
        }
        return NONE;
    }

    public void relative(BlockPos.MutableBlockPos pos) {
        if (direction == null) return;
        pos.move(direction);
    }

    public boolean isNone() {
        return direction == null;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
