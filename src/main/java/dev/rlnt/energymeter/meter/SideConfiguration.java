package dev.rlnt.energymeter.meter;

import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import java.util.EnumMap;
import net.minecraft.util.Direction;

public class SideConfiguration {

    private static final int SIZE = 12;
    private final EnumMap<Direction, IO_SETTING> directionConfig = new EnumMap<>(Direction.class);
    private final EnumMap<BLOCK_SIDE, IO_SETTING> sideConfig = new EnumMap<>(BLOCK_SIDE.class);
    private final Direction facing;

    SideConfiguration(Direction facing) {
        this.facing = facing;
        for (final Direction direction : Direction.values()) {
            directionConfig.put(direction, IO_SETTING.OFF);
        }
        for (final BLOCK_SIDE side : BLOCK_SIDE.values()) {
            sideConfig.put(side, IO_SETTING.OFF);
        }
    }

    /**
     * Converts the side configuration to a serializable integer array.
     *
     * @return the side configuration as integer array
     */
    public int[] serialize() {
        final int[] serialized = new int[SIZE];
        for (int i = 0; i < Direction.values().length; i++) {
            serialized[i] = directionConfig.get(Direction.values()[i]).ordinal();
        }
        for (int i = 0; i < BLOCK_SIDE.values().length; i++) {
            serialized[SIZE / 2 + i] = sideConfig.get(BLOCK_SIDE.values()[i]).ordinal();
        }
        return serialized;
    }

    /**
     * Reads the side configuration from an integer array and stores it.
     *
     * @param serialized the integer array to deserialize
     */
    public void deserialize(final int[] serialized) {
        for (int i = 0; i < Direction.values().length; i++) {
            directionConfig.put(Direction.values()[i], IO_SETTING.values()[serialized[i]]);
        }
        for (int i = 0; i < BLOCK_SIDE.values().length; i++) {
            sideConfig.put(BLOCK_SIDE.values()[i], IO_SETTING.values()[serialized[SIZE / 2 + i]]);
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
        return directionConfig.get(direction);
    }

    /**
     * Gets an IO setting by a specified block side.
     * This automatically takes the facing direction into account.
     *
     * @param side the block side to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(final BLOCK_SIDE side) {
        return sideConfig.get(side);
    }

    /**
     * Sets the specified block side to the specified IO setting.
     *
     * @param side    the side on which the setting should be changed
     * @param setting the setting which should be set
     */
    public void set(final BLOCK_SIDE side, final IO_SETTING setting) {
        sideConfig.put(side, setting);
        directionConfig.put(getDirectionFromSide(side), setting);
    }

    /**
     * Checks if the side configuration has an input set somewhere.
     *
     * @return true if there is an input, false otherwise
     */
    public boolean hasInput() {
        return sideConfig.containsValue(IO_SETTING.IN);
    }

    /**
     * Checks if the side configuration has an output set somewhere.
     *
     * @return true if there is an output, false otherwise
     */
    public boolean hasOutput() {
        return sideConfig.containsValue(IO_SETTING.OUT);
    }

    /**
     * Checks if the side configuration already has the maximum amount of outputs.
     *
     * @return true if there are max outputs, false otherwise
     */
    public boolean hasMaxOutputs() {
        return sideConfig.values().stream().filter(setting -> setting == IO_SETTING.OUT).count() == 4;
    }

    /**
     * Gets the direction from the given block side depending on the facing of the block.
     *
     * @param side the block side to get the direction from
     * @return the direction
     */
    public Direction getDirectionFromSide(final BLOCK_SIDE side) {
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
