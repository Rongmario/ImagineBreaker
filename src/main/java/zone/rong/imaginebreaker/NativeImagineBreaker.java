package zone.rong.imaginebreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

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
