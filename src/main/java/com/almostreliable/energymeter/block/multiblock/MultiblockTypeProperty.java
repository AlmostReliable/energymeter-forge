package com.almostreliable.energymeter.block.multiblock;

import com.almostreliable.energymeter.core.Constants;

import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;

public final class MultiblockTypeProperty {

    public static final EnumProperty<MultiblockType> INSTANCE =
        EnumProperty.create(Constants.MULTIBLOCK_TYPE_PROP, MultiblockType.class, List.of(MultiblockType.values()));

    private MultiblockTypeProperty() {}
}
