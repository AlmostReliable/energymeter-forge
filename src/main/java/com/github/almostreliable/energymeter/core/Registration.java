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
import com.github.almostreliable.energymeter.data.EnergyMeterLang;
import com.github.almostreliable.energymeter.menu.MeterMenu;
import com.github.almostreliable.energymeter.menu.MonitorMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;

public final class Registration {

    // @formatter:off

    // registries
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModConstants.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModConstants.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModConstants.MOD_ID);

    // blocks
    public static final DeferredBlock<MeterBlock> METER_BLOCK = registerBlock(Constants.METER_ID, "Energy Meter", MeterBlock::new);
    public static final DeferredBlock<MonitorBlock> MONITOR_BLOCK = registerBlock(Constants.MONITOR_ID, "External Monitor", MonitorBlock::new);

    // block entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeterBlockEntity>> METER_BLOCK_ENTITY = registerBlockEntity(METER_BLOCK, MeterBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MonitorBlockEntity>> MONITOR_BLOCK_ENTITY = registerBlockEntity(MONITOR_BLOCK, MonitorBlockEntity::new);

    // menus
    public static final DeferredHolder<MenuType<?>, MenuType<MeterMenu>> METER_MENU = registerMenu(METER_BLOCK, MeterMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<MonitorMenu>> MONITOR_MENU = registerMenu(MONITOR_BLOCK, MonitorMenu::new);

    // creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
        "tab", () -> CreativeModeTab.builder()
            .title(EnergyMeterLang.LangEntry.of("tab", "main", ModConstants.MOD_NAME).get())
            .icon(METER_BLOCK::toStack)
            .noScrollBar()
            .displayItems((features, output) -> output.acceptAll(List.of(METER_BLOCK.toStack(), MONITOR_BLOCK.toStack())))
            .build()
    );

    // @formatter:on

    private Registration() {}

    public static void init(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);

        modEventBus.addListener(Registration::registerCapabilities);
    }

    public static Iterable<Block> getKnownBlocks() {
        return BLOCKS.getEntries().stream().map(e -> (Block) e.value()).toList();
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, METER_BLOCK_ENTITY.get(), MeterBlockEntity::getEnergyStorage);
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(
        String id, String name, Function<BlockBehaviour.Properties, B> factory
    ) {
        var block = BLOCKS.registerBlock(
            id,
            factory,
            BlockBehaviour.Properties.of().strength(2f).mapColor(MapColor.METAL).sound(SoundType.METAL)
        );
        ITEMS.registerSimpleBlockItem(block);
        // EnergyMeterLang.LangEntry.of("block", id, getNameOrFormatId(id, name));
        EnergyMeterLang.LangEntry.of("item", id, name);
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
