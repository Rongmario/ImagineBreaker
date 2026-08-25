package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@Isolated
public class UnsafeTest {

    @Test
    public void unsafeSingleton() throws ReflectiveOperationException {
        Unsafe unsafe = Index.get().unsafe();
        Assertions.assertNotNull(unsafe);
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Assertions.assertSame(theUnsafe.get(null), unsafe);
    }

    @Test
    public void internalUnsafePresent() {
        Object internal = Index.get().internalUnsafe();
        Assertions.assertNotNull(internal);
        Assertions.assertEquals("jdk.internal.misc.Unsafe", internal.getClass().getName());
    }

}
