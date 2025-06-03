package testmod;

import com.almostreliable.energymeter.block.component.IoConfig.IoSettingWithPriority;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.core.Registration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;

import testmod.content.EnergyBlockEntity;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class TestUtils {

    public static final String EMPTY_STRUCTURE = "empty_test_structure";
    public static final BlockPos DEFAULT_POS = new BlockPos(1, 2, 1);

    private TestUtils() {}

    public static void assertNull(@Nullable Object o, String failureMessage) {
        if (o != null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertNotNull(@Nullable Object o, String failureMessage) {
        if (o == null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertInstanceOf(Object o, Class<?> clazz, String failureMessage) {
        if (!clazz.isInstance(o)) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static int getRandomEnergyRate() {
        return (int) (Math.random() * 10_000);
    }

    public static MeterBlockEntity setupMeter(GameTestHelper helper) {
        helper.setBlock(DEFAULT_POS, Registration.METER_BLOCK.get());
        return helper.getBlockEntity(DEFAULT_POS);
    }

    public static MeterWithIoResult setupMeterWithIo(GameTestHelper helper) {
        var plotResult = PlotBuilder.create(helper).input(Direction.WEST).output(Direction.EAST).build();

        return new MeterWithIoResult(
            plotResult.meterBlockEntity,
            plotResult.inputEnergyBlockEntities.get(Direction.WEST),
            plotResult.inputEnergyBlockCaps.get(Direction.WEST),
            plotResult.outputEnergyBlockEntities.get(Direction.EAST),
            plotResult.outputEnergyBlockCaps.get(Direction.EAST)
        );
    }

    public record MeterWithIoResult(
        MeterBlockEntity meterBlockEntity,
        EnergyBlockEntity inputEnergyBlockEntity,
        IEnergyStorage inputEnergyBlockCap,
        EnergyBlockEntity outputEnergyBlockEntity,
        IEnergyStorage outputEnergyBlockCap
    ) {}

    public static final class PlotBuilder {

        private final GameTestHelper helper;
        private final MeterBlockEntity meterBlockEntity;
        private final Map<Direction, EnergyBlockEntity> inputEnergyBlockEntities = new EnumMap<>(Direction.class);
        private final Map<Direction, IEnergyStorage> inputEnergyBlockCaps = new EnumMap<>(Direction.class);
        private final Map<Direction, EnergyBlockEntity> outputEnergyBlockEntities = new EnumMap<>(Direction.class);
        private final Map<Direction, IEnergyStorage> outputEnergyBlockCaps = new EnumMap<>(Direction.class);

        private PlotBuilder(GameTestHelper helper, MeterBlockEntity meterBlockEntity) {
            this.helper = helper;
            this.meterBlockEntity = meterBlockEntity;
        }

        public static PlotBuilder create(GameTestHelper helper) {
            MeterBlockEntity meterBlockEntity = setupMeter(helper);
            return new PlotBuilder(helper, meterBlockEntity);
        }

        public PlotBuilder input(Direction inputDirection) {
            // set io input configuration
            meterBlockEntity.getIoConfig().setSetting(inputDirection, IoSettingWithPriority.IN);

            // place the test energy block on the configured side
            helper.setBlock(DEFAULT_POS.relative(inputDirection), TestRegistration.ENERGY_BLOCK.get());
            EnergyBlockEntity inputEnergyBlockEntity = helper.getBlockEntity(DEFAULT_POS.relative(inputDirection));

            // test whether the energy block is empty
            IEnergyStorage inputEnergyBlockCap = inputEnergyBlockEntity.getEnergyCapability(null);
            helper.assertTrue(
                inputEnergyBlockCap != null && inputEnergyBlockCap.getEnergyStored() == 0,
                "input energy block should be empty"
            );

            inputEnergyBlockEntities.put(inputDirection, inputEnergyBlockEntity);
            inputEnergyBlockCaps.put(inputDirection, inputEnergyBlockCap);

            return this;
        }

        public PlotBuilder inputs(Direction... inputDirections) {
            for (Direction inputDirection : inputDirections) {
                input(inputDirection);
            }

            return this;
        }

        public PlotBuilder output(Direction outputDirection, int priority) {
            // set io output configuration
            meterBlockEntity.getIoConfig().setSetting(outputDirection, IoSettingWithPriority.priorityOutput(priority));

            // place the test energy block on the configured side
            helper.setBlock(DEFAULT_POS.relative(outputDirection), TestRegistration.ENERGY_BLOCK.get());
            EnergyBlockEntity outputEnergyBlockEntity = helper.getBlockEntity(DEFAULT_POS.relative(outputDirection));

            // test whether the energy block is empty
            IEnergyStorage outputEnergyBlockCap = outputEnergyBlockEntity.getEnergyCapability(null);
            helper.assertTrue(
                outputEnergyBlockCap != null && outputEnergyBlockCap.getEnergyStored() == 0,
                "output energy block should be empty"
            );

            outputEnergyBlockEntities.put(outputDirection, outputEnergyBlockEntity);
            outputEnergyBlockCaps.put(outputDirection, outputEnergyBlockCap);

            return this;
        }

        public PlotBuilder output(Direction outputDirection) {
            return output(outputDirection, 1);
        }

        public PlotBuilder outputs(Direction... outputDirections) {
            for (Direction outputDirection : outputDirections) {
                output(outputDirection);
            }

            return this;
        }

        public Result build() {
            assert inputEnergyBlockEntities.size() == inputEnergyBlockCaps.size();
            assert outputEnergyBlockEntities.size() == outputEnergyBlockCaps.size();

            return new Result(
                meterBlockEntity,
                inputEnergyBlockEntities,
                inputEnergyBlockCaps,
                outputEnergyBlockEntities,
                outputEnergyBlockCaps
            );
        }

        public record Result(
            MeterBlockEntity meterBlockEntity,
            Map<Direction, EnergyBlockEntity> inputEnergyBlockEntities,
            Map<Direction, IEnergyStorage> inputEnergyBlockCaps,
            Map<Direction, EnergyBlockEntity> outputEnergyBlockEntities,
            Map<Direction, IEnergyStorage> outputEnergyBlockCaps
        ) {}
    }
}
