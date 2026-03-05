package testmod.content;

import com.almostreliable.energymeter.network.action.TextValueClientAction;

import com.google.common.primitives.Ints;

import java.util.function.BiConsumer;

public enum EnergyMitterTextBoxType implements TextValueClientAction.ValueConsumer<EnergyEmitterBlockEntity> {
    ENERGY_TO_EMIT((be, value) -> be.setEnergyToEmitPerTick(Ints.saturatedCast(value)));

    private final BiConsumer<EnergyEmitterBlockEntity, Long> valueUpdater;

    EnergyMitterTextBoxType(BiConsumer<EnergyEmitterBlockEntity, Long> valueUpdater) {
        this.valueUpdater = valueUpdater;
    }

    @Override
    public void updateValue(EnergyEmitterBlockEntity blockEntity, long value) {
        valueUpdater.accept(blockEntity, value);
    }
}
