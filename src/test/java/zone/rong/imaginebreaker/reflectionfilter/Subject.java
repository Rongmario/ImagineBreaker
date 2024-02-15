package zone.rong.imaginebreaker.reflectionfilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class Subject {

    private static int subjectField = -1;

    private static int subjectMethod() {
        return -1;
    }

    static Field retrieveSubjectField() throws NoSuchFieldException {
        return Subject.class.getDeclaredField("subjectField");
    }

    static Method retrieveSubjectMethod() throws NoSuchMethodException {
        return Subject.class.getDeclaredMethod("subjectMethod");
    }

}
