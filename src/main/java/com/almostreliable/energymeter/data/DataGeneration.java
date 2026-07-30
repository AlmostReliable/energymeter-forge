package com.almostreliable.energymeter.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void initClient(GatherDataEvent.Client event) {
        init(event);
        DataGenerator generator = event.getGenerator();
        generator.addProvider(true, new EnergyMeterModels(generator.getPackOutput()));
    }

    public static void initServer(GatherDataEvent.Server event) {
        init(event);
    }

    private static void init(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var registries = event.getLookupProvider();

        generator.addProvider(true, new EnergyMeterLang(output));
        generator.addProvider(true, new EnergyMeterLoot(output, registries));
        generator.addProvider(true, new EnergyMeterRecipes.Runner(output, registries));
        generator.addProvider(true, new EnergyMeterTags(output, registries));
    }
}
