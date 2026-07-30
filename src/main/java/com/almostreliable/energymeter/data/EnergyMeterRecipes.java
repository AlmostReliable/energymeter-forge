package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.core.Registration;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

class EnergyMeterRecipes extends RecipeProvider {

    EnergyMeterRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.MISC, Registration.METER_BLOCK)
            .pattern("ici")
            .pattern("rgr")
            .pattern("ioi")
            .define('i', Tags.Items.INGOTS_IRON)
            .define('c', Items.COMPARATOR)
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .define('g', Tags.Items.GLASS_PANES)
            .define('o', Items.OBSERVER)
            .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
            .save(output, recipeKey(Registration.METER_BLOCK.getId()));

        shaped(RecipeCategory.MISC, Registration.MONITOR_BLOCK, 4)
            .pattern(" m ")
            .pattern("mem")
            .pattern(" m ")
            .define('m', Registration.METER_BLOCK)
            .define('e', Items.ENDER_PEARL)
            .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
            .save(output, recipeKey(Registration.MONITOR_BLOCK.getId()));
    }

    private static ResourceKey<Recipe<?>> recipeKey(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    static class Runner extends RecipeProvider.Runner {

        Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new EnergyMeterRecipes(registries, output);
        }

        @Override
        public String getName() {
            return "Energy Meter recipes";
        }
    }
}
