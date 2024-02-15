package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LookupTest {

    @Test
    @EnabledForJreRange(min = JRE.JAVA_9, max = JRE.JAVA_16)
    public void lookupOld() {
        MethodHandles.Lookup lookup = ImagineBreaker.lookup();
        Assertions.assertEquals(lookup.lookupModes(), ImagineBreaker.IS_OPEN_J9 ? 128 : 63);
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_17)
    public void lookupNew() {
        MethodHandles.Lookup lookup = ImagineBreaker.lookup();
        Assertions.assertEquals(lookup.lookupModes(), 127);
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
