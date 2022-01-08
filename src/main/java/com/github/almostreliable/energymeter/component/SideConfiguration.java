package com.github.almostreliable.energymeter.component;

import com.github.almostreliable.energymeter.meter.MeterBlock;
import com.github.almostreliable.energymeter.util.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.energymeter.util.TypeEnums.IO_SETTING;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SideConfiguration implements INBTSerializable<CompoundNBT> {

    private static final int MAX_OUTPUTS = 4;
    private final EnumMap<Direction, IO_SETTING> config = new EnumMap<>(Direction.class);

    public SideConfiguration() {
        for (Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.OFF);
        }
    }

    /**
     * Gets the direction from the given block side depending on the facing of the block.
     *
     * @param state the block state
     * @param side  the block side to get the direction from
     * @return the direction
     */
    public static Direction getDirectionFromSide(BlockState state, BLOCK_SIDE side) {
        Direction facing = state.getValue(MeterBlock.FACING);
        Direction bottom = state.getValue(MeterBlock.BOTTOM);
        return facing == bottom ? horizontalConversion(facing, side) : verticalConversion(facing, bottom, side);
    }

    /**
     * Converts the given block side to a direction if the facing direction
     * is any of the 4 compass directions.
     *
     * @param facing the facing direction
     * @param side   the block side to convert
     * @return the direction
     */
    private static Direction horizontalConversion(Direction facing, BLOCK_SIDE side) {
        switch (side) {
            case TOP:
                return Direction.UP;
            case BOTTOM:
                return Direction.DOWN;
            case LEFT:
                return facing.getClockWise();
            case RIGHT:
                return facing.getCounterClockWise();
            case BACK:
                return facing.getOpposite();
            default:
                return facing;
        }
    }

    /**
     * Converts the given block side to a direction if the facing direction
     * is up or down.
     *
     * @param facing the facing direction
     * @param bottom the bottom direction
     * @param side   the block side to convert
     * @return the direction
     */
    private static Direction verticalConversion(Direction facing, Direction bottom, BLOCK_SIDE side) {
        switch (side) {
            case TOP:
                return bottom.getOpposite();
            case BOTTOM:
                return bottom;
            case LEFT:
                return facing == Direction.UP ? bottom.getClockWise() : bottom.getCounterClockWise();
            case RIGHT:
                return facing == Direction.UP ? bottom.getCounterClockWise() : bottom.getClockWise();
            case BACK:
                return facing.getOpposite();
            default:
                return facing;
        }
    }

    /**
     * Gets an IO setting by a specified direction.
     * This automatically takes the facing direction into account.
     *
     * @param direction the direction to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(Direction direction) {
        return config.get(direction);
    }

    /**
     * Gets an IO setting by a specified block side.
     * This automatically takes the facing direction into account.
     *
     * @param state the block state
     * @param side  the block side to get the IO setting from
     * @return the IO setting
     */
    public IO_SETTING get(BlockState state, BLOCK_SIDE side) {
        return config.get(getDirectionFromSide(state, side));
    }

    /**
     * Sets the specified block side to the specified IO setting.
     *
     * @param state   the block state
     * @param side    the side on which the setting should be changed
     * @param setting the setting which should be set
     */
    public void set(BlockState state, BLOCK_SIDE side, IO_SETTING setting) {
        config.put(getDirectionFromSide(state, side), setting);
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

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (Direction direction : Direction.values()) {
            nbt.putInt(direction.toString(), config.get(direction).ordinal());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        for (Direction direction : Direction.values()) {
            config.put(direction, IO_SETTING.values()[nbt.getInt(direction.toString())]);
        }
    }

    /**
     * Converts the current side configuration as string map and returns it.
     *
     * @return the side configuration as string map
     */
    public Map<String, String> asStringMap() {
        Map<String, String> stringMap = new HashMap<>();
        for (Entry<Direction, IO_SETTING> entry : config.entrySet()) {
            stringMap.put(entry.getKey().getName(), entry.getValue().name());
        }
        return stringMap;
    }
}
