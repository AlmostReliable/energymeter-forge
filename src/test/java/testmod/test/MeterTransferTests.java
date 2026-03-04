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
import testmod.TestUtils.SimplePlotResult;
import testmod.content.EnergyReceiverBlockEntity;

@SuppressWarnings("NewMethodNamingConvention")
@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterTransferTests {

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void transfer_one_to_one(GameTestHelper helper) {
        // set up the plot
        SimplePlotResult plotResult = TestUtils.setupSimplePlot(helper);

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunction();
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCap();

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        // push energy towards the meter from the input side
        int energyPerTick = TestUtils.getRandomEnergyRate();
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            inputEnergyFunction.accept(energyPerTick);
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
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
        int expectedEnergyStored = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            energyStored == expectedEnergyStored,
            String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void transfer_three_to_one(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.WEST, Direction.SOUTH, Direction.EAST)
            .output(Direction.UP)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var westInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.WEST);
        var southInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.SOUTH);
        var eastInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.EAST);
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.UP);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        // push energy towards the meter from the input sides
        int energyPerTick = TestUtils.getRandomEnergyRate();
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            westInEnergyFunction.accept(energyPerTick);
            southInEnergyFunction.accept(energyPerTick);
            eastInEnergyFunction.accept(energyPerTick);
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
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
        int expectedEnergyStored = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL * 3;
        helper.assertTrue(
            energyStored == expectedEnergyStored,
            String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void transfer_one_to_three(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .output(Direction.WEST, 3)
            .output(Direction.SOUTH, 2)
            .output(Direction.EAST, 1)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        EnergyReceiverBlockEntity westOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.WEST);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        EnergyReceiverBlockEntity southOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.SOUTH);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        int energyPerTick = TestUtils.getRandomEnergyRate();

        // set the capacity of the first two outputs, so they are full after a single operation
        westOutEnergyBlockEntity.setCapacity(energyPerTick);
        southOutEnergyBlockEntity.setCapacity(energyPerTick);

        // push energy towards the meter from the input side
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            inputEnergyFunction.accept(energyPerTick);
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
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

        int westEnergyStored = westOutEnergyBlockCap.getEnergyStored();
        helper.assertTrue(
            westEnergyStored == energyPerTick,
            String.format("expected stored priority 3 output energy of %s, but was %s", energyPerTick, westEnergyStored)
        );

        int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
        helper.assertTrue(
            southEnergyStored == energyPerTick,
            String.format("expected stored priority 2 output energy of %s, but was %s", energyPerTick, southEnergyStored)
        );

        int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
        int expectedEastEnergyStored = energyPerTick * (MeterBlockEntity.DEFAULT_INTERVAL - 2);
        helper.assertTrue(
            eastEnergyStored == expectedEastEnergyStored,
            String.format("expected stored priority 1 output energy of %s, but was %s", expectedEastEnergyStored, eastEnergyStored)
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void transfer_two_to_two(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .output(Direction.SOUTH, 2)
            .output(Direction.EAST, 1)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var upInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        var westInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.WEST);
        EnergyReceiverBlockEntity southOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.SOUTH);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        int energyPerTick = TestUtils.getRandomEnergyRate();

        // set the capacity of the first output, so it's full after a single operation
        southOutEnergyBlockEntity.setCapacity(energyPerTick * 2);

        // push energy towards the meter from the input sides
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            upInEnergyFunction.accept(energyPerTick);
            westInEnergyFunction.accept(energyPerTick);
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
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

        int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
        int expectedSouthEnergyStored = energyPerTick * 2;
        helper.assertTrue(
            southEnergyStored == expectedSouthEnergyStored,
            String.format(
                "expected stored priority 2 output energy of %s, but was %s",
                expectedSouthEnergyStored,
                southEnergyStored
            )
        );

        int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
        int expectedEastEnergyStored = energyPerTick * 2 * (MeterBlockEntity.DEFAULT_INTERVAL - 1);
        helper.assertTrue(
            eastEnergyStored == expectedEastEnergyStored,
            String.format("expected stored priority 1 output energy of %s, but was %s", expectedEastEnergyStored, eastEnergyStored)
        );

        helper.succeed();
    }
}
