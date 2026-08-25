package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

@Isolated
public class FilterClearTest {

    @Test
    public void clearFieldFilters() throws Throwable {
        Index.get().clearFieldFilters();
        registerFieldFilter(Subject.class, "subjectField");
        Assertions.assertThrows(NoSuchFieldException.class, Subject::field);
        Index.get().clearFieldFilters();
        Field recovered = Assertions.assertDoesNotThrow(Subject::field);
        Assertions.assertEquals("subjectField", recovered.getName());
    }

    @Test
    public void clearMethodFilters() throws Throwable {
        Index.get().clearMethodFilters();
        registerMethodFilter(Subject.class, "subjectMethod");
        Assertions.assertThrows(NoSuchMethodException.class, Subject::method);
        Index.get().clearMethodFilters();
        Method recovered = Assertions.assertDoesNotThrow(Subject::method);
        Assertions.assertEquals("subjectMethod", recovered.getName());
    }

    @Test
    public void lookupInternalsVisibleAfterWipe() {
        Index.get().clearFieldFilters();
        Assertions.assertDoesNotThrow(() -> LookupInternals.allowedModes());
        Assertions.assertDoesNotThrow(() -> LookupInternals.implLookup());
        Assertions.assertDoesNotThrow(() -> Class.class.getDeclaredField("module"));
    }

    private static void registerFieldFilter(Class<?> owner, String name) throws Throwable {
        registerFilter("registerFieldsToFilter", owner, name);
    }

    private static void registerMethodFilter(Class<?> owner, String name) throws Throwable {
        registerFilter("registerMethodsToFilter", owner, name);
    }

    private static void registerFilter(String method, Class<?> owner, String name) throws Throwable {
        Class<?> reflection = reflectionClass();
        if (Runtime.version().major() >= 12) {
            Index.get().trustedLookup()
                    .findStatic(reflection, method, MethodType.methodType(void.class, Class.class, Set.class))
                    .invoke(owner, Set.of(name));
        } else {
            Index.get().trustedLookup()
                    .findStatic(reflection, method, MethodType.methodType(void.class, Class.class, String[].class))
                    .invoke(owner, new String[] { name });
        }
        // Discovery / earlier tests may have already filled Class.reflectionData.
        evictReflectionCache(owner);
    }

    private static void evictReflectionCache(Class<?> owner) throws Throwable {
        try {
            VarHandle reflectionData = Index.get().trustedLookup()
                    .findVarHandle(Class.class, "reflectionData", SoftReference.class);
            reflectionData.setVolatile(owner, (SoftReference<?>) null);
        } catch (NoSuchFieldException semeru) {
            MethodHandle setCache = Index.get().trustedLookup()
                    .findSetter(Class.class, "reflectCache", Class.forName("java.lang.Class$ReflectCache"));
            setCache.invoke(owner, null);
        }
    }

    private static Class<?> reflectionClass() throws ClassNotFoundException {
        return Class.forName("jdk.internal.reflect.Reflection");
    }

    static final class Subject {
        private static int subjectField = -1;

        private static int subjectMethod() {
            return -1;
        }

        static Field field() throws NoSuchFieldException {
            return Subject.class.getDeclaredField("subjectField");
        }

        static Method method() throws NoSuchMethodException {
            return Subject.class.getDeclaredMethod("subjectMethod");
        }
    }

    static final class LookupInternals {
        static Field allowedModes() throws NoSuchFieldException {
            return java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("allowedModes");
        }

        static Field implLookup() throws NoSuchFieldException {
            return java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        }
    }

}
