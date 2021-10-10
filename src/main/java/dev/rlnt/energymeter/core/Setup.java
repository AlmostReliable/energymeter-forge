package dev.rlnt.energymeter.core;

import static dev.rlnt.energymeter.core.Constants.METER_ID;
import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Setup {

    private static final Tab TAB = new Tab(MOD_ID);
    private static final String EXCEPTION_MESSAGE = "Utility class";

    private Setup() {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createRegistry(IForgeRegistry<T> registry) {
        return DeferredRegister.create(registry, MOD_ID);
    }

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Blocks.ITEMS.register(modEventBus);
        Entities.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    private static class Tab extends CreativeModeTab {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.METER_BLOCK.get());
        }
    }

    private static class Blocks {

        private static final DeferredRegister<Block> REGISTRY = createRegistry(ForgeRegistries.BLOCKS);
        private static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
        private static final RegistryObject<Block> METER_BLOCK = registerBlock(METER_ID, MeterBlock::new);

        private Blocks() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
            RegistryObject<T> result = REGISTRY.register(name, block);
            ITEMS.register(name, () -> new BlockItem(result.get(), new Item.Properties().tab(TAB)));
            return result;
        }
    }

    public static class Entities {

        private static final DeferredRegister<BlockEntityType<?>> REGISTRY = createRegistry(
            ForgeRegistries.BLOCK_ENTITIES
        );

        private Entities() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <E extends BlockEntity> RegistryObject<BlockEntityType<E>> registerBlockEntity(
            String name,
            BlockEntitySupplier<E> tile,
            RegistryObject<Block> block
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(name, () -> BlockEntityType.Builder.of(tile, block.get()).build(null));
        }

        public static final RegistryObject<BlockEntityType<MeterEntity>> METER_ENTITY = registerBlockEntity(
            METER_ID,
            MeterEntity::new,
            Blocks.METER_BLOCK
        );
    }

    public static class Containers {

        private static final DeferredRegister<MenuType<?>> REGISTRY = createRegistry(ForgeRegistries.CONTAINERS);
        public static final RegistryObject<MenuType<MeterContainer>> METER_CONTAINER = REGISTRY.register(
            METER_ID,
            () ->
                IForgeContainerType.create(
                    (
                        (windowId, inv, data) -> {
                            var pos = data.readBlockPos();
                            MeterEntity tile = (MeterEntity) inv.player.level.getBlockEntity(pos);
                            return new MeterContainer(windowId, Objects.requireNonNull(tile));
                        }
                    )
                )
        );

        private Containers() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }
    }
}
