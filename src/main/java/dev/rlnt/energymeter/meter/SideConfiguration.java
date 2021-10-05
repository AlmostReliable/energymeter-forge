package dev.rlnt.energymeter.meter;

import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import java.util.EnumMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class SideConfiguration {

    private static final int MAX_OUTPUTS = 4;
    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);
    private final int size;
    private final Direction facing;
    private final Direction bottom;

    SideConfiguration(BlockState state) {
        facing = state.getValue(MeterBlock.FACING);
        bottom = state.getValue(MeterBlock.BOTTOM);
        for (final Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
        size = config.size();
    }

    /**
     * Converts the side configuration to a serializable integer array.
     *
     * @return the side configuration as integer array
     */
    public int[] serialize() {
        final int[] serialized = new int[size];
        for (int i = 0; i < size; i++) {
            serialized[i] = config.get(Direction.values()[i]).ordinal();
        }
        return serialized;
    }

    /**
     * Reads the side configuration from an integer array and stores it.
     *
     * @param serialized the integer array to deserialize
     */
    public void deserialize(final int[] serialized) {
        for (int i = 0; i < size; i++) {
            config.put(Direction.values()[i], IO_SETTING.values()[serialized[i]]);
        }
    }

    /**
     * Gets an IO setting by a specified direction.
     * This automatically takes the facing direction into account.
     *
     * @param direction the direction to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(final Direction direction) {
        return config.get(direction);
    }

    /**
     * Gets an IO setting by a specified block side.
     * This automatically takes the facing direction into account.
     *
     * @param side the block side to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(final BLOCK_SIDE side) {
        return config.get(getDirectionFromSide(side));
    }

    /**
     * Sets the specified block side to the specified IO setting.
     *
     * @param side    the side on which the setting should be changed
     * @param setting the setting which should be set
     */
    public void set(final BLOCK_SIDE side, final IO_SETTING setting) {
        config.put(getDirectionFromSide(side), setting);
    }

    /**
     * Checks if the side configuration has an input set somewhere.
     *
     * @return true if there is an input, false otherwise
     */
    public boolean hasInput() {
        return config.containsValue(IO_SETTING.IN);
    }

    /**
     * Checks if the side configuration has an output set somewhere.
     *
     * @return true if there is an output, false otherwise
     */
    public boolean hasOutput() {
        return config.containsValue(IO_SETTING.OUT);
    }

    /**
     * Checks if the side configuration already has the maximum amount of outputs.
     *
     * @return true if there are max outputs, false otherwise
     */
    public boolean hasMaxOutputs() {
        return config.values().stream().filter(setting -> setting == IO_SETTING.OUT).count() == MAX_OUTPUTS;
    }

    /**
     * Gets the direction from the given block side depending on the facing of the block.
     *
     * @param side the block side to get the direction from
     * @return the direction
     */
    public Direction getDirectionFromSide(final BLOCK_SIDE side) {
        return facing != bottom ? verticalConversion(side) : horizontalConversion(side);
    }

    /**
     * Converts the given block side to a direction if the facing direction
     * is up or down.
     *
     * @param side the block side to convert
     * @return the direction
     */
    public Direction verticalConversion(final BLOCK_SIDE side) {
        if (side == BLOCK_SIDE.TOP) {
            return bottom.getOpposite();
        } else if (side == BLOCK_SIDE.BOTTOM) {
            return bottom;
        } else if (side == BLOCK_SIDE.LEFT) {
            return facing == Direction.UP ? bottom.getClockWise() : bottom.getCounterClockWise();
        } else if (side == BLOCK_SIDE.RIGHT) {
            return facing == Direction.UP ? bottom.getCounterClockWise() : bottom.getClockWise();
        } else if (side == BLOCK_SIDE.BACK) {
            return facing.getOpposite();
        } else {
            return facing;
        }
    }

    /**
     * Converts the given block side to a direction if the facing direction
     * is any of the 4 compass directions.
     *
     * @param side the block side to convert
     * @return the direction
     */
    public Direction horizontalConversion(final BLOCK_SIDE side) {
        if (side == BLOCK_SIDE.TOP) {
            return Direction.UP;
        } else if (side == BLOCK_SIDE.BOTTOM) {
            return Direction.DOWN;
        } else if (side == BLOCK_SIDE.LEFT) {
            return facing.getClockWise();
        } else if (side == BLOCK_SIDE.RIGHT) {
            return facing.getCounterClockWise();
        } else if (side == BLOCK_SIDE.BACK) {
            return facing.getOpposite();
        } else {
            return facing;
        }
    }
}
