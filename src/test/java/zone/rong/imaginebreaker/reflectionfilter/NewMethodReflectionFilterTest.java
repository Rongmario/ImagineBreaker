package zone.rong.imaginebreaker.reflectionfilter;

import jdk.internal.reflect.Reflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.lang.reflect.Method;
import java.util.Set;

@Isolated
@EnabledForJreRange(min = JRE.JAVA_12)
public class NewMethodReflectionFilterTest {

    @Test
    public void removeMethodFilters() {
        ImagineBreaker.openBootModule("java.base");
        registerMethodFilter();
        Assertions.assertThrows(NoSuchMethodException.class, Subject::retrieveSubjectMethod);
        ImagineBreaker.wipeMethodFilters();
        Assertions.assertDoesNotThrow(Subject::retrieveSubjectMethod);
    }

    private void registerMethodFilter() {
        try {
            Method method = Reflection.class.getDeclaredMethod("registerMethodsToFilter", Class.class, Set.class);
            method.invoke(null, Subject.class, Set.of("subjectMethod"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
