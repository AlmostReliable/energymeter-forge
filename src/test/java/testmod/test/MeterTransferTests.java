package testmod.test;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.util.TypeEnums.TransferMode;

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
public class MeterTransferTests {

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void transfer_one_to_one(GameTestHelper helper) {
        TestUtils.MeterWithIoResult meterWithIoResult = TestUtils.setupMeterWithIo(helper);
        MeterBlockEntity meterBlockEntity = meterWithIoResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = meterWithIoResult.inputEnergyBlockEntity();
        IEnergyStorage outputEnergyBlockCap = meterWithIoResult.outputEnergyBlockCap();

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        // let input energy block emit energy towards the meter
        int energyPerTick = TestUtils.getRandomEnergyRate();
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.EAST, energyPerTick);

        helper.runAtTickTime(
            MeterBlockEntity.TICK_TIME + 1,
            () -> {
                helper.assertTrue(meterBlockEntity.getEnergyRate() == energyPerTick, "energy rate should be equal to input energy rate");
                helper.assertTrue(
                    outputEnergyBlockCap.getEnergyStored() == energyPerTick * (MeterBlockEntity.TICK_TIME + 1),
                    "output energy block should have received the energy"
                );
                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void transfer_one_to_three(GameTestHelper helper) {
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .output(Direction.WEST, 3)
            .output(Direction.SOUTH, 2)
            .output(Direction.EAST, 1)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        EnergyBlockEntity inputEnergyBlockEntity = plotResult.inputEnergyBlockEntities().get(Direction.UP);
        EnergyBlockEntity westOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.WEST);
        IEnergyStorage westOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.WEST);
        EnergyBlockEntity southOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.SOUTH);
        IEnergyStorage southOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.SOUTH);
        IEnergyStorage eastOutEnergyBlockCap = plotResult.outputEnergyBlockCaps().get(Direction.EAST);

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(TransferMode.TRANSFER);

        int energyPerTick = TestUtils.getRandomEnergyRate();

        // set the capacity of the first two outputs, so they are full after a single operation
        westOutEnergyBlockEntity.setEnergyCapacity(energyPerTick);
        southOutEnergyBlockEntity.setEnergyCapacity(energyPerTick);

        // let input energy block emit energy towards the meter
        inputEnergyBlockEntity.sendEnergyPerTick(Direction.DOWN, energyPerTick);

        helper.runAtTickTime(
            1,
            () -> {
                // check whether west (priority 3) only received one tick of energy (full capacity)
                int westEnergyStored = westOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    westEnergyStored == energyPerTick,
                    String.format("expected stored priority 3 output energy of %s, but was %s", energyPerTick, westEnergyStored)
                );

                // make sure the other outputs are still empty
                int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    southEnergyStored == 0,
                    String.format("expected stored priority 2 output to be empty, but was %s", southEnergyStored)
                );
                int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    eastEnergyStored == 0,
                    String.format("expected stored priority 1 output to be empty, but was %s", eastEnergyStored)
                );
            }
        );

        helper.runAtTickTime(
            2,
            () -> {
                // check whether south (priority 2) only received one tick of energy (full capacity)
                int southEnergyStored = southOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    southEnergyStored == energyPerTick,
                    String.format("expected stored priority 2 output energy of %s, but was %s", energyPerTick, southEnergyStored)
                );

                // make sure the other outputs are still empty
                int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
                helper.assertTrue(
                    eastEnergyStored == 0,
                    String.format("expected stored priority 1 output to be empty, but was %s", eastEnergyStored)
                );
            }
        );

        helper.runAtTickTime(
            MeterBlockEntity.TICK_TIME,
            () -> {
                // check whether east (priority 1) only received the remaining ticks of energy
                int eastEnergyStored = eastOutEnergyBlockCap.getEnergyStored();
                int expectedEastEnergyStored = energyPerTick * 3;
                helper.assertTrue(
                    eastEnergyStored == expectedEastEnergyStored,
                    String.format("expected stored priority 1 output energy of %s, but was %s", expectedEastEnergyStored, eastEnergyStored)
                );
            }
        );

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

                helper.succeed();
            }
        );
    }
}
