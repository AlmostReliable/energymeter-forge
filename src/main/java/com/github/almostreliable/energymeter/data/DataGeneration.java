package com.github.almostreliable.energymeter.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new EnergyMeterLang(output));
        generator.addProvider(event.includeClient(), new EnergyMeterModels(output, existingFileHelper));

        generator.addProvider(event.includeServer(), new EnergyMeterRecipes(output, event.getLookupProvider()));
    }
}
