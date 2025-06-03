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

@GameTestHolder(TestMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MeterTransferTests {

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void meterConsumerOne(GameTestHelper helper) {
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
                helper.assertTrue(meterBlockEntity.getEnergyRate() == energyPerTick, "energy rate should be equal to input energy rate");
                helper.assertTrue(outputEnergyBlockCap.getEnergyStored() == 0, "output energy block should be empty");
                helper.succeed();
            }
        );
    }

    @GameTest(setupTicks = MeterBlockEntity.TICK_TIME + 1, template = TestUtils.EMPTY_STRUCTURE)
    public void meterTransferOneToOne(GameTestHelper helper) {
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
}
