package zone.rong.imaginebreaker;

import jdk.internal.misc.Unsafe;
import jdk.internal.reflect.Reflection;

import java.util.Map;

class UnsafeContainer {

    /**
     * For internal use
     */
    static void internal$openAllBaseModulesUnsafely() {
        try {
            // Get java.base as a Module
            final Module javaBaseModule = ImagineBreaker.findBootModule("java.base").orElseThrow(RuntimeException::new);
            // jdk.internal.misc.Unsafe is needed here
            Unsafe unsafe = Unsafe.getUnsafe();
            // Grab address to Field
            long everyoneModule$Address = unsafe.objectFieldOffset(Module.class, "EVERYONE_MODULE");
            // Retrieve Field's value, EVERYONE_MODULE as a Module
            Module everyoneModule = (Module) unsafe.getReference(Module.class, everyoneModule$Address);
            Class<?> module$ReflectionData = Class.forName("java.lang.Module$ReflectionData");
            long reflectionData$exports$address = unsafe.objectFieldOffset(module$ReflectionData, "exports");
            // Retrieve ReflectionData::exports map
            Object exports = unsafe.getReference(module$ReflectionData, reflectionData$exports$address);
            ImagineBreaker.internal$openModules(javaBaseModule, everyoneModule, exports);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * For internal use
     */
    static void internal$removeAllReflectionFiltersUnsafely() {
        // jdk.internal.misc.Unsafe is needed here
        Unsafe unsafe = Unsafe.getUnsafe();
        // Grab address to Fields
        long fieldFilterMap$Address = unsafe.objectFieldOffset(Reflection.class, "fieldFilterMap");
        long methodFilterMap$Address = unsafe.objectFieldOffset(Reflection.class, "methodFilterMap");
        @SuppressWarnings("unchecked")
        Map<Class<?>, Object> fieldFilterMap = (Map<Class<?>, Object>) unsafe.getAndSetReference(Reflection.class, fieldFilterMap$Address, null);
        @SuppressWarnings("unchecked")
        Map<Class<?>, Object> methodFilterMap = (Map<Class<?>, Object>) unsafe.getAndSetReference(Reflection.class, methodFilterMap$Address, null);
        long reflectionData$Address = unsafe.objectFieldOffset(Class.class, "reflectionData");
        if (fieldFilterMap != null) {
            // Go through every class and clear all of their ReflectionData caches
            for (Class<?> clazz : fieldFilterMap.keySet()) {
                unsafe.getAndSetReference(clazz, reflectionData$Address, null);
            }
        }
        if (methodFilterMap != null) {
            // Go through every class and clear all of their ReflectionData caches
            for (Class<?> clazz : methodFilterMap.keySet()) {
                unsafe.getAndSetReference(clazz, reflectionData$Address, null);
            }
        }
    }

}
