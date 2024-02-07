package zone.rong.imaginebreaker;

import jdk.internal.reflect.Reflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

public class RemoveMethodFiltersTest {

    private static class Subject {

        private static int subjectMethod() {
            return -1;
        }

    }

    @Test
    public void removeMethodFilters() {
        Reflection.registerMethodsToFilter(Subject.class, Set.of("subjectMethod"));
        Assertions.assertThrows(NoSuchMethodException.class, this::retrieveSubjectMethod);
        ImagineBreaker.removeMethodReflectionFilters();
        Assertions.assertDoesNotThrow(this::retrieveSubjectMethod);
    }

    private Method retrieveSubjectMethod() throws NoSuchMethodException {
        return Subject.class.getDeclaredMethod("subjectMethod");
    }

}
