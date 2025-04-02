package testmod;

import com.almostreliable.energymeter.block.component.IoConfig.IoSetting;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;

import testmod.content.EnergyBlockEntity;

import org.jetbrains.annotations.Nullable;

public final class TestUtils {

    public static final BlockPos DEFAULT_POS = new BlockPos(1, 2, 1);

    private TestUtils() {}

    public static void assertNull(@Nullable Object o, String failureMessage) {
        if (o != null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertNotNull(@Nullable Object o, String failureMessage) {
        if (o == null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertInstanceOf(Object o, Class<?> clazz, String failureMessage) {
        if (!clazz.isInstance(o)) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static MeterBlockEntity setupMeter(GameTestHelper helper) {
        helper.setBlock(DEFAULT_POS, Registration.METER_BLOCK.get());
        return helper.getBlockEntity(DEFAULT_POS);
    }

    public static MeterWithIoResult setupMeterWithIo(GameTestHelper helper) {
        MeterBlockEntity meterBlockEntity = setupMeter(helper);

        // set io configuration, west to input, east to output
        meterBlockEntity.getIoConfig().setSetting(Direction.WEST, IoSetting.IN);
        meterBlockEntity.getIoConfig().setSetting(Direction.EAST, IoSetting.OUT);

        // place test energy blocks on both configured sides
        helper.setBlock(DEFAULT_POS.relative(Direction.WEST), TestRegistration.ENERGY_BLOCK.get());
        helper.setBlock(DEFAULT_POS.relative(Direction.EAST), TestRegistration.ENERGY_BLOCK.get());
        EnergyBlockEntity inputEnergyBlockEntity = helper.getBlockEntity(DEFAULT_POS.relative(Direction.WEST));
        EnergyBlockEntity outputEnergyBlockEntity = helper.getBlockEntity(DEFAULT_POS.relative(Direction.EAST));

        // test whether both energy blocks are empty
        IEnergyStorage inputEnergyBlockCap = inputEnergyBlockEntity.getEnergyCapability(null);
        helper.assertTrue(
            inputEnergyBlockCap != null && inputEnergyBlockCap.getEnergyStored() == 0,
            "input energy block should be empty"
        );
        IEnergyStorage outputEnergyBlockCap = outputEnergyBlockEntity.getEnergyCapability(null);
        helper.assertTrue(
            outputEnergyBlockCap != null && outputEnergyBlockCap.getEnergyStored() == 0,
            "output energy block should be empty"
        );

        assert inputEnergyBlockCap != null;
        assert outputEnergyBlockCap != null;

        return new MeterWithIoResult(
            meterBlockEntity,
            inputEnergyBlockEntity,
            inputEnergyBlockCap,
            outputEnergyBlockEntity,
            outputEnergyBlockCap
        );
    }

    public static int getRandomEnergyRate() {
        return (int) (Math.random() * 10_000);
    }

    public record MeterWithIoResult(
        MeterBlockEntity meterBlockEntity,
        EnergyBlockEntity inputEnergyBlockEntity,
        IEnergyStorage inputEnergyBlockCap,
        EnergyBlockEntity outputEnergyBlockEntity,
        IEnergyStorage outputEnergyBlockCap
    ) {}
}
