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
public class MeterSplitTests {

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void split_one_to_one(GameTestHelper helper) {
        // set up the plot
        SimplePlotResult plotResult = TestUtils.setupSimplePlot(helper);

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunction();
        IEnergyStorage outputEnergyBlockCap = plotResult.outputEnergyBlockCap();

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

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
    public void split_three_to_one(GameTestHelper helper) {
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

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

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
    public void split_one_to_three_even_energy(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .outputs(Direction.WEST, Direction.SOUTH, Direction.EAST)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.UP);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // push energy towards the meter from the input side
        int energyPerTick = TestUtils.getRandomDivisibleEnergyRate(3);
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

        int expectedEnergyStored = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL / 3;
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

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_METER_TESTS)
    public void split_two_to_two_even_energy(GameTestHelper helper) {
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

        // push energy towards the meter from the input sides
        int energyPerTick = TestUtils.getRandomDivisibleEnergyRate(2);
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

        int expectedEnergyStored = energyPerTick * MeterBlockEntity.DEFAULT_INTERVAL;
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
}
