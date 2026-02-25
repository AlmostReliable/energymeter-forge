package com.almostreliable.energymeter.block.multiblock;

import com.almostreliable.energymeter.core.Constants;

import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;

public final class MultiblockTypeProperty extends EnumProperty<MultiblockType> {

    public static final MultiblockTypeProperty INSTANCE = new MultiblockTypeProperty();

    private MultiblockTypeProperty() {
        super(Constants.MULTIBLOCK_TYPE_PROP, MultiblockType.class, List.of(MultiblockType.values()));
    }
}
