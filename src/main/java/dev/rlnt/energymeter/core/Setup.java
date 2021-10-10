package dev.rlnt.energymeter.core;

import static dev.rlnt.energymeter.core.Constants.METER_ID;
import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Setup {

    private static final Tab TAB = new Tab(MOD_ID);

    private Setup() {}

    private static <E extends IForgeRegistryEntry<E>> DeferredRegister<E> createRegistry(IForgeRegistry<E> registry) {
        return DeferredRegister.create(registry, MOD_ID);
    }

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Blocks.ITEMS.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    private static class Tab extends ItemGroup {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.METER_BLOCK.get());
        }
    }

    static class Blocks {

        private static final DeferredRegister<Block> REGISTRY = createRegistry(ForgeRegistries.BLOCKS);
        private static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
        private static final RegistryObject<MeterBlock> METER_BLOCK = register(METER_ID, MeterBlock::new);

        private Blocks() {}

        @SuppressWarnings("SameParameterValue")
        private static <B extends MeterBlock> RegistryObject<B> register(String id, Supplier<B> supplier) {
            RegistryObject<B> result = REGISTRY.register(id, supplier);
            ITEMS.register(id, () -> new BlockItem(result.get(), new Item.Properties().tab(TAB)));
            return result;
        }
    }

    public static class Tiles {

        private static final DeferredRegister<TileEntityType<?>> REGISTRY = createRegistry(
            ForgeRegistries.TILE_ENTITIES
        );

        private Tiles() {}

        @SuppressWarnings("SameParameterValue")
        private static <T extends MeterTile, B extends MeterBlock> RegistryObject<TileEntityType<T>> register(
            String id,
            RegistryObject<B> block,
            Function<BlockState, T> constructor
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(
                id,
                () ->
                    TileEntityType.Builder
                        .of(() -> constructor.apply(block.get().defaultBlockState()), block.get())
                        .build(null)
            );
        }

        public static final RegistryObject<TileEntityType<MeterTile>> METER = register(
            METER_ID,
            Blocks.METER_BLOCK,
            MeterTile::new
        );
    }

    public static class Containers {

        private static final DeferredRegister<ContainerType<?>> REGISTRY = createRegistry(ForgeRegistries.CONTAINERS);

        private Containers() {}

        @SuppressWarnings("SameParameterValue")
        private static <C extends MeterContainer> RegistryObject<ContainerType<C>> register(
            String id,
            BiFunction<MeterTile, Integer, C> constructor
        ) {
            return REGISTRY.register(
                id,
                () ->
                    IForgeContainerType.create((containerID, inventory, data) -> {
                        MeterTile tile = (MeterTile) inventory.player.level.getBlockEntity(data.readBlockPos());
                        return constructor.apply(tile, containerID);
                    })
            );
        }

        public static final RegistryObject<ContainerType<MeterContainer>> METER_CONTAINER = register(
            METER_ID,
            MeterContainer::new
        );
    }
}
