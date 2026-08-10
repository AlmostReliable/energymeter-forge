package testmod.test;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import testmod.TestUtils;

public class MeterBlockTests {

    public static void neighbor_update_cache_invalidation(GameTestHelper helper) {
        var plot = TestUtils.PlotBuilder.create(helper)
            .input(Direction.WEST)
            .outputs(Direction.EAST, Direction.UP)
            .build();
        var energyHandler = plot.meterBlockEntity().getEnergyHandler();

        helper.assertTrue(
            energyHandler.getValidOutputEnergyStorages().iterator().hasNext(),
            "configured outputs should provide energy storage"
        );
        helper.assertTrue(
            energyHandler.hasOutputCache(Direction.EAST),
            "east output cache should be initialized"
        );
        helper.assertTrue(
            energyHandler.hasOutputCache(Direction.UP),
            "up output cache should be initialized"
        );

        helper.setBlock(TestUtils.DEFAULT_POS.relative(Direction.WEST), Blocks.STONE);

        helper.assertTrue(
            energyHandler.hasOutputCache(Direction.EAST),
            "input neighbor updates must not invalidate east output cache"
        );
        helper.assertTrue(
            energyHandler.hasOutputCache(Direction.UP),
            "input neighbor updates must not invalidate up output cache"
        );

        helper.setBlock(TestUtils.DEFAULT_POS.relative(Direction.UP), Blocks.STONE);

        helper.assertTrue(
            energyHandler.hasOutputCache(Direction.EAST),
            "output cache must not be invalidated when different output neighbor is updated"
        );
        helper.assertFalse(
            energyHandler.hasOutputCache(Direction.UP),
            "output cache must be invalidated when output neighbor is updated"
        );

        helper.succeed();
    }
}
