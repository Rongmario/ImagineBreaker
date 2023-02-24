package zone.rong.imaginebreaker;

import jdk.internal.reflect.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

class ReflectionContainer {

    /**
     * For internal use
     */
    static void internal$openAllBaseModulesReflectively() {
        try {
            // Get java.base as a Module
            final Module javaBaseModule = ImagineBreaker.findBootModule("java.base").orElseThrow(RuntimeException::new);
            // Roundabout way as Module.class is protected with reflection filters
            Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            Module everyoneModule = null;
            for (Field field : (Field[]) getDeclaredFields0.invoke(Module.class, false)) {
                if ("EVERYONE_MODULE".equals(field.getName())) {
                    field.setAccessible(true);
                    everyoneModule = (Module) field.get(null);
                }
            }
            // Retrieve ReflectionData::exports map
            Field reflectionData$exports = Class.forName("java.lang.Module$ReflectionData").getDeclaredField("exports");
            reflectionData$exports.setAccessible(true);
            Object exports = reflectionData$exports.get(null);
            if (everyoneModule != null) {
                ImagineBreaker.internal$openModules(javaBaseModule, everyoneModule, exports);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static void internal$removeAllReflectionFiltersReflectively() {
        try {
            Field fieldFilterMapField = null;
            Field methodFilterMapField = null;
            Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            for (Field field : (Field[]) getDeclaredFields0.invoke(Reflection.class, false)) {
                switch (field.getName()) {
                    case "fieldFilterMap" -> fieldFilterMapField = field;
                    case "methodFilterMap" -> methodFilterMapField = field;
                }
            }
            fieldFilterMapField.setAccessible(true);
            methodFilterMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Class<?>, Object> fieldFilterMap = (Map<Class<?>, Object>) fieldFilterMapField.get(null);
            @SuppressWarnings("unchecked")
            Map<Class<?>, Object> methodFilterMap = (Map<Class<?>, Object>) methodFilterMapField.get(null);
            fieldFilterMapField.set(null, null);
            methodFilterMapField.set(null, null);
            Field reflectionDataField = null;
            for (Field field : (Field[]) getDeclaredFields0.invoke(Class.class, false)) {
                if ("reflectionData".equals(field.getName())) {
                    reflectionDataField = field;
                    reflectionDataField.setAccessible(true);
                    break;
                }
            }
            if (fieldFilterMap != null) {
                // Go through every class and clear all of their ReflectionData caches
                for (Class<?> clazz : fieldFilterMap.keySet()) {
                    reflectionDataField.set(clazz, null);
                }
            }
            if (methodFilterMap != null) {
                // Go through every class and clear all of their ReflectionData caches
                for (Class<?> clazz : methodFilterMap.keySet()) {
                    reflectionDataField.set(clazz, null);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
