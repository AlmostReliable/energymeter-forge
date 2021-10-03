package dev.rlnt.energymeter.core;

import static dev.rlnt.energymeter.core.Constants.METER_ID;
import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterTile;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Setup {

    static final Tab TAB = new Tab(MOD_ID);
    private static final String EXCEPTION_MESSAGE = "Utility class";

    private Setup() {
        throw new IllegalStateException(EXCEPTION_MESSAGE);
    }

    private static <T extends IForgeRegistryEntry<T>> DeferredRegister<T> createRegistry(
        final IForgeRegistry<T> registry
    ) {
        return DeferredRegister.create(registry, MOD_ID);
    }

    public static void init(final IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Blocks.ITEMS.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    private static class Tab extends ItemGroup {

        Tab(final String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.METER_BLOCK.get());
        }
    }

    static class Blocks {

        static final DeferredRegister<Block> REGISTRY = createRegistry(ForgeRegistries.BLOCKS);
        static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
        private static final RegistryObject<Block> METER_BLOCK = registerBlock(METER_ID, MeterBlock::new);

        private Blocks() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <T extends Block> RegistryObject<T> registerBlock(final String name, final Supplier<T> block) {
            final RegistryObject<T> result = REGISTRY.register(name, block);
            ITEMS.register(name, () -> new BlockItem(result.get(), new Item.Properties().tab(TAB)));
            return result;
        }
    }

    public static class Tiles {

        static final DeferredRegister<TileEntityType<?>> REGISTRY = createRegistry(ForgeRegistries.TILE_ENTITIES);
        public static final RegistryObject<TileEntityType<MeterTile>> METER_TILE = registerTile(
            METER_ID,
            () -> new MeterTile(Blocks.METER_BLOCK.get().defaultBlockState()),
            Blocks.METER_BLOCK
        );

        private Tiles() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <T extends TileEntity, B extends Block> RegistryObject<TileEntityType<T>> registerTile(
            final String name,
            final Supplier<T> tile,
            final RegistryObject<B> block
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(name, () -> TileEntityType.Builder.of(tile, block.get()).build(null));
        }
    }

    public static class Containers {

        private static final DeferredRegister<ContainerType<?>> REGISTRY = createRegistry(ForgeRegistries.CONTAINERS);
        public static final RegistryObject<ContainerType<MeterContainer>> METER_CONTAINER = REGISTRY.register(
            METER_ID,
            () ->
                IForgeContainerType.create(
                    (
                        (windowId, inv, data) -> {
                            BlockPos pos = data.readBlockPos();
                            MeterTile tile = (MeterTile) inv.player.level.getBlockEntity(pos);
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
