package com.almostreliable.energymeter.block.entity;

import com.almostreliable.energymeter.block.component.EnergyHandler;
import com.almostreliable.energymeter.block.component.EnergyHandlerHost;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.core.Config;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.menu.MeterMenu;
import com.almostreliable.energymeter.network.packet.EnergyRateUpdatePacket;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import org.jetbrains.annotations.Nullable;

import static com.almostreliable.energymeter.core.Constants.MEASURE_INTERVAL_ID;
import static com.almostreliable.energymeter.core.Constants.MEASURE_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.SIDE_CONFIG_ID;
import static com.almostreliable.energymeter.core.Constants.TOTAL_ENERGY_ID;
import static com.almostreliable.energymeter.core.Constants.TRANSFER_LIMIT_ID;
import static com.almostreliable.energymeter.core.Constants.TRANSFER_MODE_ID;
import static com.almostreliable.energymeter.core.Constants.ZERO_TOLERANCE_ID;

public class MeterBlockEntity extends BlockEntity implements TickableMenuBlockEntity, EnergyHandlerHost {

    public static final int TICK_TIME = 5;

    // components
    private final IoConfig ioConfig;
    private final EnergyHandler energyHandler;

    // settings
    private TransferMode transferMode = TransferMode.SPLIT;
    private MeasureMode measureMode = MeasureMode.EXACT;
    private int measureInterval = Config.COMMON.defaultInterval.getAsInt();
    private int zeroTolerance = Config.COMMON.defaultInterval.getAsInt();
    private int transferLimit = -1;

    // tracking & display
    private int tickDelay;
    private double energyRate;
    private long totalEnergy;
    private double zeroThreshold;

    // status
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public MeterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.METER_BLOCK_ENTITY.get(), pos, state);
        this.ioConfig = new IoConfig(this::onConnectionRelevantSettingChanged);
        this.energyHandler = new EnergyHandler(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(SIDE_CONFIG_ID, ioConfig.serializeNBT(registries));
        tag.putString(TRANSFER_MODE_ID, transferMode.name());
        tag.putString(MEASURE_MODE_ID, measureMode.name());
        tag.putInt(MEASURE_INTERVAL_ID, measureInterval);
        tag.putInt(ZERO_TOLERANCE_ID, zeroTolerance);
        tag.putInt(TRANSFER_LIMIT_ID, transferLimit);
        tag.putLong(TOTAL_ENERGY_ID, totalEnergy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(SIDE_CONFIG_ID)) ioConfig.deserializeNBT(registries, tag.getCompound(SIDE_CONFIG_ID));
        if (tag.contains(TRANSFER_MODE_ID)) transferMode = TransferMode.valueOf(tag.getString(TRANSFER_MODE_ID));
        if (tag.contains(MEASURE_MODE_ID)) measureMode = MeasureMode.valueOf(tag.getString(MEASURE_MODE_ID));
        if (tag.contains(MEASURE_INTERVAL_ID)) measureInterval = tag.getInt(MEASURE_INTERVAL_ID);
        if (tag.contains(ZERO_TOLERANCE_ID)) zeroTolerance = tag.getInt(ZERO_TOLERANCE_ID);
        if (tag.contains(TRANSFER_LIMIT_ID)) transferLimit = tag.getInt(TRANSFER_LIMIT_ID);
        if (tag.contains(TOTAL_ENERGY_ID)) totalEnergy = tag.getLong(TOTAL_ENERGY_ID);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // TODO: test if this should be saved or if it's random enough on load after rejoining the world
            tickDelay = (int) (TICK_TIME - (level.getGameTime() % TICK_TIME));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory playerInventory, Player player) {
        return new MeterMenu(wid, playerInventory, this);
    }

    @Override
    public void tick(ServerLevel level) {
        if (!transferMode.isCorrectlyConfigured(ioConfig::hasInput, ioConfig::hasOutput)) {
            connectionStatus = ConnectionStatus.DISCONNECTED;
            return;
        }

        if ((level.getGameTime() + tickDelay) % measureInterval == 0) {
            energyHandler.onIntervalReached();
        }

        if ((level.getGameTime() + tickDelay) % TICK_TIME == 0) {
            onTickTimeReached(level);
        }

        connectionStatus = energyRate > 0 ? transferMode.activeStatus : ConnectionStatus.IDLE;
    }

    private void onTickTimeReached(ServerLevel level) {
        if (!energyHandler.hasHistory()) return;

        var measuredEnergy = energyHandler.calculateAndRestartInterval(measureMode == MeasureMode.INTERVAL);
        double oldEnergyRate = energyRate;
        double average = measuredEnergy.average();
        energyRate = average / measureInterval;
        totalEnergy += measuredEnergy.total();

        if (oldEnergyRate != energyRate) {
            syncEnergyRate(level);
        }
    }

    private void syncEnergyRate(ServerLevel level) {
        PacketDistributor.sendToPlayersTrackingChunk(
            level,
            level.getChunk(worldPosition).getPos(),
            new EnergyRateUpdatePacket(worldPosition, energyRate)
        );
    }

    private void onConnectionRelevantSettingChanged() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        serverLevel.invalidateCapabilities(worldPosition);
        energyHandler.clear();
        energyRate = 0;
        syncEnergyRate(serverLevel);
        setChanged();
    }

    @Nullable
    public IEnergyStorage getEnergyCapability(@Nullable Direction direction) {
        if (direction == null || ioConfig.getSetting(direction).isDisabled()) return null;
        return energyHandler.getEnergyStorage(direction);
    }

    @Override
    public IoConfig getIoConfig() {
        return ioConfig;
    }

    @Override
    public TransferMode getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
        onConnectionRelevantSettingChanged();
        setChanged();
    }

    public MeasureMode getMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(MeasureMode measureMode) {
        this.measureMode = measureMode;
        setChanged();
    }

    public int getMeasureInterval() {
        return measureInterval;
    }

    public int getZeroTolerance() {
        return zeroTolerance;
    }

    public double getEnergyRate() {
        return energyRate;
    }

    public long getTotalEnergy() {
        return totalEnergy;
    }

    @Override
    public int getTransferLimit() {
        return transferLimit;
    }

    @OnlyIn(Dist.CLIENT)
    public void setEnergyRate(double energyRate) {
        this.energyRate = energyRate;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
}
