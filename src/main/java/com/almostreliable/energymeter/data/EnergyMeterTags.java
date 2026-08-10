package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.core.ModRegistration;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class EnergyMeterTags extends BlockTagsProvider {

    public EnergyMeterTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ModConstants.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModRegistration.METER_BLOCK.get(), ModRegistration.MONITOR_BLOCK.get());
    }
}
