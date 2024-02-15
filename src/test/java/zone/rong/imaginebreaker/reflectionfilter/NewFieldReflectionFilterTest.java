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
public class NewFieldReflectionFilterTest {

    @Test
    public void removeFieldFilters() {
        ImagineBreaker.openBootModule("java.base");
        registerFieldFilter();
        Assertions.assertThrows(NoSuchFieldException.class, Subject::retrieveSubjectField);
        ImagineBreaker.wipeFieldFilters();
        Assertions.assertDoesNotThrow(Subject::retrieveSubjectField);
    }

    private void registerFieldFilter() {
        try {
            Method method = Reflection.class.getDeclaredMethod("registerFieldsToFilter", Class.class, Set.class);
            method.invoke(null, Subject.class, Set.of("subjectField"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
