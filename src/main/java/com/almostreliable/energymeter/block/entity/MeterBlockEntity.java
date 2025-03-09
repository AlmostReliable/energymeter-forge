package com.almostreliable.energymeter.block.entity;

import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.component.EnergyHandler;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.core.Config;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.network.ClientSyncPacket;
import com.almostreliable.energymeter.network.SettingUpdatePacket;
import com.almostreliable.energymeter.util.TextUtils;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.IoSetting;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.Setting;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;
import com.almostreliable.energymeter.util.TypeEnums.TranslateType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import org.jetbrains.annotations.Nullable;

import static com.almostreliable.energymeter.core.Constants.DISPLAY_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.MEASURE_INTERVAL_ID;
import static com.almostreliable.energymeter.core.Constants.MEASURE_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.METER_ID;
import static com.almostreliable.energymeter.core.Constants.SIDE_CONFIG_ID;
import static com.almostreliable.energymeter.core.Constants.SyncFlags;
import static com.almostreliable.energymeter.core.Constants.TRANSFER_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.ZERO_TOLERANCE_ID;

public class MeterBlockEntity extends BlockEntity implements TickableBlockEntity, MenuProvider {

    // components
    private final IoConfig ioConfig;
    private final EnergyHandler energyHandler;

    // settings
    private DisplayMode displayMode = DisplayMode.SHORT;
    private TransferMode transferMode = TransferMode.SPLIT;
    private MeasureMode measureMode = MeasureMode.EXACT;
    private int measureInterval = Config.COMMON.defaultInterval.getAsInt();
    private int zeroTolerance = Config.COMMON.defaultInterval.getAsInt();

    // tracking & display
    private double energyRate;
    private double zeroThreshold;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public MeterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.METER_BLOCK_ENTITY.get(), pos, state);

        this.ioConfig = new IoConfig();
        this.energyHandler = new EnergyHandler(FacingEntityBlock.getFacingDir(state), ioConfig::getSetting, this::getTransferMode);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(SIDE_CONFIG_ID, ioConfig.serializeNBT(registries));
        tag.putString(DISPLAY_MODE_ID, displayMode.name());
        tag.putString(TRANSFER_MODE_ID, transferMode.name());
        tag.putString(MEASURE_MODE_ID, measureMode.name());
        tag.putInt(MEASURE_INTERVAL_ID, measureInterval);
        tag.putInt(ZERO_TOLERANCE_ID, zeroTolerance);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(SIDE_CONFIG_ID)) ioConfig.deserializeNBT(registries, tag.getCompound(SIDE_CONFIG_ID));
        if (tag.contains(DISPLAY_MODE_ID)) displayMode = DisplayMode.valueOf(tag.getString(DISPLAY_MODE_ID));
        if (tag.contains(TRANSFER_MODE_ID)) transferMode = TransferMode.valueOf(tag.getString(TRANSFER_MODE_ID));
        if (tag.contains(MEASURE_MODE_ID)) measureMode = MeasureMode.valueOf(tag.getString(MEASURE_MODE_ID));
        if (tag.contains(MEASURE_INTERVAL_ID)) measureInterval = tag.getInt(MEASURE_INTERVAL_ID);
        if (tag.contains(ZERO_TOLERANCE_ID)) zeroTolerance = tag.getInt(ZERO_TOLERANCE_ID);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        return new MeterMenu(wid, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        // TODO: replace with datagen or leave empty
        return TextUtils.translate(TranslateType.CONTAINER, METER_ID);
    }

    @Override
    public void tick(ServerLevel level) {
        if (level.getGameTime() % measureInterval != 0) {
            return;
        }

        if ((transferMode.requiresInput() && !ioConfig.hasInput()) || (transferMode.requiresOutput() && !ioConfig.hasOutput())) {
            updateStatus(ConnectionStatus.DISCONNECTED);
            return;
        }

        if (energyHandler.hasHistory()) {
            double average = energyHandler.getAverage();
            double oldEnergyRate = energyRate;
            energyRate = average / measureInterval;
            if (oldEnergyRate != energyRate) {
                syncData(SyncFlags.TRANSFER_RATE);

                if (energyRate > 0) {
                    updateStatus(ConnectionStatus.TRANSFERRING);
                } else {
                    updateStatus(ConnectionStatus.IDLE);
                }
            }

            if (measureMode == MeasureMode.INTERVAL) {
                energyHandler.resetHistory(average);
            } else {
                energyHandler.resetHistory();
            }
        }

        energyHandler.intervalReached();
    }

    @Nullable
    public IEnergyStorage getEnergyCapability(@Nullable Direction direction) {
        if (direction == null || ioConfig.getSetting(direction) == IoSetting.OFF) return null;
        return energyHandler.getEnergyStorage(direction);
    }

    /**
     * Convenience method used by the {@link SettingUpdatePacket} in order
     * to flip a specific setting after a button click on the client.
     *
     * @param setting the setting to update
     */
    public void updateSetting(Setting setting) {
        switch (setting) {
            case NUMBER -> {
                displayMode = displayMode == DisplayMode.SHORT ? DisplayMode.LONG : DisplayMode.SHORT;
                syncData(SyncFlags.NUMBER_MODE);
            }
            case MODE -> {
                transferMode = transferMode == TransferMode.TRANSFER ? TransferMode.CONSUME : TransferMode.TRANSFER;
                syncData(SyncFlags.MODE);
            }
            case ACCURACY -> {
                var flags = SyncFlags.ACCURACY;
                if (measureMode == MeasureMode.EXACT) {
                    measureMode = MeasureMode.INTERVAL;
                } else {
                    measureMode = MeasureMode.EXACT;
                    measureInterval = Config.COMMON.defaultInterval.getAsInt();
                    flags |= SyncFlags.INTERVAL;
                }
                syncData(flags);
            }
        }
    }

    /**
     * Syncs data to clients tracking the current with a {@link ClientSyncPacket}.
     * <p>
     * Different flags from the sync flags can be passed to define what should be included
     * in the packet to avoid unnecessary data being sent.
     *
     * @param flags the flags of the data to sync
     */
    public void syncData(int flags) {
        if (level == null || level.isClientSide) return;
        var packet = new ClientSyncPacket(
            worldPosition,
            flags,
            sideConfig,
            energyRate,
            displayMode,
            connectionStatus,
            transferMode,
            measureMode,
            measureInterval,
            zeroTolerance
        );
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(worldPosition), packet);
    }

    /**
     * Updates the status to the specified value.
     * If it was different from the previous value, it will trigger a client sync.
     *
     * @param newStatus the new setting to set
     */
    private void updateStatus(ConnectionStatus newStatus) {
        var oldStatus = connectionStatus;
        connectionStatus = newStatus;
        energyPerInterval = 0;
        if (oldStatus != newStatus) {
            var flags = SyncFlags.STATUS;
            if (newStatus != ConnectionStatus.TRANSFERRING) {
                energyPerIntervalHistory.clear();
                energyRate = 0;
                flags |= SyncFlags.TRANSFER_RATE;
            }
            syncData(flags);
        }
    }

    public int getThreshold() {
        return zeroTolerance;
    }

    public void setThreshold(int threshold) {
        this.zeroTolerance = threshold;
    }

    public int getMeasureInterval() {
        return measureInterval;
    }

    public void setInterval(int interval) {
        this.measureInterval = interval;
    }

    public double getEnergyRate() {
        return Math.round(energyRate * 1_000.0) / 1_000.0;
    }

    public void setEnergyRate(double transferRate) {
        this.energyRate = transferRate;
    }

    public ConnectionStatus getConnectionStatus() {
        if (connectionStatus == ConnectionStatus.TRANSFERRING) {
            return transferMode == TransferMode.CONSUME ? ConnectionStatus.CONSUMING : ConnectionStatus.TRANSFERRING;
        }
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setNumberMode(DisplayMode numberMode) {
        this.displayMode = numberMode;
    }

    public MeasureMode getMeasureMode() {
        return measureMode;
    }

    public void setAccuracy(MeasureMode accuracy) {
        this.measureMode = accuracy;
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    public void setMode(TransferMode mode) {
        this.transferMode = mode;
    }
}
