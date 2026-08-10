package com.almostreliable.energymeter.block.multiblock;

import com.almostreliable.energymeter.core.Constants;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public final class OptionalDirectionProperty {

    public static final EnumProperty<OptionalDirection> HORIZONTAL = create(
        Constants.HORIZONTAL_PROP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    );
    public static final EnumProperty<OptionalDirection> VERTICAL = create(Constants.VERTICAL_PROP, Direction.UP, Direction.DOWN);

    private OptionalDirectionProperty() {}

    public static EnumProperty<OptionalDirection> create(String name, Direction... directions) {
        return EnumProperty.create(name, OptionalDirection.class, mapDirections(List.of(directions)));
    }

    private static List<OptionalDirection> mapDirections(Collection<Direction> directions) {
        var optionalDirections = EnumSet.noneOf(OptionalDirection.class);
        optionalDirections.add(OptionalDirection.NONE);
        for (var direction : directions) {
            optionalDirections.add(OptionalDirection.fromDirection(direction));
        }
        return List.copyOf(optionalDirections);
    }
}
