package dev.rlnt.energymeter.meter;

import static dev.rlnt.energymeter.core.Constants.*;

import dev.rlnt.energymeter.core.Setup;
import dev.rlnt.energymeter.energy.ISidedEnergy;
import dev.rlnt.energymeter.energy.SidedEnergyStorage;
import dev.rlnt.energymeter.network.SettingUpdatePacket;
import dev.rlnt.energymeter.util.TextUtils;
import dev.rlnt.energymeter.util.TypeEnums.*;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class MeterTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider, ISidedEnergy {

    private static final int REFRESH_RATE = 5;
    private final EnumMap<Direction, LazyOptional<IEnergyStorage>> outputCache = new EnumMap<>(Direction.class);
    private final List<LazyOptional<SidedEnergyStorage>> energyStorage;
    private final SideConfiguration sideConfig;
    private boolean hasValidInput = false;
    private boolean setupDone = false;
    private LazyOptional<IEnergyStorage> inputCache = null;
    private float transferRate = 0;
    private int averageRate = 0;
    private int lastAverageRate = 0;
    private int averageCount = 0;
    private STATUS status = STATUS.DISCONNECTED;
    private NUMBER_MODE numberMode = NUMBER_MODE.SHORT;
    private MODE mode = MODE.TRANSFER;

    public MeterTile() {
        super(Setup.Tiles.METER_TILE.get());
        energyStorage = SidedEnergyStorage.create(this);
        sideConfig = new SideConfiguration();
    }

    /**
     * Flips the IO {@link BlockState} value and returns the new {@link BlockState}.
     * @return the {@link BlockState} with the flipped IO value
     */
    private BlockState flipBlockState() {
        final BlockState state = getBlockState();
        return state.setValue(MeterBlock.IO, !state.getValue(MeterBlock.IO));
    }

    /**
     * Updates the cached input and output values depending on the {@link Direction}.
     * This ensures that the current status is always up-to-date.
     * @param direction the {@link Direction} to update the cache for
     */
    public void updateCache(final Direction direction) {
        if (level == null || level.isClientSide) return;

        final IO_SETTING setting = sideConfig.get(direction);
        if (setting == IO_SETTING.IN) {
            hasValidInput = getInputFromCache(direction);
        } else if (setting == IO_SETTING.OUT) {
            getOutputFromCache(direction);
        }
        if (!sideConfig.hasInput()) {
            hasValidInput = false;
            inputCache = null;
        }
    }

    public float getTransferRate() {
        return transferRate;
    }

    public STATUS getStatus() {
        if (status != STATUS.TRANSFERRING) {
            return status;
        } else {
            return mode == MODE.CONSUMER ? STATUS.CONSUMING : STATUS.TRANSFERRING;
        }
    }

    public NUMBER_MODE getNumberMode() {
        return numberMode;
    }

    /**
     * Convenience method used by the {@link SettingUpdatePacket} in order
     * to flip a specific setting after a button click on the client.
     * @param setting the setting to update
     */
    public void updateSetting(final SETTING setting) {
        if (setting == SETTING.NUMBER) {
            numberMode = numberMode == NUMBER_MODE.SHORT ? NUMBER_MODE.LONG : NUMBER_MODE.SHORT;
        } else if (setting == SETTING.MODE) {
            mode = mode == MODE.TRANSFER ? MODE.CONSUMER : MODE.TRANSFER;
        }
    }

    @Override
    public void load(final BlockState state, final CompoundNBT nbt) {
        super.load(state, nbt);
        sideConfig.deserialize(nbt.getIntArray(SIDE_CONFIG_ID));
        numberMode = NUMBER_MODE.values()[nbt.getInt(NUMBER_MODE_ID)];
        mode = MODE.values()[nbt.getInt(MODE_ID)];
    }

    @Override
    public CompoundNBT save(final CompoundNBT nbt) {
        nbt.putIntArray(SIDE_CONFIG_ID, sideConfig.serialize());
        nbt.putInt(NUMBER_MODE_ID, numberMode.ordinal());
        nbt.putInt(MODE_ID, mode.ordinal());
        return super.save(nbt);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, -1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        final CompoundNBT nbt = super.getUpdateTag();
        nbt.putIntArray(SIDE_CONFIG_ID, sideConfig.serialize());
        nbt.putFloat(TRANSFER_RATE_ID, transferRate);
        nbt.putInt(STATUS_ID, status.ordinal());
        nbt.putInt(NUMBER_MODE_ID, numberMode.ordinal());
        nbt.putInt(MODE_ID, mode.ordinal());
        return nbt;
    }

    @Override
    public void setRemoved() {
        for (final LazyOptional<SidedEnergyStorage> cap : energyStorage) {
            cap.invalidate();
        }
        super.setRemoved();
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet) {
        handleUpdateTag(Objects.requireNonNull(level).getBlockState(packet.getPos()), packet.getTag());
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT nbt) {
        sideConfig.deserialize(nbt.getIntArray(SIDE_CONFIG_ID));
        transferRate = nbt.getFloat(TRANSFER_RATE_ID);
        status = STATUS.values()[nbt.getInt(STATUS_ID)];
        numberMode = NUMBER_MODE.values()[nbt.getInt(NUMBER_MODE_ID)];
        mode = MODE.values()[nbt.getInt(MODE_ID)];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        sideConfig.setFacing(getBlockState().getValue(MeterBlock.HORIZONTAL_FACING));
        update(false);
    }

    @Override
    public int receiveEnergy(final int energy, final boolean simulate) {
        if (level == null || level.isClientSide) return energy;

        // void the energy if consumer mode is activated
        if (mode == MODE.CONSUMER) {
            if (!simulate) {
                averageRate += energy;
                averageCount++;
            }
            return energy;
        }

        // build a list with all valid outputs
        final List<IEnergyStorage> outputs = getPossibleOutputs();

        // if simulated try to push to any valid output
        if (simulate) {
            int accepted = 0;
            for (final IEnergyStorage cap : outputs) {
                accepted += cap.receiveEnergy(energy - accepted, true);
                if (energy <= accepted) return energy;
            }
            return accepted;
        }

        // try to equally push the energy to all valid outputs
        final int acceptedEnergy = transferEnergy(energy, outputs);

        // adjust data for calculation in tick method
        averageRate += acceptedEnergy;
        averageCount++;

        return acceptedEnergy;
    }

    @Override
    public SideConfiguration getSideConfig() {
        return sideConfig;
    }

    @Override
    public MODE getMode() {
        return mode;
    }

    /**
     * Handles the actual energy transfer process.
     *
     * Automatically checks if the energy to transfer can be accepted by the possible outputs.
     * It will try to equally distribute it.
     *
     * @param energy the energy to transfer
     * @param outputs the possible outputs
     * @return the accepted amount of energy
     */
    private int transferEnergy(final int energy, final List<IEnergyStorage> outputs) {
        int acceptedEnergy = 0;
        final Map<IEnergyStorage, Integer> maxOutputRates = new HashMap<>();

        // store the maximum amount of energy each possible output can receive
        for (final IEnergyStorage cap : outputs) {
            final int helper = cap.receiveEnergy(energy, true);
            maxOutputRates.put(cap, helper);
            acceptedEnergy += helper;
        }

        if (acceptedEnergy <= energy) {
            // if the possible accepted energy is less than the energy to transfer, fill all outputs with their cap
            maxOutputRates.keySet().forEach(cap -> cap.receiveEnergy(maxOutputRates.get(cap), false));
        } else {
            // push the energy to all possible outputs equally
            int energyToTransfer = energy;
            while (!outputs.isEmpty() && energyToTransfer >= outputs.size()) {
                final int split = energyToTransfer / outputs.size();

                final List<IEnergyStorage> outputsToRemove = new ArrayList<>();
                for (final IEnergyStorage cap : outputs) {
                    int actualSplit = split;
                    final int maxOutputRate = maxOutputRates.get(cap);
                    if (maxOutputRate < split) {
                        actualSplit = maxOutputRate;
                        outputsToRemove.add(cap);
                    }
                    cap.receiveEnergy(actualSplit, false);
                    energyToTransfer -= actualSplit;
                    acceptedEnergy += actualSplit;
                }
                outputs.removeAll(outputsToRemove);
            }
        }

        return acceptedEnergy;
    }

    /**
     * Initiates a block update to sync data between server and client.
     * @param updateNeighbors if true, it will also update the neighbor blocks and rerender
     */
    public void update(final boolean updateNeighbors) {
        if (level == null || level.isClientSide) return;
        if (updateNeighbors) level.setBlock(
            worldPosition,
            flipBlockState(),
            BlockFlags.NOTIFY_NEIGHBORS | BlockFlags.RERENDER_MAIN_THREAD
        );
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), BlockFlags.BLOCK_UPDATE);
    }

    /**
     * Checks each output direction whether there is a valid energy capability
     * and if it can receive energy.
     * @return a list of all possible outputs
     */
    private List<IEnergyStorage> getPossibleOutputs() {
        assert level != null && !level.isClientSide;

        final List<IEnergyStorage> outputs = new ArrayList<>();
        for (final Direction direction : Direction.values()) {
            // only count sides where output mode is enabled
            if (sideConfig.get(direction) != IO_SETTING.OUT) continue;

            // try to get the energy capability from the cache, otherwise store it
            final LazyOptional<IEnergyStorage> target = getOutputFromCache(direction);
            if (target == null) continue;

            // check if the tile entity on the current side accepts energy and add it to valid outputs
            target.ifPresent(cap -> {
                if (cap.receiveEnergy(1, true) == 1) outputs.add(cap);
            });
        }

        return outputs;
    }

    /**
     * Tries to get the input capability from cache, otherwise it will try to get it.
     * This features a workaround for the mod Pipez since it doesn't expose a Tile Entity
     * on the input pipe and thus a capability provider can't be received.
     * @return True if a valid input was found, false otherwise
     */
    private boolean getInputFromCache(final Direction direction) {
        assert level != null && !level.isClientSide;

        LazyOptional<IEnergyStorage> target = inputCache;
        if (target == null) {
            final ICapabilityProvider provider = level.getBlockEntity(worldPosition.relative(direction));
            if (provider instanceof MeterTile) return false;
            if (provider == null) {
                final Block block = level.getBlockState(worldPosition.relative(direction)).getBlock();
                return (
                    !block.is(Blocks.AIR) &&
                    block.getRegistryName() != null &&
                    block.getRegistryName().getNamespace().equals(PIPEZ_ID)
                );
            } else {
                target = provider.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
                inputCache = target;
                target.addListener(self -> inputCache = null);
            }
        }

        return true;
    }

    @Nullable
    private LazyOptional<IEnergyStorage> getOutputFromCache(final Direction direction) {
        assert level != null && !level.isClientSide;

        LazyOptional<IEnergyStorage> target = outputCache.get(direction);
        if (target == null) {
            final ICapabilityProvider provider = level.getBlockEntity(worldPosition.relative(direction));
            if (provider == null || provider instanceof MeterTile) return null;
            target = provider.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
            outputCache.put(direction, target);
            target.addListener(self -> outputCache.put(direction, null));
        }
        return target;
    }

    @Override
    protected void invalidateCaps() {
        for (final LazyOptional<SidedEnergyStorage> cap : energyStorage) {
            cap.invalidate();
        }
        super.invalidateCaps();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, @Nullable final Direction direction) {
        if (
            !remove &&
            cap == CapabilityEnergy.ENERGY &&
            direction != null &&
            sideConfig.get(direction) != IO_SETTING.OFF
        ) {
            return energyStorage.get(direction.ordinal()).cast();
        }
        return super.getCapability(cap, direction);
    }

    @Override
    public ITextComponent getDisplayName() {
        return TextUtils.translate(TRANSLATE_TYPE.CONTAINER, METER_ID);
    }

    @Nullable
    @Override
    public Container createMenu(final int windowID, final PlayerInventory inventory, final PlayerEntity player) {
        return new MeterContainer(windowID, this);
    }

    /**
     * Updates the connection to the specified value.
     * If it was different from the previous value, it will trigger a block update.
     * @param newStatus the new setting to set
     */
    private void updateConnection(final STATUS newStatus) {
        final STATUS oldStatus = status;
        status = newStatus;
        averageRate = 0;
        averageCount = 0;
        if (oldStatus != newStatus) {
            if (newStatus != STATUS.TRANSFERRING) transferRate = 0;
            update(false);
        }
    }

    /**
     * Checks if the output cache has at least one output which is still valid.
     * @return true if there is at least one valid output, false otherwise
     */
    private boolean hasValidOutput() {
        for (final LazyOptional<IEnergyStorage> cap : outputCache.values()) {
            if (cap != null) return true;
        }
        return false;
    }

    /**
     * Calculates the flow rate depending on the energy received within {@value REFRESH_RATE} ticks.
     * Updates the connection status accordingly.
     */
    private void calculateFlow() {
        if (averageCount != 0) {
            transferRate = (float) averageRate / averageCount;
            update(false);
        }

        if (transferRate > 0) {
            updateConnection(STATUS.TRANSFERRING);
        } else {
            updateConnection(STATUS.CONNECTED);
        }

        lastAverageRate = averageRate;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide || level.getGameTime() % REFRESH_RATE != 0) return;

        // initial setup
        if (!setupDone) {
            for (final Direction direction : Direction.values()) {
                if (sideConfig.get(direction) != IO_SETTING.OFF) updateCache(direction);
            }
            setupDone = true;
        }

        // if not properly connected or configured, set to disconnected
        if (
            (mode == MODE.CONSUMER && !hasValidInput) ||
            (mode == MODE.TRANSFER && (!hasValidInput || !sideConfig.hasOutput() || !hasValidOutput()))
        ) {
            updateConnection(STATUS.DISCONNECTED);
            return;
        }

        // if the average rate didn't change, set to connected
        if (averageRate == lastAverageRate) {
            updateConnection(STATUS.CONNECTED);
            return;
        }

        calculateFlow();
    }
}
