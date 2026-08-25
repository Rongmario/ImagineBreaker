package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.util.function.IntSupplier;

@Isolated
public class DefineClassTest {

    @Test
    public void defineClassInTestLoader() throws Exception {
        ImagineBreaker ib = Index.get();
        String name = ClassBytes.uniqueName();
        Class<?> defined = ib.defineClass(DefineClassTest.class.getClassLoader(), name, ClassBytes.intSupplier(name, 42));
        Assertions.assertEquals(name, defined.getName());
        Assertions.assertEquals(DefineClassTest.class.getClassLoader(), defined.getClassLoader());
        IntSupplier supplier = (IntSupplier) defined.getDeclaredConstructor().newInstance();
        Assertions.assertEquals(42, supplier.getAsInt());
    }

    @Test
    public void defineHiddenClassHostedByThis() throws Exception {
        ImagineBreaker ib = Index.get();
        String name = ClassBytes.uniqueName(DefineClassTest.class);
        Class<?> defined = ib.defineHiddenClass(DefineClassTest.class, ClassBytes.intSupplier(name, 21), true);
        Assertions.assertNotNull(defined);
        IntSupplier supplier = (IntSupplier) defined.getDeclaredConstructor().newInstance();
        Assertions.assertEquals(21, supplier.getAsInt());
    }

    @Test
    public void defineClassRejectsInvalidBytecodeRange() {
        ImagineBreaker ib = Index.get();
        byte[] bytecode = new byte[4];
        Assertions.assertThrows(IndexOutOfBoundsException.class, () ->
                ib.defineClass(DefineClassTest.class.getClassLoader(), null, bytecode, -1, 1, null));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () ->
                ib.defineClass(null, null, bytecode, 2, 3, null));
    }

    @Test
    public void defineHiddenClassRejectsNullBeforeUnsafeFallback() {
        ImagineBreaker ib = Index.get();
        Assertions.assertThrows(NullPointerException.class, () -> ib.defineHiddenClass(null, new byte[0], false));
        Assertions.assertThrows(NullPointerException.class, () -> ib.defineHiddenClass(DefineClassTest.class, null, false));
    }

    @Test
    public void ensureInitializedRunsClinit() {
        ImagineBreaker ib = Index.get();
        java.lang.reflect.Field initialized = ib.declaredField(Lazy.class, "initialized");
        Assertions.assertEquals(Boolean.FALSE, ib.get(null, initialized));
        ib.ensureInitialized(Lazy.class);
        Assertions.assertEquals(Boolean.TRUE, ib.get(null, initialized));
    }

    static final class Lazy {

        static boolean initialized;

        static {
            initialized = true;
        }

    }

}
