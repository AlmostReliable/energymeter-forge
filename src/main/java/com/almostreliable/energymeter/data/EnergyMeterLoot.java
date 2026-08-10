package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.core.ModRegistration;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class EnergyMeterLoot extends LootTableProvider {

    EnergyMeterLoot(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), registries);
    }

    private static final class BlockLoot extends BlockLootSubProvider {

        private BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModRegistration.getKnownBlocks();
        }

        @Override
        protected void generate() {
            dropSelf(ModRegistration.METER_BLOCK.get());
            dropSelf(ModRegistration.MONITOR_BLOCK.get());
        }
    }
}
