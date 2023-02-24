package zone.rong.imaginebreaker;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class ImagineBreaker {

    public static Optional<Module> findBootModule(String moduleName) {
        return ModuleLayer.boot().findModule(moduleName);
    }

    /**
     * Opens all modules linked under java.base Module.
     * This is the fastest way to open all modules. But by using jdk.internal.misc.Unsafe.
     * Make sure `--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void openAllBaseModulesUnsafely() {
        UnsafeContainer.internal$openAllBaseModulesUnsafely();
    }

    /**
     * Opens all modules linked under java.base Module.
     * This is the slower way to {@link ImagineBreaker#openAllBaseModulesUnsafely()} but uses standard Reflection protocols
     * Make sure `--add-opens=java.base/jdk.lang=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void openAllBaseModulesReflectively() {
        ReflectionContainer.internal$openAllBaseModulesReflectively();
    }

    /**
     * Removes all jdk.internal.reflect.Reflection field and method filters
     * This is the fastest way to remove all reflection filters. Allowing full Reflection access.
     * Make sure `--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED, --add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void removeAllReflectionFiltersUnsafely() {
        UnsafeContainer.internal$removeAllReflectionFiltersUnsafely();
    }

    /**
     * Removes all jdk.internal.reflect.Reflection field and method filters
     * This is the slower way to remove all reflection filters. Allowing full Reflection access.
     * Make sure `--add-opens=java.base/jdk.lang=ALL-UNNAMED, --add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED` is added to jvm and compiler args.
     */
    public static void removeAllReflectionFiltersReflectively() {
        ReflectionContainer.internal$removeAllReflectionFiltersReflectively();
    }

    static void internal$openModules(Module javaBaseModule, Module everyoneModule, Object exports) throws ReflectiveOperationException {
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
    }

    private ImagineBreaker() { }

}
