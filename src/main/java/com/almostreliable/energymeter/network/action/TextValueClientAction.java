package com.almostreliable.energymeter.network.action;

import com.almostreliable.energymeter.menu.SynchronizedContainerMenu;
import com.almostreliable.energymeter.network.action.ClientActionRegistry.Decoder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TextValueClientAction<E extends BlockEntity, M extends SynchronizedContainerMenu<E>, T extends Enum<T> & TextValueClientAction.ValueConsumer<E>>
    implements ClientAction<M> {

    private static final String TEXT_BOX_ID = "text_box";
    private static final String VALUE_ID = "value";

    private final ResourceLocation id;
    private final T textBox;
    private final long value;

    public TextValueClientAction(ResourceLocation id, T textBox, long value) {
        this.id = id;
        this.textBox = textBox;
        this.value = value;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public void encode(CompoundTag tag) {
        tag.putInt(TEXT_BOX_ID, textBox.ordinal());
        tag.putLong(VALUE_ID, value);
    }

    @Override
    public void handleServer(M menu, ServerPlayer player) {
        textBox.updateValue(menu.getBlockEntity(), value);
    }

    public static <E extends BlockEntity, M extends SynchronizedContainerMenu<E>,
        T extends Enum<T> & ValueConsumer<E>> Decoder<TextValueClientAction<E, M, T>>
    decoder(ResourceLocation id, Class<T> enumClass) {
        T[] enumValues = enumClass.getEnumConstants();

        return tag -> {
            int ordinal = tag.getInt(TEXT_BOX_ID);
            if (ordinal < 0 || ordinal >= enumValues.length) {
                throw new IllegalStateException("invalid enum ordinal: " + ordinal);
            }
            long value = tag.getLong(VALUE_ID);
            return new TextValueClientAction<>(id, enumValues[ordinal], value);
        };
    }

    @FunctionalInterface
    public interface ValueConsumer<T extends BlockEntity> {

        void updateValue(T blockEntity, long value);
    }
}
