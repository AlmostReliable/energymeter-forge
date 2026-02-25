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
public class MeterSplitTests {

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void split_one_to_one(GameTestHelper helper) {
        TestUtils.MeterWithIoResult meterWithIoResult = TestUtils.setupMeterWithIo(helper);
        MeterBlockEntity meterBlockEntity = meterWithIoResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = meterWithIoResult.inputEnergyBlockEntity();
        IEnergyStorage outputEnergyBlockCap = meterWithIoResult.outputEnergyBlockCap();

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // let input energy block emit energy towards the meter
        int energyPerTick = TestUtils.getRandomEnergyRate();
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                helper.assertTrue(
                    energyRate == energyPerTick,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", energyPerTick, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int energyStored = outputEnergyBlockCap.getEnergyStored();
                int expectedEnergyStored = energyPerTick * (MeterBlockEntity.DEFAULT_INTERVAL + 1);
                helper.assertTrue(
                    energyStored == expectedEnergyStored,
                    String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void split_three_to_one(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.WEST, Direction.SOUTH, Direction.EAST)
            .output(Direction.UP)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity westInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.WEST);
        EnergyBlockEntity southInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.SOUTH);
        EnergyBlockEntity eastInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.EAST);
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.UP);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // let input energy blocks emit energy towards the meter
        int energyPerTick = TestUtils.getRandomEnergyRate();
        westInEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);
        southInEnergyBlockEntity.sendEnergyPerTick(Direction.NORTH, energyPerTick);
        eastInEnergyBlockEntity.sendEnergyPerTick(Direction.WEST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                int expectedEnergyRate = energyPerTick * 3;
                helper.assertTrue(
                    energyRate == expectedEnergyRate,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", expectedEnergyRate, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL * 3;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int energyStored = outputEnergyBlockCap.getEnergyStored();
                int expectedEnergyStored = energyPerTick * (MeterBlockEntity.DEFAULT_INTERVAL + 1) * 3;
                helper.assertTrue(
                    energyStored == expectedEnergyStored,
                    String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void split_one_to_three(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .outputs(Direction.WEST, Direction.SOUTH, Direction.EAST)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        int energyPerTickCalc = TestUtils.getRandomEnergyRate();
        if (energyPerTickCalc % 3 != 0) {
            energyPerTickCalc += 3 - (energyPerTickCalc % 3);
        }
        int energyPerTick = energyPerTickCalc;

        // let input energy block emit energy towards the meter
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.DOWN, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                helper.assertTrue(
                    energyRate == energyPerTick,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", energyPerTick, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int expectedEnergyStored = energyPerTick * (MeterBlockEntity.DEFAULT_INTERVAL + 1) / 3;
                int westEnergyStored = westOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    westEnergyStored == expectedEnergyStored,
                    String.format("expected stored west output energy of %s, but was %s", expectedEnergyStored, westEnergyStored)
                );
                int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    southEnergyStored == expectedEnergyStored,
                    String.format("expected stored south output energy of %s, but was %s", expectedEnergyStored, southEnergyStored)
                );
                int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    eastEnergyStored == expectedEnergyStored,
                    String.format("expected stored east output energy of %s, but was %s", expectedEnergyStored, eastEnergyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void split_two_to_two(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .outputs(Direction.SOUTH, Direction.EAST)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity upInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        EnergyBlockEntity westInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        int energyPerTickCalc = TestUtils.getRandomEnergyRate();
        if (energyPerTickCalc % 3 != 0) {
            energyPerTickCalc += 3 - (energyPerTickCalc % 3);
        }
        int energyPerTick = energyPerTickCalc;

        // let input energy blocks emit energy towards the meter
        upInEnergyBlockEntity.sendEnergyPerTick(Direction.DOWN, energyPerTick);
        westInEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
                double energyRate = meterBlockEntity.getEnergyRate();
                int expectedEnergyRate = energyPerTick * 2;
                helper.assertTrue(
                    energyRate == expectedEnergyRate,
                    String.format("expected energy rate to be equal to input energy rate of %s, but was %s", expectedEnergyRate, energyRate)
                );

                double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
                int expectedEnergyTransferred = energyPerTick * 2 * MeterBlockEntity.DEFAULT_INTERVAL;
                helper.assertTrue(
                    totalEnergyTransferred == expectedEnergyTransferred,
                    String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
                );

                int expectedEnergyStored = (energyPerTick / 2) * 2 * (MeterBlockEntity.DEFAULT_INTERVAL + 1);
                int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    southEnergyStored == expectedEnergyStored,
                    String.format("expected stored south output energy of %s, but was %s", expectedEnergyStored, southEnergyStored)
                );
                int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    eastEnergyStored == expectedEnergyStored,
                    String.format("expected stored east output energy of %s, but was %s", expectedEnergyStored, eastEnergyStored)
                );

                helper.succeed();
            }
        );
    }
}
