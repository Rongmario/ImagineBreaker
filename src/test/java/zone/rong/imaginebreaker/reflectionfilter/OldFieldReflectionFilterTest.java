package zone.rong.imaginebreaker.reflectionfilter;

import jdk.internal.reflect.Reflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.lang.reflect.Method;

@Isolated
@EnabledForJreRange(min = JRE.JAVA_9, max = JRE.JAVA_11)
public class OldFieldReflectionFilterTest {

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
            Method method = Reflection.class.getDeclaredMethod("registerFieldsToFilter", Class.class, String[].class);
            method.invoke(null, Subject.class, new String[] { "subjectField" });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
