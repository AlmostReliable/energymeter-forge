package dev.rlnt.energymeter.core;

import static dev.rlnt.energymeter.core.Constants.METER_ID;
import static dev.rlnt.energymeter.core.Constants.MOD_ID;

import dev.rlnt.energymeter.meter.MeterBlock;
import dev.rlnt.energymeter.meter.MeterContainer;
import dev.rlnt.energymeter.meter.MeterEntity;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

    private static <E extends IForgeRegistryEntry<E>> DeferredRegister<E> createRegistry(IForgeRegistry<E> registry) {
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
            return new ItemStack(Blocks.METER.get());
        }
    }

    private static class Blocks {

        private static final DeferredRegister<Block> REGISTRY = createRegistry(ForgeRegistries.BLOCKS);
        private static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
        private static final RegistryObject<MeterBlock> METER = register(METER_ID, MeterBlock::new);

        private Blocks() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <B extends MeterBlock> RegistryObject<B> register(String id, Supplier<B> supplier) {
            RegistryObject<B> result = REGISTRY.register(id, supplier);
            ITEMS.register(id, () -> new BlockItem(result.get(), new Item.Properties().tab(TAB)));
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
        private static <E extends MeterEntity, B extends MeterBlock> RegistryObject<BlockEntityType<E>> register(
            String id,
            RegistryObject<B> block,
            BlockEntitySupplier<E> entity
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(id, () -> BlockEntityType.Builder.of(entity, block.get()).build(null));
        }

        public static final RegistryObject<BlockEntityType<MeterEntity>> METER = register(
            METER_ID,
            Blocks.METER,
            MeterEntity::new
        );
    }

    public static class Containers {

        private static final DeferredRegister<MenuType<?>> REGISTRY = createRegistry(ForgeRegistries.CONTAINERS);

        private Containers() {
            throw new IllegalStateException(EXCEPTION_MESSAGE);
        }

        @SuppressWarnings("SameParameterValue")
        private static <C extends MeterContainer> RegistryObject<MenuType<C>> register(
            String id,
            BiFunction<MeterEntity, Integer, C> constructor
        ) {
            return REGISTRY.register(
                id,
                () ->
                    IForgeContainerType.create((containerID, inventory, data) -> {
                        MeterEntity entity = (MeterEntity) inventory.player.level.getBlockEntity(data.readBlockPos());
                        return constructor.apply(entity, containerID);
                    })
            );
        }

        public static final RegistryObject<MenuType<MeterContainer>> METER = register(METER_ID, MeterContainer::new);
    }
}
