package zone.rong.imaginebreaker.api;

import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * In-process jailbreak of the running JVM. No launcher flags required.
 */
public interface ImagineBreaker {

    /**
     * Maximally privileged {@code MethodHandles.Lookup} ({@code IMPL_LOOKUP}).
     *
     * @return the trusted lookup
     */
    Lookup trustedLookup();

    /**
     * {@code sun.misc.Unsafe} singleton.
     *
     * @return the Unsafe instance
     */
    Unsafe unsafe();

    /**
     * {@code jdk.internal.misc.Unsafe} singleton, or {@code null} if this JDK does not have it.
     *
     * @return the internal Unsafe instance, or {@code null}
     */
    Object internalUnsafe();

    /**
     * Runtime {@link Instrumentation}.
     * Installing a dummy agent if needed.
     * Fails if the VM refuses dynamic agent load (JEP 451).
     *
     * @return instrumentation
     */
    Instrumentation instrumentation();

    /**
     * URLs of the system class loader's URL path.
     *
     * @return classpath URLs
     */
    URL[] systemClassPath();

    /**
     * Opens every module in the boot layer to everyone.
     */
    void openBootModules();

    /**
     * Opens a module in the boot layer.
     *
     * @param bootModule module name
     */
    void openBootModule(String bootModule);

    /**
     * Opens every module in {@code layer}.
     *
     * @param layer module layer
     */
    void openModuleLayer(ModuleLayer layer);

    /**
     * Opens {@code module} to every module (Java-side and VM-side).
     *
     * @param module module to open
     */
    void openModule(Module module);

    /**
     * Opens {@code clazz}'s module to every module.
     *
     * @param clazz class whose module is opened
     */
    default void openModule(Class<?> clazz) {
        openModule(clazz.getModule());
    }

    /**
     * Rewrites {@code target}'s {@code Class.module} to that of {@code moduleClass}.
     * Useful when invoking {@code @CallerSensitive} methods.
     *
     * @param target      class to disguise
     * @param moduleClass class whose module is copied
     */
    void disguiseAsModule(Class<?> target, Class<?> moduleClass);

    /**
     * Rewrites {@code target}'s {@code Class.module}.
     *
     * @param target class to disguise
     * @param module module to assume
     */
    void disguiseAsModule(Class<?> target, Module module);

    /**
     * Disguises {@code target} for the duration of {@code action}, then restores it.
     *
     * @param target      class to disguise
     * @param moduleClass class whose module is copied
     * @param action      work to run while disguised
     */
    void disguiseAsModule(Class<?> target, Class<?> moduleClass, Runnable action);

    /**
     * Disguises {@code target} for the duration of {@code action}, then restores it.
     *
     * @param target class to disguise
     * @param module module to assume
     * @param action work to run while disguised
     */
    void disguiseAsModule(Class<?> target, Module module, Runnable action);

    /**
     * Clears {@code jdk.internal.reflect.Reflection#fieldFilterMap} and the
     * reflection cache of every previously filtered class. Call again if
     * something re-registers filters (copy-on-write).
     */
    void clearFieldFilters();

    /**
     * Clears {@code jdk.internal.reflect.Reflection#methodFilterMap} and the
     * reflection cache of every previously filtered class. Call again if
     * something re-registers filters (copy-on-write).
     */
    void clearMethodFilters();

    /**
     * Allows unnamed modules and {@code java.base} to mutate instance {@code final}
     * fields. No-op before JDK 26.
     *
     * <p>Required because JEP 500 gates {@code Field.set}/{@code unreflectSetter}
     * on {@code Module.enableFinalMutation}, including for {@code IMPL_LOOKUP}.</p>
     */
    void enableFinalFieldMutation();

    /**
     * Allows code in {@code module} to mutate instance {@code final} fields.
     * Unnamed modules share a single sentinel. No-op before JDK 26.
     *
     * @param module caller module to enable
     */
    void enableFinalFieldMutation(Module module);

    /**
     * Allows code in {@code clazz}'s module to mutate instance {@code final} fields.
     * No-op before JDK 26.
     *
     * @param clazz class whose module is enabled
     */
    default void enableFinalFieldMutation(Class<?> clazz) {
        enableFinalFieldMutation(clazz.getModule());
    }

    /**
     * Allows unnamed modules to use restricted JNI/FFM. No-op before JDK 22.
     */
    void enableNativeAccess();

    /**
     * Allows {@code module} to use restricted JNI/FFM.
     * Unnamed modules share a single sentinel. No-op before JDK 22.
     *
     * @param module caller module to enable
     */
    void enableNativeAccess(Module module);

    /**
     * Allows {@code clazz}'s module to use restricted JNI/FFM. No-op before JDK 22.
     *
     * @param clazz class whose module is enabled
     */
    default void enableNativeAccess(Class<?> clazz) {
        enableNativeAccess(clazz.getModule());
    }

    /**
     * All declared fields of {@code clazz}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @return declared fields
     */
    Field[] declaredFields(Class<?> clazz);

    /**
     * Declared field named {@code name}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @param name field name
     * @return the field, or {@code null} if absent
     */
    default Field declaredField(Class<?> clazz, String name) {
        Field[] fields = declaredFields(clazz);
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    /**
     * All declared methods of {@code clazz}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @return declared methods
     */
    Method[] declaredMethods(Class<?> clazz);

    /**
     * Declared method named {@code name} with {@code parameterTypes}, ignoring
     * reflection filters.
     *
     * @param clazz class to inspect
     * @param name method name
     * @param parameterTypes parameter types
     * @return the method, or {@code null} if absent
     */
    default Method declaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method[] methods = declaredMethods(clazz);
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        return null;
    }

    /**
     * All declared constructors of {@code clazz}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @param <T> class type
     * @return declared constructors
     */
    <T> Constructor<T>[] declaredConstructors(Class<T> clazz);

    /**
     * Declared constructor with {@code parameterTypes}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @param parameterTypes parameter types
     * @param <T> class type
     * @return the constructor, or {@code null} if absent
     */
    default <T> Constructor<T> declaredConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        Constructor<T>[] constructors = declaredConstructors(clazz);
        for (Constructor<T> constructor : constructors) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * All declared member classes of {@code clazz}, ignoring reflection filters.
     *
     * @param clazz class to inspect
     * @return declared member classes
     */
    Class<?>[] declaredClasses(Class<?> clazz);

    /**
     * Declared member class with {@code simpleName}, ignoring reflection filters.
     *
     * @param clazz      class to inspect
     * @param simpleName simple name
     * @return the class, or {@code null} if absent
     */
    default Class<?> declaredClass(Class<?> clazz, String simpleName) {
        Class<?>[] classes = declaredClasses(clazz);
        for (Class<?> member : classes) {
            if (member.getSimpleName().equals(simpleName)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Unsafe field offset, static or instance.
     *
     * @param field field
     * @return offset
     */
    long fieldOffset(Field field);

    /**
     * Field value. {@code instance} is ignored for static fields.
     * Goes through {@code Unsafe}; static {@code final} fields are readable and writable.
     *
     * @param instance instance, or {@code null} if static
     * @param field    field
     * @return boxed value
     */
    Object get(@Nullable Object instance, Field field);

    /**
     * Field value. {@code instance} is ignored for static fields.
     * Goes through {@code Unsafe}; static {@code final} fields are writable.
     *
     * @param instance instance, or {@code null} if static
     * @param field field
     * @param value boxed value
     */
    void set(@Nullable Object instance, Field field, Object value);

    /**
     * Copies {@code field} from {@code from} to {@code to}.
     *
     * @param from source instance, or {@code null} if static
     * @param to destination instance, or {@code null} if static
     * @param field field
     */
    default void copy(@Nullable Object from, @Nullable Object to, Field field) {
        set(to, field, get(from, field));
    }

    /**
     * Invokes {@code method} via the trusted lookup.
     * {@code instance} is ignored for static methods.
     *
     * @param instance instance, or {@code null} if static
     * @param method method
     * @param args arguments
     * @return return value, or {@code null} if {@code void}
     */
    Object invoke(@Nullable Object instance, Method method, Object... args);

    /**
     * Invokes {@code constructor} via the trusted lookup.
     *
     * @param constructor constructor
     * @param args arguments
     * @param <T> constructed type
     * @return new instance
     */
    <T> T newInstance(Constructor<T> constructor, Object... args);

    /**
     * Allocates an instance without running the constructor of the instance's class.
     *
     * @param type type to allocate
     * @param <T> type
     * @return uninitialized instance
     */
    <T> T allocate(Class<T> type);

    /**
     * Forces class initialization.
     *
     * @param clazz class to initialize
     */
    void ensureInitialized(Class<?> clazz);

    /**
     * Class of the caller {@code depth} frames above this method.
     * Depth {@code 0} is the immediate caller. Returns {@code null} if the depth exceeds the stack.
     *
     * @param depth frames to skip after this method
     * @return caller class, or {@code null}
     */
    Class<?> callerClass(int depth);

    /**
     * Defines a class in {@code loader}. A {@code null} loader is the bootstrap loader.
     *
     * @param loader target loader, or {@code null} for bootstrap
     * @param name binary name, or {@code null} to read it from {@code bytecode}
     * @param bytecode class file bytes
     * @return defined class
     */
    default Class<?> defineClass(@Nullable ClassLoader loader, @Nullable String name, byte[] bytecode) {
        return defineClass(loader, name, bytecode, 0, bytecode.length, null);
    }

    /**
     * Defines a class in {@code loader}. A {@code null} loader is the bootstrap loader.
     *
     * @param loader target loader, or {@code null} for bootstrap
     * @param name binary name, or {@code null} to read it from {@code bytecode}
     * @param bytecode class file bytes
     * @param offset start offset
     * @param length length
     * @param protectionDomain  protection domain, or {@code null}
     * @return defined class
     */
    Class<?> defineClass(@Nullable ClassLoader loader, @Nullable String name, byte[] bytecode, int offset, int length, @Nullable ProtectionDomain protectionDomain);

    /**
     * Defines a hidden (JDK 15+) or anonymous (JDK 9-14) class hosted by {@code host}.
     * {@code classOptions} are {@code MethodHandles.Lookup.ClassOption} names, ignored before JDK 15.
     * The anonymous-class fallback requires {@code bytecode} to declare a class in the same package as {@code host}.
     *
     * @param host host class
     * @param bytecode class file bytes
     * @param initialize whether to initialize (JDK 15+)
     * @param classOptions {@code NESTMATE}, {@code STRONG}
     * @return defined class
     */
    Class<?> defineHiddenClass(Class<?> host, byte[] bytecode, boolean initialize, String... classOptions);

    /**
     * Appends {@code url} to the system class loader's URL path.
     *
     * @param url URL to add
     */
    void addToSystemClassPath(URL url);

    /**
     * Constructs an enum constant without adding it to the type.
     * Extra constructor parameters follow the implicit {@code (String name, int ordinal)} prefix.
     *
     * @param type enum class
     * @param name constant name
     * @param ordinal ordinal
     * @param parameterTypes extra constructor parameter types
     * @param arguments extra constructor arguments
     * @param <T> enum type
     * @return new constant
     */
    <T extends Enum<T>> T newEnum(Class<T> type, String name, int ordinal, Class<?>[] parameterTypes, Object[] arguments);

    /**
     * Appends {@code value} to {@code type}'s {@code $VALUES} and drops the {@code Class} enum cache.
     * Static {@code final} {@code $VALUES} is written through Unsafe.
     *
     * @param type enum class
     * @param value constant to append
     * @param <T> enum type
     */
    <T extends Enum<T>> void addEnum(Class<T> type, T value);

    /**
     * Drops {@code Class} enum constant caches so {@link Class#getEnumConstants()}
     * and {@link Class#enumConstantDirectory} rebuild.
     *
     * @param type enum class
     */
    void clearEnumCache(Class<? extends Enum<?>> type);

    /**
     * Loads an agent jar via {@code InstrumentationImpl.loadAgent}.
     *
     * @param agentJar agent jar
     */
    void loadAgent(File agentJar);

}
