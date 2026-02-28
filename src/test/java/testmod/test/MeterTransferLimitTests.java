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
public class MeterTransferLimitTests {

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void limit_transfer_one_to_one(GameTestHelper helper) {
        TestUtils.MeterWithIoResult meterWithIoResult = TestUtils.setupMeterWithIo(helper);
        MeterBlockEntity meterBlockEntity = meterWithIoResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = meterWithIoResult.inputEnergyBlockEntity();
        IEnergyStorage outputEnergyBlockCap = meterWithIoResult.outputEnergyBlockCap();

        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        int transferLimit = 500;
        meterBlockEntity.setTransferLimit(transferLimit);
        helper.assertValueEqual(meterBlockEntity.getTransferLimit(), transferLimit, "transfer limit should be set correctly");

        int energyPerTick = 1000;
        inputEnergyBlockEntity.setEnergyToEmitPerTick(energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
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
                int expectedEnergyStored = transferLimit * (MeterBlockEntity.DEFAULT_INTERVAL + 1);
                helper.assertTrue(
                    energyStored == expectedEnergyStored,
                    String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void limit_split_one_to_three(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .output(Direction.WEST, 3)
            .output(Direction.SOUTH, 2)
            .output(Direction.EAST, 1)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        int transferLimit = 700;
        meterBlockEntity.setTransferLimit(transferLimit);
        helper.assertValueEqual(meterBlockEntity.getTransferLimit(), transferLimit, "transfer limit should be set correctly");

        int energyPerTick = 1200;
        inputEnergyBlockEntity.setEnergyToEmitPerTick(energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
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
                int expectedEnergyStoredSum = transferLimit * (MeterBlockEntity.DEFAULT_INTERVAL + 1);

                helper.assertTrue(
                    energyStoredSum == expectedEnergyStoredSum,
                    String.format("expected total distributed energy %s, but was %s", energyStoredSum, expectedEnergyStoredSum)
                );

                // distribution must be balanced (difference ≤ 1)
                int min = Math.min(westEnergyStored, Math.min(southEnergyStored, eastEnergyStored));
                int max = Math.max(westEnergyStored, Math.max(southEnergyStored, eastEnergyStored));
                helper.assertTrue(
                    max - min <= MeterBlockEntity.DEFAULT_INTERVAL,
                    String.format("expected balanced distribution, but min=%s max=%s", min, max)
                );

                // each output must be either floor or ceil
                int floor = expectedEnergyStoredSum / 3 - MeterBlockEntity.DEFAULT_INTERVAL;
                int ceil = (int) Math.ceil(expectedEnergyStoredSum / 3.0) + MeterBlockEntity.DEFAULT_INTERVAL;

                helper.assertTrue(
                    (westEnergyStored >= floor || westEnergyStored <= ceil) &&
                        (southEnergyStored >= floor || southEnergyStored <= ceil) &&
                        (eastEnergyStored >= floor || eastEnergyStored <= ceil),
                    String.format(
                        "expected each output to be %s or %s but was W:%s S:%s E:%s",
                        floor, ceil, westEnergyStored, southEnergyStored, eastEnergyStored
                    )
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void limit_transfer_two_to_one(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .output(Direction.SOUTH)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity upInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        EnergyBlockEntity westInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);

        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        int transferLimit = 800;
        meterBlockEntity.setTransferLimit(transferLimit);
        helper.assertValueEqual(meterBlockEntity.getTransferLimit(), transferLimit, "transfer limit should be set correctly");

        int energyPerInputPerTick = 500;
        upInEnergyBlockEntity.setEnergyToEmitPerTick(energyPerInputPerTick);
        westInEnergyBlockEntity.setEnergyToEmitPerTick(energyPerInputPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
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
                int expectedEnergyStored = transferLimit * (MeterBlockEntity.DEFAULT_INTERVAL + 1);
                helper.assertTrue(
                    energyStored == expectedEnergyStored,
                    String.format("expected stored output energy of %s, but was %s", expectedEnergyStored, energyStored)
                );

                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.DEFAULT_INTERVAL + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void limit_split_multiple_inputs_multiple_outputs(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .inputs(Direction.UP, Direction.WEST)
            .outputs(Direction.SOUTH, Direction.EAST)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity upInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        EnergyBlockEntity westInEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.WEST);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        int transferLimit = 600;
        meterBlockEntity.setTransferLimit(transferLimit);
        helper.assertValueEqual(meterBlockEntity.getTransferLimit(), transferLimit, "transfer limit should be set correctly");

        int energyPerInputPerTick = 400;
        upInEnergyBlockEntity.setEnergyToEmitPerTick(energyPerInputPerTick);
        westInEnergyBlockEntity.setEnergyToEmitPerTick(energyPerInputPerTick);

        int expectedEnergyPerOutputPerTick = transferLimit / 2;

        helper.runAtTickTime(
            MeterBlockEntity.DEFAULT_INTERVAL + 1,
            () -> {
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

                int expectedEnergyStoredPerOutput = expectedEnergyPerOutputPerTick * (MeterBlockEntity.DEFAULT_INTERVAL + 1);

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
        );
    }
}
