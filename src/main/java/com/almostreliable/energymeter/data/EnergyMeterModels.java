package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.FacingEntityBlock;
import com.almostreliable.energymeter.block.MonitorBlock;
import com.almostreliable.energymeter.block.multiblock.MultiblockType;
import com.almostreliable.energymeter.core.Registration;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

class EnergyMeterModels extends BlockStateProvider {

    EnergyMeterModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        energyMeter();
        externalMonitor();
    }

    private void energyMeter() {
        var block = Registration.METER_BLOCK;
        var model = models().orientable(
            block.getId().getPath(),
            modLoc("block/normal"),
            modLoc("block/normal_front"),
            modLoc("block/normal")
        );

        getVariantBuilder(block.get()).forAllStatesExcept(
            state -> {
                Direction facing = FacingEntityBlock.getFacingDir(state);
                return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(getHorizontalRotation(facing))
                    .rotationY(getVerticalRotation(facing, null))
                    .build();
            },
            FacingEntityBlock.BOTTOM
        );

        itemModels().simpleBlockItem(block.get());
    }

    private void externalMonitor() {
        var block = Registration.MONITOR_BLOCK;
        Map<MultiblockType, ModelFile> monitorModels = new EnumMap<>(MultiblockType.class);

        for (MultiblockType type : MultiblockType.values()) {
            if (type == MultiblockType.NONE || type == MultiblockType.SELF) {
                monitorModels.put(type, models().cubeAll(type.getSerializedName(), MultiblockType.NORMAL.getTexture()));
                continue;
            }

            BlockModelBuilder model = models().getBuilder(type.getTexture().getPath())
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("front", type.getFrontTexture())
                .texture("particle", type.getTexture());

            var element = model.element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(Direction.SOUTH).uvs(16, 0, 0, 16).texture("#particle").end();

            if (type.getUpTexture() != null) {
                model.texture("up", type.getUpTexture());
                element.face(Direction.UP).uvs(16, 0, 0, 16).texture("#up").end();
            }
            if (type.getDownTexture() != null) {
                model.texture("down", type.getDownTexture());
                element.face(Direction.DOWN).uvs(16, 16, 0, 0).texture("#down").end();
            }
            if (type.getLeftTexture() != null) {
                model.texture("left", type.getLeftTexture());
                element.face(Direction.EAST).uvs(0, 0, 16, 16).texture("#left").end();
            }
            if (type.getRightTexture() != null) {
                model.texture("right", type.getRightTexture());
                element.face(Direction.WEST).uvs(0, 0, 16, 16).texture("#right").end();
            }

            element.end();
            monitorModels.put(type, model);
        }

        getVariantBuilder(block.get()).forAllStatesExcept(
            state -> {
                MultiblockType type = state.getValue(MonitorBlock.TYPE);
                ModelFile model = monitorModels.get(type);

                if (type == MultiblockType.NONE || type == MultiblockType.SELF) {
                    return ConfiguredModel.builder().modelFile(model).build();
                }

                Direction facing = FacingEntityBlock.getFacingDir(state);
                Direction bottom = FacingEntityBlock.getBottomDir(state);

                return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(getHorizontalRotation(facing))
                    .rotationY(getVerticalRotation(facing, bottom))
                    .build();
            },
            MonitorBlock.CONTROLLER, MonitorBlock.HORIZONTAL, MonitorBlock.VERTICAL
        );

        itemModels().simpleBlockItem(modLoc(MultiblockType.NORMAL.getSerializedName()));
    }

    private int getVerticalRotation(Direction facing, @Nullable Direction bottom) {
        return switch (bottom != null && facing.getAxis().isVertical() ? bottom : facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private int getHorizontalRotation(Direction facing) {
        return switch (facing) {
            case UP -> 270;
            case DOWN -> 90;
            default -> 0;
        };
    }
}
