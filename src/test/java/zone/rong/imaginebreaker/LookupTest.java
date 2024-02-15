package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LookupTest {

    // Can be 63 or 127
    private static final Try<Object> EXPECTED_MODES = ReflectionUtils.tryToReadFieldValue(MethodHandles.Lookup.class, "ALL_MODES", null);

    @Test
    public void lookup() throws Exception {
        MethodHandles.Lookup lookup = ImagineBreaker.lookup();
        Assertions.assertTrue(lookup.lookupModes() == 63 || lookup.lookupModes() == 127);
    }

    @Test
    public void unreflectAndInvoke() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = ImagineBreaker.lookup();
        Field class$module = Class.class.getDeclaredField("module");
        Method setAccessible0 = AccessibleObject.class.getDeclaredMethod("setAccessible0", boolean.class);

        Assertions.assertThrows(IllegalAccessException.class, () -> setAccessible0.invoke(class$module, true));
        Assertions.assertFalse(class$module.isAccessible());

        MethodHandle setAccessible0$Handle = lookup.unreflect(setAccessible0);

        Assertions.assertDoesNotThrow(() -> setAccessible0$Handle.invoke(class$module, true));
        Assertions.assertTrue(class$module.isAccessible());
    }

}
