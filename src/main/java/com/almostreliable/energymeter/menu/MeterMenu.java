package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.component.IoConfig.IoSetting;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.network.menu.handler.DelegateDataHandler;
import com.almostreliable.energymeter.network.menu.handler.DoubleDataHandler;
import com.almostreliable.energymeter.network.menu.handler.EnumDataHandler;
import com.almostreliable.energymeter.network.menu.handler.IntegerDataHandler;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MeterMenu extends SynchronizedContainerMenu<MeterBlockEntity> {

    private final IoConfig ioConfig = new IoConfig();
    private DisplayMode displayMode = DisplayMode.SHORT;
    private TransferMode transferMode = TransferMode.SPLIT;
    private MeasureMode measureMode = MeasureMode.EXACT;
    private int measureInterval;
    private double totalEnergy;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    public MeterMenu(int wid, Inventory playerInventory, MeterBlockEntity blockEntity) {
        super(Registration.METER_MENU.get(), wid, playerInventory, blockEntity);
    }

    @Override
    public void setupDataHandlers() {
        menuSynchronizer.addDataHandler(new DelegateDataHandler(blockEntity.getIoConfig(), () -> ioConfig));
        menuSynchronizer.addDataHandler(new EnumDataHandler<>(
            blockEntity::getDisplayMode,
            v -> this.displayMode = v,
            DisplayMode.values()
        ));
        menuSynchronizer.addDataHandler(new EnumDataHandler<>(
            blockEntity::getTransferMode,
            v -> this.transferMode = v,
            TransferMode.values()
        ));
        menuSynchronizer.addDataHandler(new EnumDataHandler<>(
            blockEntity::getMeasureMode,
            v -> this.measureMode = v,
            MeasureMode.values()
        ));
        menuSynchronizer.addDataHandler(new IntegerDataHandler(blockEntity::getMeasureInterval, v -> this.measureInterval = v));
        menuSynchronizer.addDataHandler(new DoubleDataHandler(blockEntity::getTotalEnergy, v -> this.totalEnergy = v));
        menuSynchronizer.addDataHandler(new EnumDataHandler<>(
            blockEntity::getConnectionStatus,
            v -> this.connectionStatus = v,
            ConnectionStatus.values()
        ));
    }

    @Override
    public void receiveClientData(ServerPlayer player, CompoundTag data) {
        String type = data.getString("type");

        switch (type) {
            case "setting_changed" -> receiveSettingChange(data);
            case "io_setting" -> receiveIoSettingChange(data);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private void receiveSettingChange(CompoundTag data) {
        String setting = data.getString("setting");
        if (setting.equals("transfer_mode")) {
            TransferMode currentMode = blockEntity.getTransferMode();
            int newOrdinal = (currentMode.ordinal() + 1) % TransferMode.values().length;
            TransferMode newMode = TransferMode.values()[newOrdinal];
            blockEntity.setTransferMode(newMode);
        }
    }

    private void receiveIoSettingChange(CompoundTag data) {
        Direction direction = Direction.values()[data.getInt("direction")];

        if (data.contains("setting")) {
            IoSetting setting = IoSetting.deserialize(data.getCompound("setting"));
            blockEntity.getIoConfig().setSetting(direction, setting);
            return;
        }

        boolean reverse = data.getBoolean("reverse");
        boolean shift = data.getBoolean("shift");

        if (shift) {
            blockEntity.getIoConfig().resetSetting(direction);
        } else {
            blockEntity.getIoConfig().cycleSetting(direction, reverse);
        }
    }

    public BlockState getBlockState() {
        return blockEntity.getBlockState();
    }

    public double getEnergyRate() {
        return blockEntity.getEnergyRate();
    }

    // region syncing client getters
    @OnlyIn(Dist.CLIENT)
    public IoSetting getIoSetting(Direction direction) {
        return ioConfig.getSetting(direction);
    }

    @OnlyIn(Dist.CLIENT)
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @OnlyIn(Dist.CLIENT)
    public TransferMode getTransferMode() {
        return transferMode;
    }

    @OnlyIn(Dist.CLIENT)
    public MeasureMode getMeasureMode() {
        return measureMode;
    }

    @OnlyIn(Dist.CLIENT)
    public double getMeasureInterval() {
        return measureInterval;
    }

    @OnlyIn(Dist.CLIENT)
    public double getTotalEnergy() {
        return totalEnergy;
    }

    @OnlyIn(Dist.CLIENT)
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
    // endregion
}
