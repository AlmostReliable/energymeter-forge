package com.github.almostreliable.energymeter.meter;

import com.github.almostreliable.energymeter.compat.CapabilityAdapterFactory;
import com.github.almostreliable.energymeter.compat.ICapabilityAdapter;
import com.github.almostreliable.energymeter.compat.cct.MeterPeripheral;
import com.github.almostreliable.energymeter.component.IMeter;
import com.github.almostreliable.energymeter.component.SideConfiguration;
import com.github.almostreliable.energymeter.component.SidedEnergyStorage;
import com.github.almostreliable.energymeter.core.Setup.Tiles;
import com.github.almostreliable.energymeter.network.ClientSyncPacket;
import com.github.almostreliable.energymeter.network.PacketHandler;
import com.github.almostreliable.energymeter.network.SettingUpdatePacket;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

import static com.github.almostreliable.energymeter.core.Constants.*;

public class MeterTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IMeter {

    public static final int REFRESH_RATE = 5;
    private final EnumMap<Direction, LazyOptional<IEnergyStorage>> outputCache = new EnumMap<>(Direction.class);
    private final List<LazyOptional<SidedEnergyStorage>> energyStorage;
    private final SideConfiguration sideConfig;
    private final List<Double> energyRates = Collections.synchronizedList(new ArrayList<>());
    private final Set<IMeterTileObserver> observers = Collections.synchronizedSet(new HashSet<>());
    @Nullable
    private final ICapabilityAdapter<MeterPeripheral> meterPeripheral;
    private boolean hasValidInput;
    private boolean setupDone;
    private LazyOptional<IEnergyStorage> inputCache;
    private double transferRate;
    private double averageRate;
    private NUMBER_MODE numberMode = NUMBER_MODE.SHORT;
    private STATUS status = STATUS.DISCONNECTED;
    private MODE mode = MODE.TRANSFER;
    private ACCURACY accuracy = ACCURACY.EXACT;
    private int interval = REFRESH_RATE;
    private int threshold = REFRESH_RATE;
    private double zeroThreshold;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public MeterTile() {
        super(Tiles.METER.get());
        energyStorage = SidedEnergyStorage.create(this);
        sideConfig = new SideConfiguration();
        meterPeripheral = CapabilityAdapterFactory.createMeterPeripheral(this);
    }

    /**
     * Handles the actual energy transfer process.
     * <p>
     * Automatically checks if the energy to transfer can be accepted by the possible outputs.
     * It will try to equally distribute it.
     *
     * @param energy  the energy to transfer
     * @param outputs the possible outputs
     * @return the accepted amount of energy
     */
    private static int transferEnergy(int energy, Map<IEnergyStorage, Integer> outputs) {
        int acceptedEnergy = 0;
        int energyToTransfer = energy;
        while (!outputs.isEmpty() && energyToTransfer >= outputs.size()) {
            int equalSplit = energyToTransfer / outputs.size();
            Collection<IEnergyStorage> outputsToRemove = new ArrayList<>();

            for (Entry<IEnergyStorage, Integer> output : outputs.entrySet()) {
                int actualSplit = equalSplit;
                if (output.getValue() < equalSplit) {
                    actualSplit = output.getValue();
                    outputsToRemove.add(output.getKey());
                }
                output.getKey().receiveEnergy(actualSplit, false);
                energyToTransfer -= actualSplit;
                acceptedEnergy += actualSplit;
            }

            outputsToRemove.forEach(outputs::remove);
        }

        return acceptedEnergy;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public double getTransferRate() {
        return Math.round(transferRate * 1_000.0) / 1_000.0;
    }

    public void setTransferRate(double transferRate) {
        this.transferRate = transferRate;
    }

    public STATUS getStatus() {
        if (status == STATUS.TRANSFERRING) {
            return mode == MODE.CONSUMER ? STATUS.CONSUMING : STATUS.TRANSFERRING;
        }
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public NUMBER_MODE getNumberMode() {
        return numberMode;
    }

    public void setNumberMode(NUMBER_MODE numberMode) {
        this.numberMode = numberMode;
    }

    public ACCURACY getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(ACCURACY accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Convenience method used by the {@link SettingUpdatePacket} in order
     * to flip a specific setting after a button click on the client.
     *
     * @param setting the setting to update
     */
    public void updateSetting(SETTING setting) {
        switch (setting) {
            case NUMBER:
                numberMode = numberMode == NUMBER_MODE.SHORT ? NUMBER_MODE.LONG : NUMBER_MODE.SHORT;
                syncData(SYNC_FLAGS.NUMBER_MODE);
                break;
            case MODE:
                mode = mode == MODE.TRANSFER ? MODE.CONSUMER : MODE.TRANSFER;
                syncData(SYNC_FLAGS.MODE);
                break;
            case ACCURACY:
                int flags = SYNC_FLAGS.ACCURACY;
                if (accuracy == ACCURACY.EXACT) {
                    accuracy = ACCURACY.INTERVAL;
                } else {
                    accuracy = ACCURACY.EXACT;
                    interval = REFRESH_RATE;
                    flags |= SYNC_FLAGS.INTERVAL;
                }
                syncData(flags);
                break;
        }
    }

    /**
     * Syncs data to clients that track the current {@link Chunk} with a {@link ClientSyncPacket}.
     * <p>
     * Different flags from the {@link SYNC_FLAGS} can be passed to define what should be included
     * in the packet to avoid unnecessary data being sent.
     *
     * @param flags the flags of the data to sync
     */
    public void syncData(int flags) {
        if (level == null || level.isClientSide) return;
        ClientSyncPacket packet = new ClientSyncPacket(worldPosition,
            flags,
            sideConfig,
            transferRate,
            numberMode,
            status,
            mode,
            accuracy,
            interval,
            threshold
        );
        PacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
            packet
        );

        for (IMeterTileObserver observer : observers) {
            observer.onMeterTileChanged(this, flags);
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
        if (nbt.contains(NUMBER_MODE_ID)) numberMode = NUMBER_MODE.values()[nbt.getInt(NUMBER_MODE_ID)];
        if (nbt.contains(MODE_ID)) mode = MODE.values()[nbt.getInt(MODE_ID)];
        if (nbt.contains(ACCURACY_ID)) accuracy = ACCURACY.values()[nbt.getInt(ACCURACY_ID)];
        if (nbt.contains(INTERVAL_ID)) interval = nbt.getInt(INTERVAL_ID);
        if (nbt.contains(THRESHOLD_ID)) threshold = nbt.getInt(THRESHOLD_ID);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        nbt.putInt(NUMBER_MODE_ID, numberMode.ordinal());
        nbt.putInt(MODE_ID, mode.ordinal());
        nbt.putInt(ACCURACY_ID, accuracy.ordinal());
        nbt.putInt(INTERVAL_ID, interval);
        nbt.putInt(THRESHOLD_ID, threshold);
        return super.save(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        nbt.putDouble(TRANSFER_RATE_ID, transferRate);
        nbt.putInt(STATUS_ID, status.ordinal());
        nbt.putInt(NUMBER_MODE_ID, numberMode.ordinal());
        nbt.putInt(MODE_ID, mode.ordinal());
        nbt.putInt(ACCURACY_ID, accuracy.ordinal());
        nbt.putInt(INTERVAL_ID, interval);
        nbt.putInt(THRESHOLD_ID, threshold);
        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
        transferRate = nbt.getDouble(TRANSFER_RATE_ID);
        status = STATUS.values()[nbt.getInt(STATUS_ID)];
        numberMode = NUMBER_MODE.values()[nbt.getInt(NUMBER_MODE_ID)];
        mode = MODE.values()[nbt.getInt(MODE_ID)];
        accuracy = ACCURACY.values()[nbt.getInt(ACCURACY_ID)];
        interval = nbt.getInt(INTERVAL_ID);
        threshold = nbt.getInt(THRESHOLD_ID);
    }

    @Override
    public int receiveEnergy(int energy, boolean simulate) {
        if (level == null || !setupDone) return 0;

        // void the energy if consumer mode is activated
        if (mode == MODE.CONSUMER) {
            if (!simulate) {
                averageRate += energy;
            }
            return energy;
        }

        // create a map with all possible outputs and their energy limit
        Map<IEnergyStorage, Integer> outputs = getPossibleOutputs(energy);
        if (outputs.isEmpty()) return 0;

        // get the maximum energy which could be accepted by all outputs
        int maximumAccepted = outputs.values().stream().mapToInt(maxEnergy -> maxEnergy).sum();

        // if simulated, just check if the energy fits somewhere
        if (simulate) return Math.min(maximumAccepted, energy);

        // actual energy transfer
        int acceptedEnergy;
        if (maximumAccepted <= energy) {
            // if maximum accepted energy is less or equal the energy to transfer, fill all outputs with their maximum
            outputs.forEach((cap, integer) -> cap.receiveEnergy(integer, false));
            acceptedEnergy = maximumAccepted;
        } else {
            // otherwise, push the energy to all possible outputs equally
            acceptedEnergy = transferEnergy(energy, outputs);
        }

        // adjust data for calculation in tick method
        averageRate += acceptedEnergy;

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

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    /**
     * Checks each output direction whether there is a valid energy capability.
     * It will simulate an energy transfer to this capability to make sure it can
     * accept energy and to retrieve the energy limit.
     *
     * @return a map of all possible outputs with their corresponding energy limit
     */
    private Map<IEnergyStorage, Integer> getPossibleOutputs(int energy) {
        Map<IEnergyStorage, Integer> outputs = new HashMap<>();
        for (Direction direction : Direction.values()) {
            // only consider sides where output mode is enabled
            if (sideConfig.get(direction) != IO_SETTING.OUT) continue;

            // try to get the energy capability from the cache, otherwise store it
            LazyOptional<IEnergyStorage> target = getOutputFromCache(direction);
            if (target == null) continue;

            // store the maximum amount of energy each possible output can receive
            target.ifPresent(cap -> {
                int accepted = cap.receiveEnergy(energy, true);
                if (accepted > 0) outputs.put(cap, accepted);
            });
        }
        return outputs;
    }

    @Nullable
    private LazyOptional<IEnergyStorage> getOutputFromCache(Direction direction) {
        assert level != null && !level.isClientSide;

        LazyOptional<IEnergyStorage> target = outputCache.get(direction);
        if (target == null) {
            ICapabilityProvider provider = level.getBlockEntity(worldPosition.relative(direction));
            if (provider == null || provider instanceof MeterTile) return null;
            target = provider.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
            outputCache.put(direction, target);
            target.addListener(self -> outputCache.put(direction, null));
        }
        return target;
    }

    /**
     * Updates the neighbor blocks of the {@link TileEntity}.
     * Can be useful to connect cables.
     */
    public void updateNeighbors() {
        if (level == null || level.isClientSide) return;
        level.setBlock(worldPosition, flipBlockState(), BlockFlags.NOTIFY_NEIGHBORS | BlockFlags.RERENDER_MAIN_THREAD);
    }

    /**
     * Flips the IO {@link BlockState} value and returns the new {@link BlockState}.
     *
     * @return the {@link BlockState} with the flipped IO value
     */
    private BlockState flipBlockState() {
        BlockState state = getBlockState();
        return state.setValue(MeterBlock.IO, !state.getValue(MeterBlock.IO));
    }

    @Override
    protected void invalidateCaps() {
        for (LazyOptional<SidedEnergyStorage> cap : energyStorage) {
            cap.invalidate();
        }

        if (meterPeripheral != null) {
            meterPeripheral.getLazyAdapter().invalidate();
        }

        super.invalidateCaps();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction direction) {
        if (!remove) {
            if (cap.equals(CapabilityEnergy.ENERGY) && direction != null &&
                sideConfig.get(direction) != IO_SETTING.OFF) {
                return energyStorage.get(direction.ordinal()).cast();
            }
            if (meterPeripheral != null && meterPeripheral.isCapability(cap)) {
                return meterPeripheral.getLazyAdapter().cast();
            }
        }

        return super.getCapability(cap, direction);
    }

    @Override
    public ITextComponent getDisplayName() {
        return TextUtils.translate(TRANSLATE_TYPE.CONTAINER, METER_ID);
    }

    @Nullable
    @Override
    public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity player) {
        return new MeterContainer(this, windowID);
    }

    @Override
    public void setRemoved() {
        for (IMeterTileObserver observer : observers) {
            observer.onMeterTileRemoved(this);
        }
        super.setRemoved();
    }

    /**
     * Adds a new observer to the list of observers.
     * <p>
     * Observers are CCT components that are notified about specific events.
     *
     * @param observer the observer to add
     */
    public void subscribe(IMeterTileObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from the list of observers.
     * <p>
     * Observers are CCT components that are notified about specific events.
     *
     * @param observer the observer to remove
     */
    public void unsubscribe(IMeterTileObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) return;
        if ((thresholdReached() || intervalReached()) && !energyRates.isEmpty()) calculateTransferRate();
        if (level.getGameTime() % REFRESH_RATE != 0) return;

        // initial setup
        if (!setupDone) {
            for (Direction direction : Direction.values()) {
                if (sideConfig.get(direction) != IO_SETTING.OFF) updateCache(direction);
            }
            setupDone = true;
        }

        // if not properly connected or configured, set to disconnected
        if ((mode == MODE.CONSUMER && !hasValidInput) ||
            (mode == MODE.TRANSFER && (!hasValidInput || !sideConfig.hasOutput() || !hasValidOutput()))) {
            updateStatus(STATUS.DISCONNECTED);
            return;
        }

        energyRates.add(averageRate);
        averageRate = 0;
        calculateThreshold();

        if (transferRate > 0) {
            updateStatus(STATUS.TRANSFERRING);
        } else {
            updateStatus(STATUS.CONNECTED);
        }
    }

    private boolean thresholdReached() {
        return energyRates.size() * REFRESH_RATE >= threshold && zeroThreshold == 0;
    }

    private boolean intervalReached() {
        assert level != null;
        return level.getGameTime() % interval == 0;
    }

    /**
     * Calculates the flow rate depending on the energy received within the specified interval.
     * Updates the status accordingly.
     */
    private void calculateTransferRate() {
        assert level != null && !level.isClientSide;

        double oldTransferRate = transferRate;
        double average = energyRates.stream().mapToDouble(Double::valueOf).average().orElse(0);
        transferRate = average / REFRESH_RATE;
        if (oldTransferRate != transferRate) syncData(SYNC_FLAGS.TRANSFER_RATE);

        energyRates.clear();
        if (accuracy == ACCURACY.INTERVAL) energyRates.add(average);
    }

    /**
     * Updates the cached input and output values depending on the {@link Direction}.
     * This ensures that the current status is always up-to-date.
     *
     * @param direction the {@link Direction} to update the cache for
     */
    public void updateCache(Direction direction) {
        if (level == null || level.isClientSide) return;

        IO_SETTING setting = sideConfig.get(direction);
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

    /**
     * Checks if the output cache has at least one output which is still valid.
     *
     * @return true if there is at least one valid output, false otherwise
     */
    private boolean hasValidOutput() {
        return outputCache.values().stream().anyMatch(Objects::nonNull);
    }

    /**
     * Updates the status to the specified value.
     * If it was different from the previous value, it will trigger a client sync.
     *
     * @param newStatus the new setting to set
     */
    private void updateStatus(STATUS newStatus) {
        STATUS oldStatus = status;
        status = newStatus;
        averageRate = 0;
        if (oldStatus != newStatus) {
            int flags = SYNC_FLAGS.STATUS;
            if (newStatus != STATUS.TRANSFERRING) {
                energyRates.clear();
                transferRate = 0;
                flags |= SYNC_FLAGS.TRANSFER_RATE;
            }
            syncData(flags);
        }
    }

    private void calculateThreshold() {
        long skips = Math.max(0, energyRates.size() * REFRESH_RATE - threshold);
        zeroThreshold = energyRates.stream().skip(skips).reduce(0.0, Double::sum);
    }

    /**
     * Tries to get the input capability from cache, otherwise it will try to get it.
     * This features a workaround for the mod Pipez since it doesn't expose a Tile Entity
     * on the input pipe and thus a capability provider can't be received.
     *
     * @return True if a valid input was found, false otherwise
     */
    private boolean getInputFromCache(Direction direction) {
        assert level != null && !level.isClientSide;

        LazyOptional<IEnergyStorage> target = inputCache;
        if (target == null) {
            ICapabilityProvider provider = level.getBlockEntity(worldPosition.relative(direction));
            if (provider instanceof MeterTile) return false;
            if (provider == null) {
                Block block = level.getBlockState(worldPosition.relative(direction)).getBlock();
                return !block.is(Blocks.AIR) && block.getRegistryName() != null &&
                    block.getRegistryName().getNamespace().equals(PIPEZ_ID);
            }
            target = provider.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
            inputCache = target;
            target.addListener(self -> inputCache = null);
        }

        return true;
    }
}
