package zone.rong.imaginebreaker;

import jdk.internal.reflect.Reflection;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * @since 2.0
 * No natives required, no starting arguments required.
 * This is achieved thanks to jdk.internal.reflect.Reflection class not banning methods
 * from being reflectively invoked. With this, we can gain a lot of ground.
 * Unfortunately, it is likely that this will be patched very soon.
 * Extensive testing is provided for nearly all major jdk versions and vendors.
 */
public final class ImagineBreaker {

    private static final boolean IS_OPEN_J9 = isOpenJ9();
    private static final Unsafe UNSAFE = retrieveUnsafe();
    private static final MethodHandles.Lookup LOOKUP = retrieveLookup();
    private static final Set<Module> EVERYONE_MODULE_SINGLETON = retrieveEveryoneModule();
    private static final VarHandle MODULE$OPEN_PACKAGES = retrieveOpenPackagesHandle();
    private static final VarHandle CLASS$MODULE = retrieveModuleHandle();
    private static final VarHandle REFLECTION$FIELD_FILTER_MAP = retrieveFieldFilterMap();
    private static final VarHandle REFLECTION$METHOD_FILTER_MAP = retrieveMethodFilterMap();
    private static final VarHandle CLASS$REFLECTION_DATA = retrieveReflectionData();

    /**
     * Accessor to sun.misc.Unsafe.
     *
     * @return instance of sun.misc.Unsafe.
     */
    public static Unsafe unsafe() {
        return UNSAFE;
    }

    /**
     * Accessor to the trusted MethodHandles$Lookup.
     *
     * @return instance of the trusted lookup.
     */
    public static MethodHandles.Lookup lookup() {
        return LOOKUP;
    }

    /**
     * Opens all modules within the boot ModuleLayer.
     */
    public static void openBootModules() {
        openModuleLayer(ModuleLayer.boot());
    }

    /**
     * Opens all modules within the specified ModuleLayer
     *
     * @param layer module layer to have all of its modules opened
     */
    public static void openModuleLayer(ModuleLayer layer) {
        layer.modules().forEach(ImagineBreaker::openModule);
    }

    /**
     * Opens a specific module
     *
     * @param module module to be opened
     */
    public static void openModule(Module module) {
        MODULE$OPEN_PACKAGES.set(module, WorldRejector.INSTANCE);
    }

    /**
     * Disguises a class as having a module of a different class.
     * Extremely useful when invoking caller-sensitive methods.
     *
     * <p>Consider {@link ImagineBreaker#disguiseAsModule(Class, Class, Runnable)}
     *    if you want to revert the disguise. Use the runnable to run actions before the reversion</p>
     *
     * @param target      target class to have its module changed
     * @param moduleClass class to have its module queried
     */
    public static void disguiseAsModule(Class<?> target, Class<?> moduleClass) {
        var module = CLASS$MODULE.get(moduleClass);
        CLASS$MODULE.set(target, module);
    }

    /**
     * Disguises a class as having a different module.
     * Extremely useful when invoking caller-sensitive methods.
     *
     * <p>Consider {@link ImagineBreaker#disguiseAsModule(Class, Module, Runnable)}
     *    if you want to revert the disguise. Use the runnable to run actions before the reversion</p>
     *
     * @param target target class to have its module changed
     * @param module module for the target class
     */
    public static void disguiseAsModule(Class<?> target, Module module) {
        CLASS$MODULE.set(target, module);
    }

    /**
     * Disguises a class as having a different module.
     * Extremely useful when invoking caller-sensitive methods.
     * This method allows reversion of module for the target class after the runnable is ran.
     *
     * @param target      target class to have its module changed
     * @param moduleClass module for the target class
     * @param runnable    runnable to run before the class is reverted to having its module changed
     */
    public static void disguiseAsModule(Class<?> target, Class<?> moduleClass, Runnable runnable) {
        var old = CLASS$MODULE.get(target);
        disguiseAsModule(target, moduleClass);
        runnable.run();
        CLASS$MODULE.set(target, old);
    }

    /**
     * Disguises a class as having a different module.
     * Extremely useful when invoking caller-sensitive methods.
     * This method allows reversion of module for the target class after the runnable is ran.
     *
     * @param target   target class to have its module changed
     * @param module   module for the target class
     * @param runnable runnable to run before the class is reverted to having its module changed
     */
    public static void disguiseAsModule(Class<?> target, Module module, Runnable runnable) {
        var old = CLASS$MODULE.get(target);
        disguiseAsModule(target, module);
        runnable.run();
        CLASS$MODULE.set(target, old);
    }

    /**
     * Wipes {@link jdk.internal.reflect.Reflection#fieldFilterMap} as well as the
     * {@link java.lang.Class#reflectionData} member in their respective classes, though this is not done for OpenJ9.
     * This method should be called first to eliminate the default filters
     * but since the method to register field filters is copy-on-write
     * new filters may be added after-the-fact, meaning this method needs to be called again.
     */
    public static void wipeFieldFilters() {
        if (!IS_OPEN_J9) {
            for (var clazz : ((Map<Class, Set>) REFLECTION$FIELD_FILTER_MAP.get()).keySet()) {
                CLASS$REFLECTION_DATA.setVolatile(clazz, null);
            }
        }
        REFLECTION$FIELD_FILTER_MAP.setVolatile((Object) null);
    }

    /**
     * Wipes {@link jdk.internal.reflect.Reflection#methodFilterMap} as well as the
     * {@link java.lang.Class#reflectionData} member in their respective classes, though this is not done for OpenJ9.
     * This method should be called first to eliminate the default filters
     * but since the method to register method filters is copy-on-write
     * new filters may be added after-the-fact, meaning this method needs to be called again.
     */
    public static void wipeMethodFilters() {
        if (!IS_OPEN_J9) {
            for (var clazz : ((Map<Class, Set>) REFLECTION$METHOD_FILTER_MAP.get()).keySet()) {
                CLASS$REFLECTION_DATA.setVolatile(clazz, null);
            }
        }
        REFLECTION$METHOD_FILTER_MAP.setVolatile((Object) null);
    }

    private static boolean isOpenJ9() {
        return "Eclipse OpenJ9".equals(System.getProperty("java.vm.vendor"));
    }

    private static Unsafe retrieveUnsafe() {
        try {
            var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandles.Lookup retrieveLookup() {
        var methodHandles$lookup$implLookup = retrieveImplLookup();
        long offset = UNSAFE.staticFieldOffset(methodHandles$lookup$implLookup);
        return (MethodHandles.Lookup) UNSAFE.getObject(MethodHandles.Lookup.class, offset);
    }

    private static Field retrieveImplLookup() {
        try {
            return MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<Module> retrieveEveryoneModule() {
        try {
            var everyoneModule$Handle = LOOKUP.findStaticVarHandle(Module.class, "EVERYONE_MODULE", Module.class);
            return Set.of((Module) everyoneModule$Handle.get());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle retrieveOpenPackagesHandle() {
        try {
            return LOOKUP.findVarHandle(Module.class, "openPackages", Map.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle retrieveModuleHandle() {
        try {
            return LOOKUP.findVarHandle(Class.class, "module", Module.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle retrieveFieldFilterMap() {
        try {
            return LOOKUP.findStaticVarHandle(Reflection.class, "fieldFilterMap", Map.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle retrieveMethodFilterMap() {
        try {
            return LOOKUP.findStaticVarHandle(Reflection.class, "methodFilterMap", Map.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle retrieveReflectionData() {
        try {
            return LOOKUP.findVarHandle(Class.class, "reflectionData", SoftReference.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (!IS_OPEN_J9) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    private ImagineBreaker() { }

    private static class WorldRejector extends AbstractMap<String, Set<Module>> {

        private static final WorldRejector INSTANCE = new WorldRejector();

        @Override
        public Set<Module> get(Object key) {
            return EVERYONE_MODULE_SINGLETON;
        }

        @Override
        public Set<Entry<String, Set<Module>>> entrySet() {
            return Set.of();
        }

    }

}
