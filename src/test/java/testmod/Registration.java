package testmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class Registration {

    // @formatter:off

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TestMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TestMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TestMod.MOD_ID);

    public static final DeferredBlock<EnergyBlock> ENERGY_BLOCK = registerBlock("energy_block", EnergyBlock::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBlockEntity>> ENERGY_BLOCK_ENTITY = registerBlockEntity(ENERGY_BLOCK, EnergyBlockEntity::new);

    // @formatter:on

    private Registration() {}

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(Registration::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ENERGY_BLOCK_ENTITY.get(),
            EnergyBlockEntity::getEnergyCapability
        );
    }

    private static <B extends Block> DeferredBlock<B> registerBlock(
        String id, Function<BlockBehaviour.Properties, B> factory
    ) {
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
}
