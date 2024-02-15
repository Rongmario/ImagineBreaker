package zone.rong.imaginebreaker;

import jdk.internal.reflect.Reflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class FilterTest {

    private static class Subject {

        private static int subjectField = -1;

        private static int subjectMethod() {
            return -1;
        }

    }

    @Test
    public void removeFieldFilters() throws ReflectiveOperationException {
        registerFieldFilter();
        Assertions.assertThrows(NoSuchFieldException.class, this::retrieveSubjectField);
        ImagineBreaker.wipeFieldFilters();
        Assertions.assertDoesNotThrow(this::retrieveSubjectMethod);
    }

    @Test
    public void removeMethodFilters() throws ReflectiveOperationException {
        registerMethodFilter();
        Assertions.assertThrows(NoSuchMethodException.class, this::retrieveSubjectMethod);
        ImagineBreaker.wipeMethodFilters();
        Assertions.assertDoesNotThrow(this::retrieveSubjectMethod);
    }

    private void registerFieldFilter() throws ReflectiveOperationException {
        try {
            Method method = Reflection.class.getDeclaredMethod("registerFieldsToFilter", Class.class, String[].class);
            method.invoke(null, Subject.class, new String[] { "subjectField" });
        } catch (Exception e) {
            Method method = Reflection.class.getDeclaredMethod("registerFieldsToFilter", Class.class, Set.class);
            method.invoke(null, Subject.class, Set.of("subjectField"));
        }
    }

    private void registerMethodFilter() throws ReflectiveOperationException {
        try {
            Method method = Reflection.class.getDeclaredMethod("registerMethodsToFilter", Class.class, String[].class);
            method.invoke(null, Subject.class, new String[] { "subjectMethod" });
        } catch (Exception e) {
            Method method = Reflection.class.getDeclaredMethod("registerMethodsToFilter", Class.class, Set.class);
            method.invoke(null, Subject.class, Set.of("subjectMethod"));
        }
    }

    private Field retrieveSubjectField() throws NoSuchFieldException {
        return Subject.class.getDeclaredField("subjectField");
    }

    private Method retrieveSubjectMethod() throws NoSuchMethodException {
        return Subject.class.getDeclaredMethod("subjectMethod");
    }

}
