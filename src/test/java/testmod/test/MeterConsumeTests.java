package testmod.test;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import testmod.TestMod;
import testmod.TestUtils;
import testmod.content.EnergyBlockEntity;

@SuppressWarnings("NewMethodNamingConvention")
@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterConsumeTests {

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void consume_single(GameTestHelper helper) {
        TestUtils.MeterWithIoResult meterWithIoResult = TestUtils.setupMeterWithIo(helper);
        MeterBlockEntity meterBlockEntity = meterWithIoResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = meterWithIoResult.inputEnergyBlockEntity();
        IEnergyStorage outputEnergyBlockCap = meterWithIoResult.outputEnergyBlockCap();

        // set transfer mode to consume
        meterBlockEntity.setTransferMode(TransferMode.CONSUME);

        // let input energy block emit energy towards the meter
        int energyPerTick = TestUtils.getRandomEnergyRate();
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.TICK_TIME + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                helper.assertTrue(
                    energyRate == energyPerTick,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", energyPerTick, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * MeterBlockEntity.TICK_TIME;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int energyStored = outputEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    energyStored == 0,
                    String.format("expected stored output energy of %s, but was %s", 0, energyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void consume_trio(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.WEST, Direction.SOUTH, Direction.EAST)
            .output(Direction.UP)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.UP);
        EnergyBlockEntity westInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.WEST);
        EnergyBlockEntity southInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.SOUTH);
        EnergyBlockEntity eastInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.EAST);

        // set transfer mode to consume
        meterBlockEntity.setTransferMode(TransferMode.CONSUME);

        // let input energy blocks emit energy towards the meter
        int energyPerTick = TestUtils.getRandomEnergyRate();
        westInEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);
        southInEnergyBlockEntity.sendEnergyPerTick(Direction.NORTH, energyPerTick);
        eastInEnergyBlockEntity.sendEnergyPerTick(Direction.WEST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.TICK_TIME + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                int expectedEnergyRate = energyPerTick * 3;
                helper.assertTrue(
                    energyRate == expectedEnergyRate,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", expectedEnergyRate, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * MeterBlockEntity.TICK_TIME * 3;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int energyStored = outputEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    energyStored == 0,
                    String.format("expected stored output energy of %s, but was %s", 0, energyStored)
                );

                helper.succeed();
            }
        );
    }
}
