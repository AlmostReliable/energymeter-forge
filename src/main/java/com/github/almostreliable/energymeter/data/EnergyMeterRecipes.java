package com.github.almostreliable.energymeter.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import com.github.almostreliable.energymeter.core.Registration;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

class EnergyMeterRecipes extends RecipeProvider {

    EnergyMeterRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.METER_BLOCK)
            .pattern("ici")
            .pattern("rgr")
            .pattern("ioi")
            .define('i', Tags.Items.INGOTS_IRON)
            .define('c', Items.COMPARATOR)
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .define('g', Tags.Items.GLASS_PANES)
            .define('o', Items.OBSERVER)
            .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
            .save(recipeOutput, Registration.METER_BLOCK.getId());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.MONITOR_BLOCK, 4)
            .pattern(" m ")
            .pattern("mem")
            .pattern(" m ")
            .define('m', Registration.METER_BLOCK)
            .define('e', Items.ENDER_PEARL)
            .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
            .save(recipeOutput, Registration.MONITOR_BLOCK.getId());
    }
}
