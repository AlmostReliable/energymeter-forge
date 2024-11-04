package com.github.almostreliable.energymeter.block.multiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public final class OptionalDirectionProperty extends EnumProperty<OptionalDirection> {

    public static final OptionalDirectionProperty HORIZONTAL = create(
        "horizontal",
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    );
    public static final OptionalDirectionProperty VERTICAL = create("vertical", Direction.UP, Direction.DOWN);

    private OptionalDirectionProperty(String name, Collection<Direction> directions) {
        super(name, OptionalDirection.class, mapDirections(directions));
    }

    public static OptionalDirectionProperty create(String name, Direction... directions) {
        return new OptionalDirectionProperty(name, List.of(directions));
    }

    private static Collection<OptionalDirection> mapDirections(Collection<Direction> directions) {
        var optionalDirections = EnumSet.noneOf(OptionalDirection.class);
        optionalDirections.add(OptionalDirection.NONE);
        for (var direction : directions) {
            optionalDirections.add(OptionalDirection.fromDirection(direction));
        }
        return optionalDirections;
    }
}
