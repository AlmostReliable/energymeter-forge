package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.EnergyMeter;
import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.MonitorBlock;
import com.almostreliable.energymeter.block.multiblock.MultiblockType;
import com.almostreliable.energymeter.core.Registration;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Quadrant;

import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

class EnergyMeterModels extends ModelProvider {

    EnergyMeterModels(PackOutput output) {
        super(output, ModConstants.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        energyMeter(blockModels);
        externalMonitor(blockModels);
    }

    private static void energyMeter(BlockModelGenerators blockModels) {
        var block = Registration.METER_BLOCK.get();

        Identifier model = ModelTemplates.CUBE_ORIENTABLE.create(
            block,
            new TextureMapping()
                .put(TextureSlot.SIDE, new Material(EnergyMeter.getRL("block/normal")))
                .put(TextureSlot.FRONT, new Material(EnergyMeter.getRL("block/normal_front")))
                .put(TextureSlot.TOP, new Material(EnergyMeter.getRL("block/normal"))),
            blockModels.modelOutput
        );

        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(PropertyDispatch.modify(FacingEntityBlock.FACING)
                    .generate(facing -> rotate(facing, null)))
        );
    }

    private static void externalMonitor(BlockModelGenerators blockModels) {
        var block = Registration.MONITOR_BLOCK.get();

        Map<MultiblockType, Identifier> models = new EnumMap<>(MultiblockType.class);
        for (MultiblockType type : MultiblockType.values()) {
            models.put(type, createMonitorModel(blockModels, type));
        }

        blockModels.blockStateOutput.accept(
            MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(MonitorBlock.TYPE)
                    .generate(type -> BlockModelGenerators.plainVariant(models.get(type))))
                .with(PropertyDispatch.modify(FacingEntityBlock.FACING, FacingEntityBlock.BOTTOM)
                    .generate(EnergyMeterModels::rotate))
        );

        blockModels.itemModelOutput.accept(
            block.asItem(),
            ItemModelUtils.plainModel(models.get(MultiblockType.NORMAL))
        );
    }

    private static Identifier createMonitorModel(BlockModelGenerators blockModels, MultiblockType type) {
        if (type == MultiblockType.NONE || type == MultiblockType.SELF) {
            return ModelTemplates.CUBE_ALL.create(
                EnergyMeter.getRL("block/" + type.getSerializedName()),
                TextureMapping.cube(new Material(MultiblockType.NORMAL.getTexture())),
                blockModels.modelOutput
            );
        }

        Identifier model = type.getTexture();
        blockModels.modelOutput.accept(model, monitorModel(type));
        return model;
    }

    private static ModelInstance monitorModel(MultiblockType type) {
        JsonObject faces = new JsonObject();
        faces.add("north", face("#front", null));
        faces.add("south", face("#particle", new float[]{16, 0, 0, 16}));
        addFace(faces, "up", "#up", type.getUpTexture(), new float[]{16, 0, 0, 16});
        addFace(faces, "down", "#down", type.getDownTexture(), new float[]{16, 16, 0, 0});
        addFace(faces, "east", "#left", type.getLeftTexture(), null);
        addFace(faces, "west", "#right", type.getRightTexture(), null);

        JsonObject element = new JsonObject();
        element.add("from", vector(0, 0, 0));
        element.add("to", vector(16, 16, 16));
        element.add("faces", faces);

        JsonArray elements = new JsonArray();
        elements.add(element);

        JsonObject textures = new JsonObject();
        textures.addProperty("particle", type.getTexture().toString());
        textures.addProperty("front", type.getFrontTexture().toString());
        addTexture(textures, "up", type.getUpTexture());
        addTexture(textures, "down", type.getDownTexture());
        addTexture(textures, "left", type.getLeftTexture());
        addTexture(textures, "right", type.getRightTexture());

        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/block");
        model.add("elements", elements);
        model.add("textures", textures);

        return () -> model;
    }

    private static void addFace(
        JsonObject faces, String side, String textureSlot, @Nullable Identifier texture, float @Nullable [] uv
    ) {
        if (texture == null) return;
        faces.add(side, face(textureSlot, uv));
    }

    private static JsonObject face(String textureSlot, float @Nullable [] uv) {
        JsonObject face = new JsonObject();
        face.addProperty("texture", textureSlot);
        if (uv != null) {
            JsonArray array = new JsonArray();
            for (float value : uv) {
                array.add(value);
            }
            face.add("uv", array);
        }
        return face;
    }

    private static void addTexture(JsonObject textures, String slot, @Nullable Identifier texture) {
        if (texture == null) return;
        textures.addProperty(slot, texture.toString());
    }

    private static JsonArray vector(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static VariantMutator rotate(Direction facing, @Nullable Direction bottom) {
        return VariantMutator.X_ROT.withValue(horizontalRotation(facing))
            .then(VariantMutator.Y_ROT.withValue(verticalRotation(facing, bottom)));
    }

    private static Quadrant verticalRotation(Direction facing, @Nullable Direction bottom) {
        return switch (bottom != null && facing.getAxis().isVertical() ? bottom : facing) {
            case EAST -> Quadrant.R90;
            case SOUTH -> Quadrant.R180;
            case WEST -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    private static Quadrant horizontalRotation(Direction facing) {
        return switch (facing) {
            case UP -> Quadrant.R270;
            case DOWN -> Quadrant.R90;
            default -> Quadrant.R0;
        };
    }
}
