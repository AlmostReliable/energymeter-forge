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

@SuppressWarnings("NewMethodNamingConvention")
@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterTransferLimitTests {

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void limit_transfer_one_to_one(GameTestHelper helper) {
        // set up the plot
        SimplePlotResult plotResult = TestUtils.setupSimplePlot(helper);

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunction();
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCap();

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        // set transfer limit and ensure it is set correctly
        int transferLimit = 500;
        meterBlockEntity.setTransferLimit(transferLimit);

        // push energy towards the meter from the input side
        int energyPerTick = 1000;
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            inputEnergyFunction.accept(energyPerTick);
            meterBlockEntity.getEnergyHandler().resetTickLimiter();
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
        double energyRate = meterBlockEntity.getEnergyRate();
        helper.assertTrue(
            energyRate == transferLimit,
            String.format("expected energy rate to be limited to %s, but was %s", transferLimit, energyRate)
        );

        double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
        int expectedEnergyTransferred = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            totalEnergyTransferred == expectedEnergyTransferred,
            String.format("expected total energy of %s, but was %s", expectedEnergyTransferred, totalEnergyTransferred)
        );

        int energyStored = outputEnergyBlockCap.getEnergyStored();
        int expectedEnergyStored = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            energyStored == expectedEnergyStored,
            String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void limit_split_one_to_three(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .output(Direction.WEST, 3)
            .output(Direction.SOUTH, 2)
            .output(Direction.EAST, 1)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // set transfer limit and ensure it is set correctly
        int transferLimit = 700;
        meterBlockEntity.setTransferLimit(transferLimit);

        // push energy towards the meter from the input side
        int energyPerTick = 1200;
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            inputEnergyFunction.accept(energyPerTick);
            meterBlockEntity.getEnergyHandler().resetTickLimiter();
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
        double energyRate = meterBlockEntity.getEnergyRate();
        helper.assertTrue(
            energyRate == transferLimit,
            String.format("expected energy rate to be limited to %s, but was %s", transferLimit, energyRate)
        );

        double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
        int expectedTotalEnergyTransferred = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            totalEnergyTransferred == expectedTotalEnergyTransferred,
            String.format("expected total energy of %s, but was %s", expectedTotalEnergyTransferred, totalEnergyTransferred)
        );

        int westEnergyStored = westOutEnergyBlockCap.getEnergyStored();
        int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
        int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
        int energyStoredSum = westEnergyStored + southEnergyStored + eastEnergyStored;
        int expectedEnergyStoredSum = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;

        helper.assertTrue(
            energyStoredSum == expectedEnergyStoredSum,
            String.format("expected total distributed energy %s, but was %s", expectedEnergyStoredSum, energyStoredSum)
        );

        // distribution must be balanced (difference ≤ 1)
        int min = Math.min(westEnergyStored, Math.min(southEnergyStored, eastEnergyStored));
        int max = Math.max(westEnergyStored, Math.max(southEnergyStored, eastEnergyStored));
        helper.assertTrue(
            max - min <= MeterBlockEntity.DEFAULT_INTERVAL,
            String.format("expected balanced distribution, but min=%s max=%s", min, max)
        );

        // each output must be within floor and ceil bounds
        int floor = expectedEnergyStoredSum / 3 - MeterBlockEntity.DEFAULT_INTERVAL;
        int ceil = (int) Math.ceil(expectedEnergyStoredSum / 3.0) + MeterBlockEntity.DEFAULT_INTERVAL;

        helper.assertTrue(
            (westEnergyStored >= floor && westEnergyStored <= ceil) &&
                (southEnergyStored >= floor && southEnergyStored <= ceil) &&
                (eastEnergyStored >= floor && eastEnergyStored <= ceil),
            String.format(
                "expected each output to be between %s and %s but was W:%s S:%s E:%s",
                floor, ceil, westEnergyStored, southEnergyStored, eastEnergyStored
            )
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void limit_transfer_two_to_one(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .output(Direction.SOUTH)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var upInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        var westInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        // set transfer limit and ensure it is set correctly
        int transferLimit = 800;
        meterBlockEntity.setTransferLimit(transferLimit);

        // push energy towards the meter from the input sides
        int energyPerInputPerTick = 500;
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            upInEnergyFunction.accept(energyPerInputPerTick);
            westInEnergyFunction.accept(energyPerInputPerTick);
            meterBlockEntity.getEnergyHandler().resetTickLimiter();
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
        double energyRate = meterBlockEntity.getEnergyRate();
        helper.assertTrue(
            energyRate == transferLimit,
            String.format("expected energy rate to be limited to %s, but was %s", transferLimit, energyRate)
        );

        double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
        int expectedTotalEnergyTransferred = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            totalEnergyTransferred == expectedTotalEnergyTransferred,
            String.format("expected total energy of %s, but was %s", expectedTotalEnergyTransferred, totalEnergyTransferred)
        );

        int energyStored = southOutEnergyBlockCap.getEnergyStored();
        int expectedEnergyStored = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            energyStored == expectedEnergyStored,
            String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void limit_split_two_to_two(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .outputs(Direction.SOUTH, Direction.EAST)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var upInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        var westInEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // set transfer limit and ensure it is set correctly
        int transferLimit = 600;
        meterBlockEntity.setTransferLimit(transferLimit);

        // push energy towards the meter from the input sides
        int energyPerInputPerTick = 400;
        for (int i = 0; i < MeterBlockEntity.DEFAULT_INTERVAL; i++) {
            upInEnergyFunction.accept(energyPerInputPerTick);
            westInEnergyFunction.accept(energyPerInputPerTick);
            meterBlockEntity.getEnergyHandler().resetTickLimiter();
        }

        // simulate a full interval
        meterBlockEntity.refreshEnergyValues();

        // check the values
        double energyRate = meterBlockEntity.getEnergyRate();
        helper.assertTrue(
            energyRate == transferLimit,
            String.format("expected energy rate to be limited to %s, but was %s", transferLimit, energyRate)
        );

        double totalEnergyTransferred = meterBlockEntity.getTotalEnergy();
        int expectedTotalEnergyTransferred = transferLimit * MeterBlockEntity.DEFAULT_INTERVAL;
        helper.assertTrue(
            totalEnergyTransferred == expectedTotalEnergyTransferred,
            String.format("expected total energy of %s, but was %s", expectedTotalEnergyTransferred, totalEnergyTransferred)
        );

        int expectedEnergyPerOutputPerTick = transferLimit / 2;
        int expectedEnergyStoredPerOutput = expectedEnergyPerOutputPerTick * MeterBlockEntity.DEFAULT_INTERVAL;

        int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
        helper.assertTrue(
            southEnergyStored == expectedEnergyStoredPerOutput,
            String.format("expected stored south output energy of %s, but was %s", expectedEnergyStoredPerOutput, southEnergyStored)
        );

        int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
        helper.assertTrue(
            eastEnergyStored == expectedEnergyStoredPerOutput,
            String.format("expected stored east output energy of %s, but was %s", expectedEnergyStoredPerOutput, eastEnergyStored)
        );

        helper.succeed();
    }
}
