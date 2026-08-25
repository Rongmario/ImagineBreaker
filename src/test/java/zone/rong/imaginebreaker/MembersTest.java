package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

@Isolated
public class MembersTest {

    @Test
    public void declaredFieldIgnoresFilter() throws Throwable {
        ImagineBreaker ib = Index.get();
        ib.clearFieldFilters();
        registerFieldFilter(Subject.class, "hidden");
        Assertions.assertThrows(NoSuchFieldException.class, () -> Subject.class.getDeclaredField("hidden"));
        Field field = ib.declaredField(Subject.class, "hidden");
        Assertions.assertNotNull(field);
        Assertions.assertEquals("hidden", field.getName());
        ib.clearFieldFilters();
    }

    @Test
    public void declaredFieldSeesLookupInternalsWithoutWipe() {
        ImagineBreaker ib = Index.get();
        Field implLookup = ib.declaredField(Lookup.class, "IMPL_LOOKUP");
        Assertions.assertNotNull(implLookup);
        Assertions.assertSame(ib.trustedLookup(), ib.get(null, implLookup));
    }

    @Test
    public void declaredMethodIgnoresFilter() throws Throwable {
        ImagineBreaker ib = Index.get();
        ib.clearMethodFilters();
        registerMethodFilter(Subject.class, "secret");
        Assertions.assertThrows(NoSuchMethodException.class, () -> Subject.class.getDeclaredMethod("secret"));
        Method method = ib.declaredMethod(Subject.class, "secret");
        Assertions.assertNotNull(method);
        Assertions.assertEquals(7, ib.invoke(null, method));
        ib.clearMethodFilters();
    }

    @Test
    public void declaredClassFindsNested() {
        Class<?> nested = Index.get().declaredClass(MembersTest.class, "Subject");
        Assertions.assertEquals(Subject.class, nested);
    }

    @Test
    public void callerClassDepthZeroIsThisTest() {
        Assertions.assertEquals(MembersTest.class, Index.get().callerClass(0));
    }

    private static void registerFieldFilter(Class<?> owner, String name) throws Throwable {
        registerFilter("registerFieldsToFilter", owner, name);
    }

    private static void registerMethodFilter(Class<?> owner, String name) throws Throwable {
        registerFilter("registerMethodsToFilter", owner, name);
    }

    private static void registerFilter(String method, Class<?> owner, String name) throws Throwable {
        Class<?> reflection = Class.forName("jdk.internal.reflect.Reflection");
        if (Runtime.version().major() >= 12) {
            Index.get().trustedLookup()
                    .findStatic(reflection, method, MethodType.methodType(void.class, Class.class, Set.class))
                    .invoke(owner, Set.of(name));
        } else {
            Index.get().trustedLookup()
                    .findStatic(reflection, method, MethodType.methodType(void.class, Class.class, String[].class))
                    .invoke(owner, new String[] { name });
        }
        try {
            Index.get().trustedLookup()
                    .findVarHandle(Class.class, "reflectionData", SoftReference.class)
                    .setVolatile(owner, (SoftReference<?>) null);
        } catch (NoSuchFieldException semeru) {
            Index.get().trustedLookup()
                    .findSetter(Class.class, "reflectCache", Class.forName("java.lang.Class$ReflectCache"))
                    .invoke(owner, null);
        }
    }

    static final class Subject {

        private static final int hidden = 1;

        private static int secret() {
            return 7;
        }

    }

}
