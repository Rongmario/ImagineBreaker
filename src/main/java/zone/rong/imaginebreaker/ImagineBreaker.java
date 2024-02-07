package zone.rong.imaginebreaker;

import java.util.Map;

public final class ImagineBreaker {

    static {
        NativeLoader.load();
    }

    public static void openBootModules() {
        openModuleLayer(ModuleLayer.boot());
    }

    public static void openModuleFor(Class<?> clazz) {
        openModule(clazz.getModule());
    }

    public static void openModuleLayer(ModuleLayer moduleLayer) {
        moduleLayer.modules().forEach(ImagineBreaker::openModule);
    }

    public static void openModule(Module module) {
        for (var pkg : module.getPackages()) {
            implAddOpensToModule(module, pkg);
        }
    }

    public static void removeFieldReflectionFilters() {
        var map = implRemoveFieldReflectionFilters();
        for (var clazz : map.keySet()) {
            implRemoveClassReflectionCacheData(clazz);
        }
    }

    public static void removeMethodReflectionFilters() {
        var map = implRemoveMethodReflectionFilters();
        for (var clazz : map.keySet()) {
            implRemoveClassReflectionCacheData(clazz);
        }
    }

    private static native void implAddOpensToModule(Module module, String pkg);

    private static native Map<Class<?>, String> implRemoveFieldReflectionFilters();

    private static native Map<Class<?>, String> implRemoveMethodReflectionFilters();

    private static native void implRemoveClassReflectionCacheData(Class<?> clazz);

    private ImagineBreaker() { }

}
