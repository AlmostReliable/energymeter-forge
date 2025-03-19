package testmod.test;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.component.ForwardingEnergyStorage;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;
import com.almostreliable.energymeter.util.TypeEnums;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import testmod.TestRegistration;
import testmod.TestUtils;
import testmod.content.EnergyBlockEntity;

@GameTestHolder(ModConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterTests {

    private static final BlockPos DEFAULT_POS = new BlockPos(1, 2, 1);

    private static MeterBlockEntity setupMeter(GameTestHelper helper) {
        helper.setBlock(DEFAULT_POS, Registration.METER_BLOCK.get());
        return helper.getBlockEntity(DEFAULT_POS);
    }

    @GameTest(template = "empty_test_structure")
    public void testMeterDefaults(GameTestHelper helper) {
        MeterBlockEntity blockEntity = setupMeter(helper);

        // test whether io configuration has correct default values
        IoConfig ioConfig = blockEntity.getIoConfig();
        helper.assertFalse(ioConfig.hasInput(), "energy meter should not have input by default");
        helper.assertFalse(ioConfig.hasOutput(), "energy meter should not have output by default");
        helper.assertFalse(ioConfig.hasChanged(), "energy meter io config should not be marked as changed by default");

        // test enum defaults
        helper.assertValueEqual(blockEntity.getDisplayMode(), DisplayMode.SHORT, "display mode");
        helper.assertValueEqual(blockEntity.getTransferMode(), TransferMode.SPLIT, "transfer mode");
        helper.assertValueEqual(blockEntity.getMeasureMode(), MeasureMode.EXACT, "measure mode");
        helper.assertValueEqual(blockEntity.getConnectionStatus(), ConnectionStatus.DISCONNECTED, "connection status");

        helper.succeed();
    }

    @GameTest(template = "empty_test_structure")
    public void testMeterEnergyCap(GameTestHelper helper) {
        MeterBlockEntity blockEntity = setupMeter(helper);

        // test capability access without io configuration
        for (Direction direction : Direction.values()) {
            IEnergyStorage energyCap = blockEntity.getEnergyCapability(direction);
            TestUtils.assertNull(energyCap, "energy meter should not expose any energy capability by default");
        }
        IEnergyStorage energyCapWithoutContext = blockEntity.getEnergyCapability(null);
        TestUtils.assertNull(energyCapWithoutContext, "energy meter should not expose the internal energy capability");

        // set io configuration, west to input, east to output
        blockEntity.getIoConfig().setSetting(Direction.WEST, TypeEnums.IoSetting.IN);
        blockEntity.getIoConfig().setSetting(Direction.EAST, TypeEnums.IoSetting.OUT);

        // test whether energy input capability exposes correct handler
        IEnergyStorage inputEnergyCap = blockEntity.getEnergyCapability(Direction.WEST);
        TestUtils.assertNotNull(inputEnergyCap, "energy meter should expose an input energy capability");
        TestUtils.assertInstanceOf(
            inputEnergyCap,
            ForwardingEnergyStorage.class,
            "energy meter input capability should be an energy handler"
        );

        // test whether energy input capability handles extraction and insertion correctly
        helper.assertFalse(inputEnergyCap.canExtract(), "energy meter input capability should not allow extraction");
        int inputEnergyExtracted = inputEnergyCap.extractEnergy(1_000, false);
        helper.assertTrue(inputEnergyExtracted == 0, "energy meter input capability should not allow extraction");
        helper.assertTrue(inputEnergyCap.canReceive(), "energy meter input capability should be able to receive energy");

        // test whether energy output capability exposes correct handler
        IEnergyStorage outputEnergyCap = blockEntity.getEnergyCapability(Direction.EAST);
        TestUtils.assertNotNull(outputEnergyCap, "energy meter should expose an output energy capability");
        TestUtils.assertInstanceOf(
            outputEnergyCap,
            ForwardingEnergyStorage.class,
            "energy meter output capability should be an energy handler"
        );

        // test whether energy output capability handles extraction and insertion correctly
        helper.assertFalse(outputEnergyCap.canExtract(), "energy meter output capability should not allow extraction");
        int outputEnergyExtracted = inputEnergyCap.extractEnergy(1_000, false);
        helper.assertTrue(outputEnergyExtracted == 0, "energy meter output capability should not allow extraction");
        helper.assertFalse(outputEnergyCap.canReceive(), "energy meter output capability should not be able to receive energy");

        helper.succeed();
    }

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = "empty_test_structure")
    public void testMeterConsumerMode(GameTestHelper helper) {
        MeterBlockEntity blockEntity = setupMeter(helper);

        // set io configuration, west to input, east to output
        blockEntity.getIoConfig().setSetting(Direction.WEST, TypeEnums.IoSetting.IN);
        blockEntity.getIoConfig().setSetting(Direction.EAST, TypeEnums.IoSetting.OUT);

        // set transfer mode to consume
        blockEntity.setTransferMode(TransferMode.CONSUME);

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

        // let input energy block emit energy towards the meter
        int energyPerTick = 1_000;
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.TICK_TIME + 1,
            () -> {
                helper.assertTrue(blockEntity.getEnergyRate() == energyPerTick, "energy rate should be equal to input energy rate");
                helper.assertTrue(
                    outputEnergyBlockCap != null && outputEnergyBlockCap.getEnergyStored() == 0,
                    "output energy block should be empty"
                );

                helper.succeed();
            }
        );
    }
}
