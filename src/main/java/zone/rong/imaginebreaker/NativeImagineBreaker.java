package zone.rong.imaginebreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Before calling any native methods, you must load the library, there are numerous ways of doing so.
 * The native shared libraries are packed in the natives/ folder.
 */
public class NativeImagineBreaker {

    public static native void openBaseModules();

    public static native void removeAllReflectionFilters();

    private static native void addExportsToAll0(Module module, String exports);

    private static native Object weakPairMap_computeIfAbsent(Object exports, Module javaBaseModule, Module everyoneModule, Object computeIfAbsentFunction);

    private static void internal$openModules(Module javaBaseModule, Module everyoneModule, Object exports) {
        for (String packageString : javaBaseModule.getPackages()) {
            addExportsToAll0(javaBaseModule, packageString);
            @SuppressWarnings("unchecked")
            Map<String, Boolean> map = (Map<String, Boolean>) weakPairMap_computeIfAbsent(exports, javaBaseModule, everyoneModule,
                    ((BiFunction<Object, Object, Object>) (k, v) -> new ConcurrentHashMap<>()));
            map.put(packageString, true);
        }
    }

}
