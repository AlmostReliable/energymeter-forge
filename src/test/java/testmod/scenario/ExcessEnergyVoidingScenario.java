package testmod.scenario;

import com.almostreliable.energymeter.block.component.EnergyHandler;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import testmod.TestMod;
import testmod.TestUtils;
import testmod.TestUtils.SimplePlotResult;
import testmod.content.EnergyReceiverBlockEntity;

/**
 * This scenario tests a bug that occurred in development.
 * <p>
 * When sending energy to a block that only accepts less than its designated amount, the
 * excess energy is voided instead of being returned.
 * <p>
 * This was fixed by returning the forwarded energy in {@link EnergyHandler#forwardEnergy(int, boolean)}.
 */
@SuppressWarnings("NewMethodNamingConvention")
@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ExcessEnergyVoidingScenario {

    @GameTest(template = TestUtils.EMPTY_STRUCTURE, batch = TestUtils.BATCH_SCENARIO)
    public void test(GameTestHelper helper) {
        // set up the plot
        SimplePlotResult plotResult = TestUtils.setupSimplePlot(helper);

        MeterBlockEntity meterBlockEntity = plotResult.meterBlockEntity();
        var inputEnergyFunction = plotResult.inputEnergyFunctionWithResult();
        EnergyReceiverBlockEntity outputEnergyBlockEntity = plotResult.outputEnergyBlockEntity();

        // set transfer mode to transfer
        meterBlockEntity.setTransferMode(MeterBlockEntity.TransferMode.TRANSFER);

        // limit the output block to not accept all energy in one operation
        int energyMaxReceive = 200;
        outputEnergyBlockEntity.setMaxReceive(energyMaxReceive);

        // push energy towards the meter from the input side and store the result
        int energyPerTick = 600;
        var energyAccepted = inputEnergyFunction.apply(energyPerTick, false);

        // check the value
        helper.assertValueEqual(energyAccepted, energyMaxReceive, "energy accepted");

        helper.succeed();
    }
}
