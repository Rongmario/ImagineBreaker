package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessibleObject;

public class LookupTest {

    @Test
    public void lookup() {
        var lookup = ImagineBreaker.lookup();
        Assertions.assertEquals(127, lookup.lookupModes()); // TRUSTED = -1, ALL_MODES = 127, lookupModes = allowed & 127
    }

    @Test
    public void unreflectAndInvoke() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        var lookup = ImagineBreaker.lookup();
        var class$module = Class.class.getDeclaredField("module");
        var setAccessible0 = AccessibleObject.class.getDeclaredMethod("setAccessible0", boolean.class);

        Assertions.assertThrows(IllegalAccessException.class, () -> setAccessible0.invoke(class$module, true));
        Assertions.assertFalse(class$module.isAccessible());

        var setAccessible0$Handle = lookup.unreflect(setAccessible0);

        Assertions.assertDoesNotThrow(() -> setAccessible0$Handle.invoke(class$module, true));
        Assertions.assertTrue(class$module.isAccessible());
    }

}
