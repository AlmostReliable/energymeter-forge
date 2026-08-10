package testmod.test;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.block.entity.MeterBlockEntity.TransferMode;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import testmod.TestUtils;

import java.util.Objects;

public class MeterTransactionTests {

    public static void valid_input_simulation_rolls_back(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        EnergyHandler input = input(plot.meterBlockEntity(), Direction.WEST);

        // insert energy without committing the transaction
        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(helper, 100, input.insert(100, transaction), "simulated insertion");
        }

        // check that the simulation left no state behind
        assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 0, 0);
        helper.succeed();
    }

    public static void root_rollback(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        EnergyHandler input = input(plot.meterBlockEntity(), Direction.WEST);

        // insert energy and let the root transaction roll back
        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(helper, 100, input.insert(100, transaction), "pending insertion");
            assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 100, 100);
        }

        // check that destination energy and throughput rolled back
        assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 0, 0);
        helper.succeed();
    }

    public static void child_commit_root_rollback(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        EnergyHandler input = input(plot.meterBlockEntity(), Direction.WEST);

        // commit the child transaction and let the root transaction roll back
        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                assertEquals(helper, 100, input.insert(100, child), "child insertion");
                child.commit();
            }
            assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 100, 100);
        }

        // check that the root rollback reverted the committed child state
        assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 0, 0);
        helper.succeed();
    }

    public static void child_rollback_root_commit(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        EnergyHandler input = input(plot.meterBlockEntity(), Direction.WEST);

        // roll back the child transaction and commit a separate root insertion
        try (Transaction root = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(root)) {
                assertEquals(helper, 100, input.insert(100, child), "child insertion");
            }
            assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 0, 0);
            assertEquals(helper, 40, input.insert(40, root), "root insertion");
            root.commit();
        }

        // check that only the root insertion remains
        assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 40, 40);
        helper.succeed();
    }

    public static void repeated_inserts_in_root_commit(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        EnergyHandler input = input(plot.meterBlockEntity(), Direction.WEST);

        // insert energy twice in the same root transaction
        try (Transaction root = Transaction.openRoot()) {
            assertEquals(helper, 30, input.insert(30, root), "first insertion");
            assertEquals(helper, 45, input.insert(45, root), "second insertion");
            root.commit();
        }

        // check that both insertions were committed and counted
        assertState(helper, plot.meterBlockEntity(), plot.outputEnergyBlockCap(), 75, 75);
        helper.succeed();
    }

    public static void partial_multi_output_rollback(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.PlotBuilder.create(helper)
            .input(Direction.UP)
            .outputs(Direction.WEST, Direction.EAST)
            .build();
        MeterBlockEntity meter = plot.meterBlockEntity();
        meter.setTransferMode(TransferMode.SPLIT);
        EnergyHandler input = input(meter, Direction.UP);

        // split energy across outputs and let the transaction roll back
        try (Transaction root = Transaction.openRoot()) {
            assertEquals(helper, 101, input.insert(101, root), "split insertion");
            assertEquals(helper, 101, outputTotal(plot), "pending output total");
            assertEquals(helper, 101, meter.getEnergyHandler().getEnergyPerInterval(), "pending throughput");
        }

        // check that all output energy and throughput rolled back
        assertEquals(helper, 0, outputTotal(plot), "rolled-back output total");
        assertEquals(helper, 0, meter.getEnergyHandler().getEnergyPerInterval(), "rolled-back throughput");
        helper.succeed();
    }

    public static void consume_mode_rollback(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        MeterBlockEntity meter = plot.meterBlockEntity();
        meter.setTransferMode(TransferMode.CONSUME);
        EnergyHandler input = input(meter, Direction.WEST);

        // consume energy and let the transaction roll back
        try (Transaction root = Transaction.openRoot()) {
            assertEquals(helper, 100, input.insert(100, root), "consumed insertion");
            assertState(helper, meter, plot.outputEnergyBlockCap(), 0, 100);
        }

        // check that consumed throughput rolled back
        assertState(helper, meter, plot.outputEnergyBlockCap(), 0, 0);
        helper.succeed();
    }

    public static void transfer_limit_rollback(GameTestHelper helper) {
        // set up the plot
        var plot = TestUtils.setupSimplePlot(helper);
        MeterBlockEntity meter = plot.meterBlockEntity();
        meter.setTransferLimit(100);
        EnergyHandler input = input(meter, Direction.WEST);

        // use part of the transfer limit and let the transaction roll back
        try (Transaction root = Transaction.openRoot()) {
            assertEquals(helper, 60, input.insert(60, root), "rolled-back limited insertion");
        }
        assertState(helper, meter, plot.outputEnergyBlockCap(), 0, 0);

        // use the full transfer limit in a new transaction
        try (Transaction root = Transaction.openRoot()) {
            assertEquals(helper, 100, input.insert(100, root), "insertion after rollback");
            root.commit();
        }

        // check that the rolled-back insertion did not consume the limit
        assertState(helper, meter, plot.outputEnergyBlockCap(), 100, 100);
        helper.succeed();
    }

    private static EnergyHandler input(MeterBlockEntity meter, Direction direction) {
        return Objects.requireNonNull(meter.getEnergyCapability(direction));
    }

    private static int outputTotal(TestUtils.PlotBuilder.Result plot) {
        return plot.outputEnergyBlockCaps().values().stream().mapToInt(EnergyHandler::getAmountAsInt).sum();
    }

    private static void assertState(
        GameTestHelper helper,
        MeterBlockEntity meter,
        EnergyHandler output,
        long expectedOutput,
        long expectedThroughput
    ) {
        assertEquals(helper, expectedOutput, output.getAmountAsInt(), "destination energy");
        assertEquals(helper, expectedThroughput, meter.getEnergyHandler().getEnergyPerInterval(), "throughput");
    }

    private static void assertEquals(GameTestHelper helper, long expected, long actual, String subject) {
        helper.assertTrue(expected == actual, subject + ": expected " + expected + ", got " + actual);
    }
}
