package testmod;

import net.minecraft.gametest.framework.GameTestAssertException;

import org.jetbrains.annotations.Nullable;

public final class TestUtils {

    private TestUtils() {}

    public static void assertNull(@Nullable Object o, String failureMessage) {
        if (o != null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertNotNull(@Nullable Object o, String failureMessage) {
        if (o == null) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static void assertInstanceOf(Object o, Class<?> clazz, String failureMessage) {
        if (!clazz.isInstance(o)) {
            throw new GameTestAssertException(failureMessage);
        }
    }

    public static <T> void assertSameObject(T o1, T o2, String failureMessage) {
        // noinspection ObjectEquality
        if (o1 != o2) {
            throw new GameTestAssertException(failureMessage);
        }
    }
}
