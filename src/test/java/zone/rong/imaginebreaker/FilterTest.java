package zone.rong.imaginebreaker;

import jdk.internal.reflect.Reflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class FilterTest {

    private static class Subject {

        private static int subjectMethod() {
            return -1;
        }

    }

    @Test
    public void removeFieldFilters() {
        Assertions.assertThrows(NoSuchFieldException.class, this::retrieveModifiersField);
        ImagineBreaker.wipeFieldFilters();
        Assertions.assertDoesNotThrow(this::retrieveModifiersField);
    }

    @Test
    public void removeMethodFilters() {
        Reflection.registerMethodsToFilter(Subject.class, Set.of("subjectMethod"));
        Assertions.assertThrows(NoSuchMethodException.class, this::retrieveSubjectMethod);
        ImagineBreaker.wipeMethodFilters();
        Assertions.assertDoesNotThrow(this::retrieveSubjectMethod);
    }

    private Field retrieveModifiersField() throws NoSuchFieldException {
        return Field.class.getDeclaredField("modifiers");
    }

    private Method retrieveSubjectMethod() throws NoSuchMethodException {
        return Subject.class.getDeclaredMethod("subjectMethod");
    }

}
