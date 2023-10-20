package com.github.almostreliable.energymeter.core;

import com.almostreliable.energymeter.EnergyMeterConstants;
import com.github.almostreliable.energymeter.meter.MeterBlock;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterEntity;
import com.github.almostreliable.energymeter.util.TextUtils;
import com.github.almostreliable.energymeter.util.TypeEnums;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.github.almostreliable.energymeter.core.Constants.METER_ID;

public final class Setup {

    private Setup() {}

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Blocks.ITEMS.register(modEventBus);
        Entities.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    private static <E> DeferredRegister<E> createRegistry(IForgeRegistry<E> registry) {
        return DeferredRegister.create(registry, EnergyMeterConstants.MOD_ID);
    }

    public static final class Tab {

        private static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            TextUtils.getRL("tab")
        );
        private static final CreativeModeTab TAB = CreativeModeTab
            .builder()
            .title(TextUtils.translate(TypeEnums.TRANSLATE_TYPE.LABEL, "itemGroup"))
            .icon(() -> new ItemStack(Blocks.METER.get()))
            .noScrollBar()
            .build();

        private Tab() {}

        public static void initContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == TAB_KEY) {
                event.accept(Blocks.METER);
            }
        }

        public static void registerTab(RegisterEvent event) {
            event.register(Registries.CREATIVE_MODE_TAB, TAB_KEY.location(), () -> TAB);
        }
    }

    public static final class Entities {

        private static final DeferredRegister<BlockEntityType<?>> REGISTRY
            = createRegistry(ForgeRegistries.BLOCK_ENTITY_TYPES);

        private Entities() {}

        @SuppressWarnings("SameParameterValue")
        private static <E extends MeterEntity, B extends MeterBlock> RegistryObject<BlockEntityType<E>> register(
            String id, RegistryObject<B> block, BlockEntitySupplier<E> entity
        ) {
            // noinspection ConstantConditions
            return REGISTRY.register(id, () -> Builder.of(entity, block.get()).build(null));
        }

        public static final RegistryObject<BlockEntityType<MeterEntity>> METER = register(METER_ID,
            Blocks.METER,
            MeterEntity::new
        );
    }

    public static final class Containers {

        private static final DeferredRegister<MenuType<?>> REGISTRY = createRegistry(ForgeRegistries.MENU_TYPES);

        private Containers() {}

        @SuppressWarnings("SameParameterValue")
        private static <C extends MeterContainer> RegistryObject<MenuType<C>> register(
            String id, BiFunction<? super MeterEntity, ? super Integer, ? extends C> constructor
        ) {
            return REGISTRY.register(id, () -> IForgeMenuType.create((containerID, inventory, data) -> {
                var entity = (MeterEntity) inventory.player.level().getBlockEntity(data.readBlockPos());
                return constructor.apply(entity, containerID);
            }));
        }

        public static final RegistryObject<MenuType<MeterContainer>> METER = register(METER_ID, MeterContainer::new);
    }

    private static final class Blocks {

        private static final DeferredRegister<Block> REGISTRY = createRegistry(ForgeRegistries.BLOCKS);
        private static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
        private static final RegistryObject<MeterBlock> METER = register(METER_ID, MeterBlock::new);

        @SuppressWarnings("SameParameterValue")
        private static <B extends MeterBlock> RegistryObject<B> register(String id, Supplier<B> supplier) {
            var result = REGISTRY.register(id, supplier);
            ITEMS.register(id, () -> new BlockItem(result.get(), new Properties()));
            return result;
        }
    }
}
