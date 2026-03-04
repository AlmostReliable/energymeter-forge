package com.almostreliable.energymeter.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface EnumExtension<E extends Enum<E>> {

    Map<Class<?>, Enum<?>[]> CACHE = new ConcurrentHashMap<>();

    default E next() {
        E[] values = getEnumConstants();
        return values[(ordinal() + 1) % values.length];
    }

    default E previous() {
        E[] values = getEnumConstants();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

    @SuppressWarnings("unchecked")
    default E[] getEnumConstants() {
        Class<?> type = ((Enum<?>) this).getDeclaringClass();
        return (E[]) CACHE.computeIfAbsent(type, clazz -> (E[]) clazz.getEnumConstants());
    }

    int ordinal();
}
