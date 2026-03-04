package testmod.scenario;

import com.almostreliable.energymeter.block.component.EnergyHandler;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import testmod.TestMod;
import testmod.TestUtils;
import testmod.content.EnergyReceiverBlockEntity;

/**
 * This scenario tests a bug that occurred in development.
 * <p>
 * When in split mode, the {@link EnergyHandler} loops over available outputs and tries
 * to split all energy equally, as long as there are outputs not full and as long as
 * there is remaining energy to forward.
 * <p>
 * When an output is not full but does not accept multiple energy insertions per tick, the
 * while loop is never broken and the logic bricks the world.
 * <p>
 * This was fixed by a maximum operation threshold checked in the while loop.
 */
@SuppressWarnings("NewMethodNamingConvention")
@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SplitNoDoubleInsertScenario {

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_SCENARIOS)
    public void test(GameTestHelper helper) {
        // set up the plot
        var plotResult = TestUtils.PlotBuilder.create(helper)
            .input(Direction.WEST)
            .outputs(Direction.EAST, Direction.UP)
            .build();

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctions().get(Direction.WEST);
        EnergyReceiverBlockEntity eastOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.EAST);
        EnergyReceiverBlockEntity upOutEnergyBlockEntity = plotResult.outputEnergyBlockEntities().get(Direction.UP);

        // set transfer mode to split
        meterBlockEntity.setTransferMode(TransferMode.SPLIT);

        // modify the up output block capacity so it's full after the first operation
        upOutEnergyBlockEntity.setCapacity(1_000);

        // block the energy insertion of the east output block after the first operation
        eastOutEnergyBlockEntity.scheduleInsertionBlock();

        // push energy towards the meter from the input side
        int energyPerTick = 5_000;
        inputEnergyFunction.accept(energyPerTick);

        helper.succeed();
    }
}
