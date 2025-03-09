package com.almostreliable.energymeter.menu;

import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.network.menu.handler.DelegateDataHandler;
import com.almostreliable.energymeter.network.menu.handler.EnumDataHandler;
import com.almostreliable.energymeter.network.menu.handler.IntegerDataHandler;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MeterMenu extends SynchronizedContainerMenu<MeterBlockEntity> {

    private final IoConfig ioConfig;
    private DisplayMode displayMode = DisplayMode.SHORT;
    private TransferMode transferMode = TransferMode.SPLIT;
    private MeasureMode measureMode = MeasureMode.EXACT;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    private int measureInterval;

    public MeterMenu(int wid, Inventory playerInventory, MeterBlockEntity blockEntity) {
        super(Registration.METER_MENU.get(), wid, playerInventory, blockEntity);
        this.ioConfig = new IoConfig();
        setupSyncing();
    }

    private void setupSyncing() {
        menuSynchronizer.addDataHandler(new DelegateDataHandler(blockEntity.getIoConfig(), ioConfig));
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
        menuSynchronizer.addDataHandler(new EnumDataHandler<>(
            blockEntity::getConnectionStatus,
            v -> this.connectionStatus = v,
            ConnectionStatus.values()
        ));
        menuSynchronizer.addDataHandler(new IntegerDataHandler(blockEntity::getMeasureInterval, v -> this.measureInterval = v));
    }

    // region syncing client getters
    @OnlyIn(Dist.CLIENT)
    public IoConfig getIoConfig() {
        return ioConfig;
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
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @OnlyIn(Dist.CLIENT)
    public double getMeasureInterval() {
        return measureInterval;
    }
    // endregion
}
