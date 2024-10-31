package com.github.almostreliable.energymeter.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import com.almostreliable.energymeter.ModConstants;

import com.github.almostreliable.energymeter.block.MeterBlock;
import com.github.almostreliable.energymeter.block.MonitorBlock;
import com.github.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.github.almostreliable.energymeter.block.entity.MonitorBlockEntity;
import com.github.almostreliable.energymeter.menu.MeterMenu;
import com.github.almostreliable.energymeter.menu.MonitorMenu;
import com.github.almostreliable.energymeter.util.Utils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;

public final class Registration {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(
        Registries.CREATIVE_MODE_TAB,
        ModConstants.MOD_ID
    );
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE,
        ModConstants.MOD_ID
    );
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
        Registries.MENU,
        ModConstants.MOD_ID
    );

    public static final DeferredBlock<MeterBlock> METER_BLOCK = registerBlock(Constants.METER_ID, MeterBlock::new);
    public static final DeferredBlock<MonitorBlock> MONITOR_BLOCK = registerBlock(Constants.MONITOR_ID, MonitorBlock::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeterBlockEntity>> METER_BLOCK_ENTITY = registerBlockEntity(
        METER_BLOCK,
        MeterBlockEntity::new
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MonitorBlockEntity>> MONITOR_BLOCK_ENTITY = registerBlockEntity(
        MONITOR_BLOCK,
        MonitorBlockEntity::new
    );

    public static final DeferredHolder<MenuType<?>, MenuType<MeterMenu>> METER_MENU = MENUS.register(Constants.METER_ID, () ->
        IMenuTypeExtension.create((wid, inventory, data) -> {
            var entity = (MeterBlockEntity) inventory.player.level().getBlockEntity(data.readBlockPos());
            return new MeterMenu(entity, wid);
        })
    );
    public static final DeferredHolder<MenuType<?>, MenuType<MonitorMenu>> MONITOR_MENU = registerMenu(MONITOR_BLOCK, MonitorMenu::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
        "tab", () -> CreativeModeTab.builder()
            .title(Utils.translate("itemGroup", "tab"))
            .icon(METER_BLOCK::toStack)
            .noScrollBar()
            .displayItems((features, output) -> output.acceptAll(List.of(METER_BLOCK.toStack(), MONITOR_BLOCK.toStack())))
            .build()
    );

    private Registration() {}

    public static void init(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(String id, Function<BlockBehaviour.Properties, B> factory) {
        var block = BLOCKS.registerBlock(
            id,
            factory,
            BlockBehaviour.Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL)
        );
        ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    @SuppressWarnings("DataFlowIssue")
    private static <E extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<E>> registerBlockEntity(
        DeferredBlock<?> block, BlockEntityType.BlockEntitySupplier<E> factory
    ) {
        return BLOCK_ENTITIES.register(block.getId().getPath(), () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <M extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<M>> registerMenu(
        DeferredBlock<?> block, MenuType.MenuSupplier<M> factory
    ) {
        return MENUS.register(block.getId().getPath(), () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
    }
}
