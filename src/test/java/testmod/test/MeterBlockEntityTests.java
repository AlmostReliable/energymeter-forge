package testmod.test;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.component.ForwardingEnergyStorage;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.util.TypeEnums;
import com.almostreliable.energymeter.util.TypeEnums.ConnectionStatus;
import com.almostreliable.energymeter.util.TypeEnums.DisplayMode;
import com.almostreliable.energymeter.util.TypeEnums.MeasureMode;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import testmod.TestUtils;

@GameTestHolder(ModConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterBlockEntityTests {

    @GameTest(template = "empty_test_structure")
    public void meterDefaults(GameTestHelper helper) {
        MeterBlockEntity meterBlockEntity = TestUtils.setupMeter(helper);

        // test whether io configuration has correct default values
        IoConfig ioConfig = meterBlockEntity.getIoConfig();
        helper.assertFalse(ioConfig.hasInput(), "energy meter should not have input by default");
        helper.assertFalse(ioConfig.hasOutput(), "energy meter should not have output by default");
        helper.assertFalse(ioConfig.hasChanged(), "energy meter io config should not be marked as changed by default");

        // test enum defaults
        helper.assertValueEqual(meterBlockEntity.getDisplayMode(), DisplayMode.SHORT, "display mode");
        helper.assertValueEqual(meterBlockEntity.getTransferMode(), TransferMode.SPLIT, "transfer mode");
        helper.assertValueEqual(meterBlockEntity.getMeasureMode(), MeasureMode.EXACT, "measure mode");
        helper.assertValueEqual(meterBlockEntity.getConnectionStatus(), ConnectionStatus.DISCONNECTED, "connection status");

        helper.succeed();
    }

    @GameTest(template = "empty_test_structure")
    public void meterEnergyCap(GameTestHelper helper) {
        MeterBlockEntity meterBlockEntity = TestUtils.setupMeter(helper);

        // test capability access without io configuration
        for (Direction direction : Direction.values()) {
            IEnergyStorage energyCap = meterBlockEntity.getEnergyCapability(direction);
            TestUtils.assertNull(energyCap, "energy meter should not expose any energy capability by default");
        }
        IEnergyStorage energyCapWithoutContext = meterBlockEntity.getEnergyCapability(null);
        TestUtils.assertNull(energyCapWithoutContext, "energy meter should not expose the internal energy capability");

        // set io configuration, west to input, east to output
        meterBlockEntity.getIoConfig().setSetting(Direction.WEST, TypeEnums.IoSetting.IN);
        meterBlockEntity.getIoConfig().setSetting(Direction.EAST, TypeEnums.IoSetting.OUT);

        // test whether energy input capability exposes correct handler
        IEnergyStorage inputEnergyCap = meterBlockEntity.getEnergyCapability(Direction.WEST);
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
        IEnergyStorage outputEnergyCap = meterBlockEntity.getEnergyCapability(Direction.EAST);
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
}
