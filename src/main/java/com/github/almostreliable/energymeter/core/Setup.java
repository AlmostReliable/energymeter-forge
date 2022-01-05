package com.github.almostreliable.energymeter.core;

import com.github.almostreliable.energymeter.meter.MeterBlock;
import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Setup {
    ;

    private static final Tab TAB = new Tab(Constants.MOD_ID);

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Blocks.ITEMS.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    public enum Tiles {
        ;

        private static final DeferredRegister<TileEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Constants.MOD_ID);

        @SuppressWarnings("SameParameterValue")
        private static <T extends MeterTile, B extends MeterBlock> RegistryObject<TileEntityType<T>> register(
            String id, RegistryObject<B> block, Function<? super BlockState, T> constructor
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(id,
                () -> Builder.of(() -> constructor.apply(null), block.get()).build(null)
            );
        }

        public static final RegistryObject<TileEntityType<MeterTile>> METER = register(Constants.METER_ID,
            Blocks.METER_BLOCK,
            MeterTile::new
        );
    }

    public enum Containers {
        ;

        private static final DeferredRegister<ContainerType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, Constants.MOD_ID);

        @SuppressWarnings("SameParameterValue")
        private static <C extends MeterContainer> RegistryObject<ContainerType<C>> register(
            String id, BiFunction<? super MeterTile, ? super Integer, ? extends C> constructor
        ) {
            return REGISTRY.register(id, () -> IForgeContainerType.create((containerID, inventory, data) -> {
                MeterTile tile = (MeterTile) inventory.player.level.getBlockEntity(data.readBlockPos());
                return constructor.apply(tile, containerID);
            }));
        }

        public static final RegistryObject<ContainerType<MeterContainer>> METER_CONTAINER = register(Constants.METER_ID,
            MeterContainer::new
        );
    }

    private static final class Tab extends ItemGroup {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.METER_BLOCK.get());
        }
    }

    private static class Blocks {

        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Constants.MOD_ID
        );
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            Constants.MOD_ID
        );
        private static final RegistryObject<MeterBlock> METER_BLOCK = register(Constants.METER_ID, MeterBlock::new);

        @SuppressWarnings("SameParameterValue")
        private static <B extends MeterBlock> RegistryObject<B> register(String id, Supplier<? extends B> supplier) {
            RegistryObject<B> result = REGISTRY.register(id, supplier);
            ITEMS.register(id, () -> new BlockItem(result.get(), new Properties().tab(TAB)));
            return result;
        }
    }
}
