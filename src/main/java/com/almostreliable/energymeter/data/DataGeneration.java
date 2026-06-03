package com.almostreliable.energymeter.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void initClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(true, new EnergyMeterLang(output));
        generator.addProvider(true, new EnergyMeterModels(output));
    }

    public static void initServer(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var registryAccess = event.getLookupProvider();

        generator.addProvider(true, new EnergyMeterLoot(output, registryAccess));
        generator.addProvider(true, new EnergyMeterRecipes.Runner(output, registryAccess));
        generator.addProvider(true, new EnergyMeterTags(output, registryAccess));
    }
}
