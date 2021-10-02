package dev.rlnt.energymeter.meter;

import dev.rlnt.energymeter.util.TypeEnums.BLOCK_SIDE;
import dev.rlnt.energymeter.util.TypeEnums.IO_SETTING;
import java.util.EnumMap;
import net.minecraft.core.Direction;

public class SideConfiguration {

    private static final int SIZE = 12;
    private final EnumMap<Direction, IO_SETTING> directionConfig = new EnumMap<>(Direction.class);
    private final EnumMap<BLOCK_SIDE, IO_SETTING> sideConfig = new EnumMap<>(BLOCK_SIDE.class);
    private final Direction facing;

    SideConfiguration(Direction facing) {
        this.facing = facing;
        for (Direction direction : Direction.values()) {
            directionConfig.put(direction, IO_SETTING.OFF);
        }
        for (BLOCK_SIDE side : BLOCK_SIDE.values()) {
            sideConfig.put(side, IO_SETTING.OFF);
        }
    }

    /**
     * Converts the {@link SideConfiguration} to a serializable integer array.
     *
     * @return the {@link SideConfiguration} as integer array
     */
    public int[] serialize() {
        int[] serialized = new int[SIZE];
        for (int i = 0; i < Direction.values().length; i++) {
            serialized[i] = directionConfig.get(Direction.values()[i]).ordinal();
        }
        for (int i = 0; i < BLOCK_SIDE.values().length; i++) {
            serialized[SIZE / 2 + i] = sideConfig.get(BLOCK_SIDE.values()[i]).ordinal();
        }
        return serialized;
    }

    /**
     * Reads the {@link SideConfiguration} from an integer array and stores it.
     *
     * @param serialized the integer array to deserialize
     */
    public void deserialize(int[] serialized) {
        for (int i = 0; i < Direction.values().length; i++) {
            directionConfig.put(Direction.values()[i], IO_SETTING.values()[serialized[i]]);
        }
        for (int i = 0; i < BLOCK_SIDE.values().length; i++) {
            sideConfig.put(BLOCK_SIDE.values()[i], IO_SETTING.values()[serialized[SIZE / 2 + i]]);
        }
    }

    /**
     * Gets the IO setting for the given direction.
     *
     * @param direction the direction to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(Direction direction) {
        return directionConfig.get(direction);
    }

    /**
     * Gets the IO setting for the given block side.
     *
     * @param side the block side to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(BLOCK_SIDE side) {
        return sideConfig.get(side);
    }

    /**
     * Sets the given block side to the passed IO setting.
     *
     * @param side    the side on which the setting should be changed
     * @param setting the setting which should be set
     */
    public void set(BLOCK_SIDE side, IO_SETTING setting) {
        sideConfig.put(side, setting);
        directionConfig.put(getDirectionFromSide(side), setting);
    }

    /**
     * Checks if the {@link SideConfiguration} has an input set somewhere.
     *
     * @return true if there is an input, false otherwise
     */
    public boolean hasInput() {
        return sideConfig.containsValue(IO_SETTING.IN);
    }

    /**
     * Checks if the {@link SideConfiguration} has an output set somewhere.
     *
     * @return true if there is an output, false otherwise
     */
    public boolean hasOutput() {
        return sideConfig.containsValue(IO_SETTING.OUT);
    }

    /**
     * Checks if the {@link SideConfiguration} already has the maximum amount of outputs.
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
    public Direction getDirectionFromSide(BLOCK_SIDE side) {
        return switch (side) {
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
            case BACK -> facing.getOpposite();
            case FRONT -> facing;
        };
    }
}
