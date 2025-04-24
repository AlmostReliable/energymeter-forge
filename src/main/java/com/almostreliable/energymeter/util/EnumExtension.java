package com.almostreliable.energymeter.util;

public interface EnumExtension {

    default <E extends Enum<E>> E next() {
        E[] values = getEnumConstants();
        return values[(ordinal() + 1) % values.length];
    }

    default <E extends Enum<E>> E previous() {
        E[] values = getEnumConstants();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

    @SuppressWarnings("unchecked")
    default <E extends Enum<E>> E[] getEnumConstants() {
        return (E[]) getClass().getEnumConstants();
    }

    int ordinal();
}
