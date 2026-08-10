package testmod.scenario;

import com.almostreliable.energymeter.block.component.MeterEnergyHandler;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import testmod.TestMod;
import testmod.TestUtils;
import testmod.content.EnergyReceiverBlockEntity;

/**
 * This scenario tests a bug that occurred in development.
 * <p>
 * When in split mode, the {@link MeterEnergyHandler} loops over available outputs and tries
 * to split all energy equally, as long as there are outputs not full and as long as
 * there is remaining energy to forward.
 * <p>
 * When an output is not full but does not accept multiple energy insertions per tick, the
 * while loop is never broken and the logic bricks the world.
 * <p>
 * At first, this was fixed by a maximum operation threshold checked in the while loop.
 * Later the logic was refactored to pre-calculate the maximum energy per output and then
 * doing a single operation per output.
 */
public class SplitNoDoubleInsertScenario {

    public static void test(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.WEST)
            .outputs(Direction.EAST, Direction.UP)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctionsWithResult().get(Direction.WEST);
        EnergyReceiverBlockEntity eastOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.EAST);
        EnergyReceiverBlockEntity upOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.UP);
        var eastOutEnergy = plotResult.outputEnergyBlockCaps().get(Direction.EAST);
        var upOutEnergy = plotResult.outputEnergyBlockCaps().get(Direction.UP);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // modify the up output block capacity so it's full after the first operation
        upOutEnergyBlockEntity.setCapacity(1_000);

        int energyPerTick = 5_000;

        // verify the split calculation without committing energy to either output
        int simulatedInsertion = inputEnergyFunction.apply(energyPerTick, true);
        helper.assertTrue(simulatedInsertion == energyPerTick, "simulation should accept all input energy");
        helper.assertTrue(eastOutEnergy.getAmountAsInt() == 0, "simulation stored energy in east output");
        helper.assertTrue(upOutEnergy.getAmountAsInt() == 0, "simulation stored energy in up output");

        // exclude the simulated root operation from committed-operation counts
        eastOutEnergyBlockEntity.resetRootInsertionCalls();
        upOutEnergyBlockEntity.resetRootInsertionCalls();

        // commit the same transfer and verify its calculated distribution
        int inserted = inputEnergyFunction.apply(energyPerTick, false);
        helper.assertTrue(inserted == energyPerTick, "expected all input energy to be accepted");
        helper.assertTrue(eastOutEnergy.getAmountAsInt() == 4_000, "expected east output to store 4000 energy");
        helper.assertTrue(upOutEnergy.getAmountAsInt() == 1_000, "expected up output to store 1000 energy");

        // probe child transactions are ignored, these counts expose repeated committed inserts
        helper.assertTrue(eastOutEnergyBlockEntity.getRootInsertionCalls() == 1, "east output received multiple inserts");
        helper.assertTrue(upOutEnergyBlockEntity.getRootInsertionCalls() == 1, "up output received multiple inserts");

        helper.succeed();
    }
}
