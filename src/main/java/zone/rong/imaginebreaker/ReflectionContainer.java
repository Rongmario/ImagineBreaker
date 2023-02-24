package zone.rong.imaginebreaker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

}
