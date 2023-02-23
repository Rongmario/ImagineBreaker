package zone.rong.imaginebreaker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class ImagineBreaker {

    /**
     * Opens all modules linked under java.base Module.
     * This is the fastest way to open all modules. But by using jdk.internal.misc.Unsafe.
     * Make sure `--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void openAllBaseModulesUnsafely() {
        try {
            // Get java.base as a Module
            final Module javaBaseModule = ModuleLayer.boot().findModule("java.base").orElseThrow(RuntimeException::new);
            // jdk.internal.misc.Unsafe is needed here
            jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
            // Grab address to Field
            long everyoneModule$Address = unsafe.objectFieldOffset(Module.class, "EVERYONE_MODULE");
            // Retrieve Field's value, EVERYONE_MODULE as a Module
            Module everyoneModule = (Module) unsafe.getReference(Module.class, everyoneModule$Address);
            Class<?> module$ReflectionData = Class.forName("java.lang.Module$ReflectionData");
            long reflectionData$exports$address = unsafe.objectFieldOffset(module$ReflectionData, "exports");
            // Retrieve ReflectionData::exports map
            Object exports = unsafe.getReference(module$ReflectionData, reflectionData$exports$address);
            Method addExportsToAll0 = Module.class.getDeclaredMethod("addExportsToAll0", Module.class, String.class);
            addExportsToAll0.setAccessible(true);
            Method weakKeyMap$computeIfAbsent = Class.forName("java.lang.WeakPairMap").getMethod("computeIfAbsent", Object.class, Object.class, BiFunction.class);
            weakKeyMap$computeIfAbsent.setAccessible(true);
            for (String packageString : javaBaseModule.getPackages()) {
                // Add it natively
                addExportsToAll0.invoke(null, javaBaseModule, packageString);
                // Add it to ReflectionData
                @SuppressWarnings("unchecked")
                Map<String, Boolean> map = (Map<String, Boolean>) weakKeyMap$computeIfAbsent.invoke(exports, javaBaseModule, everyoneModule,
                        ((BiFunction<Object, Object, Object>) (k, v) -> new ConcurrentHashMap<>()));
                map.put(packageString, true);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Opens all modules linked under java.base Module.
     * This is the slower way to {@link ImagineBreaker#openAllBaseModulesUnsafely()} but uses standard Reflection protocols
     * Make sure `--add-opens=java.base/jdk.lang=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void openAllBaseModulesReflectively() {
        try {
            // Get java.base as a Module
            final Module javaBaseModule = ModuleLayer.boot().findModule("java.base").orElseThrow(RuntimeException::new);
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
            if (everyoneModule != null) {
                Method addExportsToAll0 = Module.class.getDeclaredMethod("addExportsToAll0", Module.class, String.class);
                addExportsToAll0.setAccessible(true);
                Field reflectionData$exports = Class.forName("java.lang.Module$ReflectionData").getDeclaredField("exports");
                reflectionData$exports.setAccessible(true);
                Object exports = reflectionData$exports.get(null);
                Method weakKeyMap$computeIfAbsent = Class.forName("java.lang.WeakPairMap").getMethod("computeIfAbsent", Object.class, Object.class, BiFunction.class);
                weakKeyMap$computeIfAbsent.setAccessible(true);
                for (String packageString : javaBaseModule.getPackages()) {
                    // Add it natively
                    addExportsToAll0.invoke(null, javaBaseModule, packageString);
                    // Add it to ReflectionData
                    @SuppressWarnings("unchecked")
                    Map<String, Boolean> map = (Map<String, Boolean>) weakKeyMap$computeIfAbsent.invoke(exports, javaBaseModule, everyoneModule,
                            ((BiFunction<Object, Object, Object>) (k, v) -> new ConcurrentHashMap<>()));
                    map.put(packageString, true);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private ImagineBreaker() { }

}
