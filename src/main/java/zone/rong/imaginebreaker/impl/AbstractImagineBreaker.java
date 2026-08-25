package zone.rong.imaginebreaker.impl;

import sun.misc.Unsafe;
import zone.rong.imaginebreaker.Index;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Shared {@link ImagineBreaker} implementation. Subclasses only provide {@link #obtainTrustedLookup()}.
 */
public abstract class AbstractImagineBreaker implements ImagineBreaker {

    private static final int ACC_SYNTHETIC = 0x1000;
    private static final int JDK = Runtime.version().major();
    private static final MethodType VOID = MethodType.methodType(void.class);
    private static final MethodType VOID_MODULE_STRING = MethodType.methodType(void.class, Module.class, String.class);
    private static final MethodType SPREAD = MethodType.methodType(Object.class, Object[].class);
    private static final StackWalker CALLER_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Maximally privileged lookup ({@code IMPL_LOOKUP}).
     */
    protected abstract Lookup obtainTrustedLookup();

    @Override
    public Lookup trustedLookup() {
        return Holder.LOOKUP;
    }

    @Override
    public Unsafe unsafe() {
        return Holder.UNSAFE;
    }

    @Override
    public Object internalUnsafe() {
        return Holder.INTERNAL_UNSAFE;
    }

    @Override
    public void openBootModules() {
        openModuleLayer(ModuleLayer.boot());
    }

    @Override
    public void openBootModule(String bootModule) {
        openModule(ModuleLayer.boot().findModule(bootModule).orElseThrow(() -> new IllegalArgumentException("No boot module named " + bootModule)));
    }

    @Override
    public void openModuleLayer(ModuleLayer layer) {
        for (Module module : layer.modules()) {
            openModule(module);
        }
    }

    @Override
    public void openModule(Module module) {
        Holder.module$openPackages.set(module, WorldRejector.INSTANCE);
        for (String pkg : module.getPackages()) {
            try {
                Holder.module$addExportsToAll0.invokeExact(module, pkg);
            } catch (Throwable t) {
                throw new RuntimeException("Unable to open " + module.getName() + "/" + pkg, t);
            }
        }
    }

    @Override
    public void disguiseAsModule(Class<?> target, Class<?> moduleClass) {
        disguiseAsModule(target, moduleClass.getModule());
    }

    @Override
    public void disguiseAsModule(Class<?> target, Module module) {
        Holder.class$module.set(target, module);
    }

    @Override
    public void disguiseAsModule(Class<?> target, Class<?> moduleClass, Runnable action) {
        disguiseAsModule(target, moduleClass.getModule(), action);
    }

    @Override
    public void disguiseAsModule(Class<?> target, Module module, Runnable action) {
        Module previous = (Module) Holder.class$module.get(target);
        disguiseAsModule(target, module);
        try {
            action.run();
        } finally {
            Holder.class$module.set(target, previous);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void clearFieldFilters() {
        Map fieldFilterMap = (Map) Holder.reflection$fieldFilterMap.get();
        Holder.reflection$fieldFilterMap.set(new HashMap());
        if (fieldFilterMap != null) {
            for (Object key : fieldFilterMap.keySet()) {
                Holder.clearReflectionCache((Class<?>) key);
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void clearMethodFilters() {
        Map methodFilterMap = (Map) Holder.reflection$methodFilterMap.get();
        Holder.reflection$methodFilterMap.set(new HashMap());
        if (methodFilterMap != null) {
            for (Object key : methodFilterMap.keySet()) {
                Holder.clearReflectionCache((Class<?>) key);
            }
        }
    }

    @Override
    public void enableFinalFieldMutation() {
        Modules.enableFinalFieldMutationFloor();
    }

    @Override
    public void enableFinalFieldMutation(Module module) {
        Modules.enableFinalFieldMutation(module);
    }

    @Override
    public void enableNativeAccess() {
        Modules.enableNativeAccessFloor();
    }

    @Override
    public void enableNativeAccess(Module module) {
        Modules.enableNativeAccess(module);
    }

    @Override
    public Field[] declaredFields(Class<?> clazz) {
        return Members.declaredFields(clazz);
    }

    @Override
    public Method[] declaredMethods(Class<?> clazz) {
        return Members.declaredMethods(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Constructor<T>[] declaredConstructors(Class<T> clazz) {
        return (Constructor<T>[]) Members.declaredConstructors(clazz);
    }

    @Override
    public Class<?>[] declaredClasses(Class<?> clazz) {
        return Members.declaredClasses(clazz);
    }

    @Override
    public long fieldOffset(Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            return Holder.staticOffset(field);
        }
        return Holder.objectOffset(field);
    }

    @Override
    public Object get(Object instance, Field field) {
        return Holder.get(fieldBase(instance, field), fieldOffset(field), field.getType());
    }

    @Override
    public boolean getBoolean(Object instance, Field field) {
        return Holder.getBoolean(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public byte getByte(Object instance, Field field) {
        return Holder.getByte(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public short getShort(Object instance, Field field) {
        return Holder.getShort(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public char getChar(Object instance, Field field) {
        return Holder.getChar(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public int getInt(Object instance, Field field) {
        return Holder.getInt(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public long getLong(Object instance, Field field) {
        return Holder.getLong(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public float getFloat(Object instance, Field field) {
        return Holder.getFloat(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public double getDouble(Object instance, Field field) {
        return Holder.getDouble(fieldBase(instance, field), fieldOffset(field));
    }

    @Override
    public void set(Object instance, Field field, Object value) {
        Holder.set(fieldBase(instance, field), fieldOffset(field), field.getType(), value);
    }

    @Override
    public void setBoolean(Object instance, Field field, boolean value) {
        Holder.setBoolean(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setByte(Object instance, Field field, byte value) {
        Holder.setByte(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setShort(Object instance, Field field, short value) {
        Holder.setShort(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setChar(Object instance, Field field, char value) {
        Holder.setChar(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setInt(Object instance, Field field, int value) {
        Holder.setInt(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setLong(Object instance, Field field, long value) {
        Holder.setLong(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setFloat(Object instance, Field field, float value) {
        Holder.setFloat(fieldBase(instance, field), fieldOffset(field), value);
    }

    @Override
    public void setDouble(Object instance, Field field, double value) {
        Holder.setDouble(fieldBase(instance, field), fieldOffset(field), value);
    }

    private Object fieldBase(Object instance, Field field) {
        return Modifier.isStatic(field.getModifiers()) ? Holder.staticBase(field) : instance;
    }

    @Override
    public Object invoke(Object instance, Method method, Object... args) {
        try {
            MethodHandle handle = trustedLookup().unreflect(method).asFixedArity();
            if (!Modifier.isStatic(method.getModifiers())) {
                handle = handle.bindTo(instance);
            }
            return spread(handle, args);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return (T) spread(trustedLookup().unreflectConstructor(constructor).asFixedArity(), args);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T allocate(Class<T> type) {
        try {
            return (T) unsafe().allocateInstance(type);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void ensureInitialized(Class<?> clazz) {
        if (Define.lookup$ensureInitialized != null) {
            try {
                Lookup lookup = trustedLookup().in(clazz);
                Class<?> ignored = (Class<?>) Define.lookup$ensureInitialized.invokeExact(lookup, clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
            return;
        }
        if (Define.iu$ensureClassInitialized != null) {
            try {
                Define.iu$ensureClassInitialized.invokeExact(clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
            return;
        }
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> callerClass(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("depth < 0");
        }
        if (depth == 0) {
            try {
                return CALLER_WALKER.getCallerClass();
            } catch (IllegalCallerException e) {
                return null;
            }
        }
        return CALLER_WALKER.walk(frames -> frames.skip(depth + 1)
                .findFirst()
                .map(StackWalker.StackFrame::getDeclaringClass)
                .orElse(null));
    }

    @Override
    public Class<?> defineClass(ClassLoader loader, String name, byte[] bytecode, int offset, int length, ProtectionDomain protectionDomain) {
        if (loader != null) {
            try {
                return (Class<?>) Define.classLoader$defineClass.invokeExact(loader, name, bytecode, offset, length, protectionDomain);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }
        if (Define.iu$defineClass == null) {
            throw new IllegalStateException("No defineClass path for the bootstrap loader");
        }
        try {
            return (Class<?>) Define.iu$defineClass.invokeExact(name, bytecode, offset, length, loader, protectionDomain);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @Override
    public Class<?> defineHiddenClass(Class<?> host, byte[] bytecode, boolean initialize, String... classOptions) {
        if (Define.lookup$defineHiddenClass != null && Define.classOptionClass != null) {
            Object options = classOptions(classOptions);
            try {
                Lookup defined = (Lookup) Define.lookup$defineHiddenClass.invokeExact(
                        trustedLookup().in(host), bytecode, initialize, options);
                return defined.lookupClass();
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }
        if (Define.unsafe$defineAnonymousClass != null) {
            Object[] patches = new Object[0];
            try {
                return (Class<?>) Define.unsafe$defineAnonymousClass.invokeExact(unsafe(), host, bytecode, patches);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }
        throw new UnsupportedOperationException("Hidden / anonymous class definition is not available");
    }

    @Override
    public void addToSystemClassPath(URL url) {
        try {
            ClassPath.ADD_URL.invokeExact(ClassPath.UCP, url);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @Override
    public URL[] systemClassPath() {
        try {
            return (URL[]) ClassPath.GET_URLS.invokeExact(ClassPath.UCP);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @Override
    public <T extends Enum<T>> T newEnum(Class<T> type, String name, int ordinal, Class<?>[] parameterTypes, Object[] arguments) {
        Class<?>[] extra = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        Object[] extraArgs = arguments == null ? new Object[0] : arguments;
        Class<?>[] fullTypes = new Class<?>[extra.length + 2];
        fullTypes[0] = String.class;
        fullTypes[1] = int.class;
        System.arraycopy(extra, 0, fullTypes, 2, extra.length);
        Object[] fullArgs = new Object[extraArgs.length + 2];
        fullArgs[0] = name;
        fullArgs[1] = ordinal;
        System.arraycopy(extraArgs, 0, fullArgs, 2, extraArgs.length);
        Constructor<T> constructor = declaredConstructor(type, fullTypes);
        if (constructor == null) {
            throw new IllegalArgumentException("No enum constructor " + type.getName() + Arrays.toString(fullTypes));
        }
        return newInstance(constructor, fullArgs);
    }

    @Override
    public <T extends Enum<T>> void addEnum(Class<T> type, T value) {
        Field values = null;
        Field potential = null;
        Field[] fields = declaredFields(type);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers()) || !field.getType().isArray()) {
                continue;
            }
            if (!field.getType().getComponentType().equals(type)) {
                continue;
            }
            if ((field.getModifiers() & ACC_SYNTHETIC) != 0) {
                values = field;
                break;
            }
            if (field.getName().startsWith("$VALUES")) {
                potential = field;
            }
        }
        if (values == null) {
            values = potential;
        }
        if (values == null) {
            throw new IllegalStateException("No $VALUES array on " + type.getName());
        }
        Object[] current = (Object[]) get(null, values);
        Object[] expanded = Arrays.copyOf(current, current.length + 1);
        expanded[expanded.length - 1] = value;
        set(null, values, expanded);
        clearEnumCache(type);
    }

    @Override
    public void clearEnumCache(Class<? extends Enum<?>> type) {
        Members.clearEnumCache(type);
    }

    @Override
    public Instrumentation instrumentation() {
        if (AgentProbe.instrumentation != null) {
            return AgentProbe.instrumentation;
        }
        File jar;
        try {
            jar = Agent.dummyJar();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write dummy agent jar", e);
        }
        loadAgent(jar);
        if (AgentProbe.instrumentation == null) {
            throw new IllegalStateException("Instrumentation was not installed");
        }
        return AgentProbe.instrumentation;
    }

    @Override
    public void loadAgent(File agentJar) {
        if (Agent.LOAD_AGENT == null) {
            throw new IllegalStateException("InstrumentationImpl.loadAgent is not available");
        }
        String path = agentJar.getAbsolutePath();
        try {
            Agent.LOAD_AGENT.invokeExact(path);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object classOptions(String[] names) {
        Object options = Array.newInstance(Define.classOptionClass, names.length);
        Class<? extends Enum> optClass = (Class<? extends Enum>) Define.classOptionClass;
        for (int i = 0; i < names.length; i++) {
            Array.set(options, i, Enum.valueOf(optClass, names[i].toUpperCase(Locale.ROOT)));
        }
        return options;
    }

    private static Object spread(MethodHandle handle, Object[] args) throws Throwable {
        int arity = handle.type().parameterCount();
        return handle.asSpreader(Object[].class, arity).asType(SPREAD).invokeExact(args);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            Method method = Index.get().declaredMethod(current, name, parameterTypes);
            if (method != null) {
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Object fallbackUcp(ClassLoader loader) {
        Class<?> type = loader.getClass();
        while (type != null && type != Object.class) {
            Field ucp = Index.get().declaredField(type, "ucp");
            if (ucp != null) {
                return Index.get().get(loader, ucp);
            }
            type = type.getSuperclass();
        }
        throw new IllegalStateException("URLClassPath field not found on " + loader.getClass().getName());
    }

    private static void invokeVoid(MethodHandle handle) {
        try {
            handle.invokeExact();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    private static void invokeVoid(MethodHandle handle, Module module) {
        try {
            handle.invokeExact(module);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    private static RuntimeException rethrow(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        return new RuntimeException(t);
    }

    public static final class AgentProbe {

        public static volatile Instrumentation instrumentation;

        public static void agentmain(String agentArgs, Instrumentation inst) {
            instrumentation = inst;
        }

        public static void premain(String agentArgs, Instrumentation inst) {
            instrumentation = inst;
        }

    }

    /**
     * Lookup, both Unsafes, module-open / filter handles, and internal-Unsafe memory ops.
     */
    private static final class Holder {

        static final Lookup LOOKUP;
        static final Unsafe UNSAFE;
        static final Object INTERNAL_UNSAFE;

        static final VarHandle class$module, class$reflectionData, reflection$fieldFilterMap, reflection$methodFilterMap;
        static final VarHandle module$openPackages;
        static final MethodHandle module$addExportsToAll0, semeru$class$setReflectCache;

        static final MethodHandle iu$objectFieldOffset, iu$staticFieldOffset, iu$staticFieldBase;
        static final MethodHandle iu$getBoolean, iu$getByte, iu$getShort, iu$getChar, iu$getInt, iu$getLong, iu$getFloat, iu$getDouble, iu$getReference;
        static final MethodHandle iu$putBoolean, iu$putByte, iu$putShort, iu$putChar, iu$putInt, iu$putLong, iu$putFloat, iu$putDouble, iu$putReference;

        static {
            try {
                LOOKUP = ((AbstractImagineBreaker) Index.get()).obtainTrustedLookup();
                Lookup lookup = LOOKUP;

                UNSAFE = (Unsafe) lookup.findStaticGetter(Unsafe.class, "theUnsafe", Unsafe.class).invokeExact();

                Class<?> iuClass = Class.forName("jdk.internal.misc.Unsafe");
                Object iu = lookup.findStaticGetter(iuClass, "theUnsafe", iuClass).invoke();
                INTERNAL_UNSAFE = iu;

                String getRef = JDK >= 12 ? "getReference" : "getObject";
                String putRef = JDK >= 12 ? "putReference" : "putObject";
                iu$objectFieldOffset = bindIu(lookup, iuClass, iu, "objectFieldOffset", MethodType.methodType(long.class, Field.class));
                iu$staticFieldOffset = bindIu(lookup, iuClass, iu, "staticFieldOffset", MethodType.methodType(long.class, Field.class));
                iu$staticFieldBase = bindIu(lookup, iuClass, iu, "staticFieldBase", MethodType.methodType(Object.class, Field.class));
                iu$getBoolean = bindIu(lookup, iuClass, iu, "getBoolean", MethodType.methodType(boolean.class, Object.class, long.class));
                iu$getByte = bindIu(lookup, iuClass, iu, "getByte", MethodType.methodType(byte.class, Object.class, long.class));
                iu$getShort = bindIu(lookup, iuClass, iu, "getShort", MethodType.methodType(short.class, Object.class, long.class));
                iu$getChar = bindIu(lookup, iuClass, iu, "getChar", MethodType.methodType(char.class, Object.class, long.class));
                iu$getInt = bindIu(lookup, iuClass, iu, "getInt", MethodType.methodType(int.class, Object.class, long.class));
                iu$getLong = bindIu(lookup, iuClass, iu, "getLong", MethodType.methodType(long.class, Object.class, long.class));
                iu$getFloat = bindIu(lookup, iuClass, iu, "getFloat", MethodType.methodType(float.class, Object.class, long.class));
                iu$getDouble = bindIu(lookup, iuClass, iu, "getDouble", MethodType.methodType(double.class, Object.class, long.class));
                iu$getReference = bindIu(lookup, iuClass, iu, getRef, MethodType.methodType(Object.class, Object.class, long.class));
                iu$putBoolean = bindIu(lookup, iuClass, iu, "putBoolean", MethodType.methodType(void.class, Object.class, long.class, boolean.class));
                iu$putByte = bindIu(lookup, iuClass, iu, "putByte", MethodType.methodType(void.class, Object.class, long.class, byte.class));
                iu$putShort = bindIu(lookup, iuClass, iu, "putShort", MethodType.methodType(void.class, Object.class, long.class, short.class));
                iu$putChar = bindIu(lookup, iuClass, iu, "putChar", MethodType.methodType(void.class, Object.class, long.class, char.class));
                iu$putInt = bindIu(lookup, iuClass, iu, "putInt", MethodType.methodType(void.class, Object.class, long.class, int.class));
                iu$putLong = bindIu(lookup, iuClass, iu, "putLong", MethodType.methodType(void.class, Object.class, long.class, long.class));
                iu$putFloat = bindIu(lookup, iuClass, iu, "putFloat", MethodType.methodType(void.class, Object.class, long.class, float.class));
                iu$putDouble = bindIu(lookup, iuClass, iu, "putDouble", MethodType.methodType(void.class, Object.class, long.class, double.class));
                iu$putReference = bindIu(lookup, iuClass, iu, putRef, MethodType.methodType(void.class, Object.class, long.class, Object.class));

                class$module = lookup.findVarHandle(Class.class, "module", Module.class);
                class$reflectionData = Index.isSemeru() ? null : lookup.findVarHandle(Class.class, "reflectionData", SoftReference.class);

                Class<?> reflectionClass = Class.forName("jdk.internal.reflect.Reflection");
                Lookup reflectionLookup = lookup.in(reflectionClass);
                reflection$fieldFilterMap = reflectionLookup.findStaticVarHandle(reflectionClass, "fieldFilterMap", Map.class);
                reflection$methodFilterMap = reflectionLookup.findStaticVarHandle(reflectionClass, "methodFilterMap", Map.class);

                Lookup moduleLookup = lookup.in(Module.class);
                module$openPackages = moduleLookup.findVarHandle(Module.class, "openPackages", Map.class);
                module$addExportsToAll0 = moduleLookup.findStatic(Module.class, "addExportsToAll0", VOID_MODULE_STRING);

                semeru$class$setReflectCache = Index.isSemeru()
                        ? lookup.findSetter(Class.class, "reflectCache", Class.forName("java.lang.Class$ReflectCache"))
                                .asType(MethodType.methodType(void.class, Class.class, Object.class))
                        : null;

                WorldRejector.init(everyoneSet(moduleLookup));
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to construct handles", t);
            }
        }

        static long objectOffset(Field field) {
            try {
                return (long) iu$objectFieldOffset.invokeExact(field);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static long staticOffset(Field field) {
            try {
                return (long) iu$staticFieldOffset.invokeExact(field);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static Object staticBase(Field field) {
            try {
                return iu$staticFieldBase.invokeExact(field);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static Object get(Object base, long offset, Class<?> type) {
            if (type == boolean.class) {
                return getBoolean(base, offset);
            }
            if (type == byte.class) {
                return getByte(base, offset);
            }
            if (type == short.class) {
                return getShort(base, offset);
            }
            if (type == char.class) {
                return getChar(base, offset);
            }
            if (type == int.class) {
                return getInt(base, offset);
            }
            if (type == long.class) {
                return getLong(base, offset);
            }
            if (type == float.class) {
                return getFloat(base, offset);
            }
            if (type == double.class) {
                return getDouble(base, offset);
            }
            try {
                return iu$getReference.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static boolean getBoolean(Object base, long offset) {
            try {
                return (boolean) iu$getBoolean.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static byte getByte(Object base, long offset) {
            try {
                return (byte) iu$getByte.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static short getShort(Object base, long offset) {
            try {
                return (short) iu$getShort.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static char getChar(Object base, long offset) {
            try {
                return (char) iu$getChar.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static int getInt(Object base, long offset) {
            try {
                return (int) iu$getInt.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static long getLong(Object base, long offset) {
            try {
                return (long) iu$getLong.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static float getFloat(Object base, long offset) {
            try {
                return (float) iu$getFloat.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static double getDouble(Object base, long offset) {
            try {
                return (double) iu$getDouble.invokeExact(base, offset);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void set(Object base, long offset, Class<?> type, Object value) {
            if (type == boolean.class) {
                setBoolean(base, offset, ((Boolean) value).booleanValue());
            } else if (type == byte.class) {
                setByte(base, offset, ((Byte) value).byteValue());
            } else if (type == short.class) {
                setShort(base, offset, ((Short) value).shortValue());
            } else if (type == char.class) {
                setChar(base, offset, ((Character) value).charValue());
            } else if (type == int.class) {
                setInt(base, offset, ((Integer) value).intValue());
            } else if (type == long.class) {
                setLong(base, offset, ((Long) value).longValue());
            } else if (type == float.class) {
                setFloat(base, offset, ((Float) value).floatValue());
            } else if (type == double.class) {
                setDouble(base, offset, ((Double) value).doubleValue());
            } else {
                try {
                    iu$putReference.invokeExact(base, offset, value);
                } catch (Throwable t) {
                    throw rethrow(t);
                }
            }
        }

        static void setBoolean(Object base, long offset, boolean value) {
            try {
                iu$putBoolean.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setByte(Object base, long offset, byte value) {
            try {
                iu$putByte.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setShort(Object base, long offset, short value) {
            try {
                iu$putShort.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setChar(Object base, long offset, char value) {
            try {
                iu$putChar.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setInt(Object base, long offset, int value) {
            try {
                iu$putInt.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setLong(Object base, long offset, long value) {
            try {
                iu$putLong.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setFloat(Object base, long offset, float value) {
            try {
                iu$putFloat.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void setDouble(Object base, long offset, double value) {
            try {
                iu$putDouble.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void putReference(Object base, long offset, Object value) {
            try {
                iu$putReference.invokeExact(base, offset, value);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        private static void clearReflectionCache(Class<?> clazz) {
            if (class$reflectionData == null) {
                try {
                    semeru$class$setReflectCache.invokeExact(clazz, (Object) null);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to clear reflection cache", e);
                }
            } else {
                class$reflectionData.setVolatile(clazz, (SoftReference<?>) null);
            }
        }

        @SuppressWarnings("unchecked")
        private static Set<Module> everyoneSet(Lookup moduleLookup) throws ReflectiveOperationException {
            try {
                return (Set<Module>) moduleLookup.findStaticVarHandle(Module.class, "EVERYONE_SET", Set.class).get();
            } catch (IllegalAccessException e) {
                if (e.getMessage() != null && e.getMessage().endsWith("Expected static field.")) {
                    return (Set<Module>) moduleLookup.in(Module.class)
                            .findStaticVarHandle(Module.class, "EVERYONE_SET", Set.class)
                            .get();
                }
                throw e;
            }
        }

        private static MethodHandle bindIu(Lookup lookup, Class<?> iuClass, Object iu, String name, MethodType type)
                throws NoSuchMethodException, IllegalAccessException {
            return lookup.findVirtual(iuClass, name, type).bindTo(iu);
        }

    }

    /**
     * Filter-blind declared members and {@code Class} enum-cache offsets.
     */
    private static final class Members {

        static final MethodHandle GET_FIELDS, GET_METHODS, GET_CONSTRUCTORS, GET_CLASSES;
        static final boolean FIELDS_PUBLIC_ONLY, METHODS_PUBLIC_ONLY, CONSTRUCTORS_PUBLIC_ONLY;
        static final long ENUM_VARS, ENUM_CONSTANTS, ENUM_DIRECTORY;

        static {
            try {
                Lookup lookup = Holder.LOOKUP;
                if (Index.isSemeru()) {
                    GET_FIELDS = lookup.findVirtual(Class.class, "getDeclaredFieldsImpl", MethodType.methodType(Field[].class));
                    FIELDS_PUBLIC_ONLY = false;
                    GET_METHODS = lookup.findVirtual(Class.class, "getDeclaredMethodsImpl", MethodType.methodType(Method[].class));
                    METHODS_PUBLIC_ONLY = false;
                    GET_CONSTRUCTORS = lookup.findVirtual(Class.class, "getDeclaredConstructorsImpl", MethodType.methodType(Constructor[].class));
                    CONSTRUCTORS_PUBLIC_ONLY = false;
                    GET_CLASSES = lookup.findVirtual(Class.class, "getDeclaredClassesImpl", MethodType.methodType(Class[].class));
                } else {
                    GET_FIELDS = lookup.findVirtual(Class.class, "getDeclaredFields0", MethodType.methodType(Field[].class, boolean.class));
                    FIELDS_PUBLIC_ONLY = true;
                    GET_METHODS = lookup.findVirtual(Class.class, "getDeclaredMethods0", MethodType.methodType(Method[].class, boolean.class));
                    METHODS_PUBLIC_ONLY = true;
                    GET_CONSTRUCTORS = lookup.findVirtual(Class.class, "getDeclaredConstructors0", MethodType.methodType(Constructor[].class, boolean.class));
                    CONSTRUCTORS_PUBLIC_ONLY = true;
                    GET_CLASSES = lookup.findVirtual(Class.class, "getDeclaredClasses0", MethodType.methodType(Class[].class));
                }

                Field[] classFields = declaredFields(Class.class);
                if (Index.isSemeru()) {
                    Field enumVars = named(classFields, "enumVars");
                    ENUM_VARS = enumVars == null ? -1L : Holder.objectOffset(enumVars);
                    ENUM_CONSTANTS = -1L;
                    ENUM_DIRECTORY = -1L;
                } else {
                    ENUM_VARS = -1L;
                    Field constants = named(classFields, "enumConstants");
                    Field directory = named(classFields, "enumConstantDirectory");
                    ENUM_CONSTANTS = constants == null ? -1L : Holder.objectOffset(constants);
                    ENUM_DIRECTORY = directory == null ? -1L : Holder.objectOffset(directory);
                }
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to construct member handles", t);
            }
        }

        static Field[] declaredFields(Class<?> clazz) {
            try {
                if (FIELDS_PUBLIC_ONLY) {
                    return (Field[]) GET_FIELDS.invokeExact(clazz, false);
                }
                return (Field[]) GET_FIELDS.invokeExact(clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static Method[] declaredMethods(Class<?> clazz) {
            try {
                if (METHODS_PUBLIC_ONLY) {
                    return (Method[]) GET_METHODS.invokeExact(clazz, false);
                }
                return (Method[]) GET_METHODS.invokeExact(clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static Constructor<?>[] declaredConstructors(Class<?> clazz) {
            try {
                if (CONSTRUCTORS_PUBLIC_ONLY) {
                    return (Constructor<?>[]) GET_CONSTRUCTORS.invokeExact(clazz, false);
                }
                return (Constructor<?>[]) GET_CONSTRUCTORS.invokeExact(clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static Class<?>[] declaredClasses(Class<?> clazz) {
            try {
                return (Class<?>[]) GET_CLASSES.invokeExact(clazz);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void clearEnumCache(Class<?> type) {
            if (ENUM_VARS >= 0L) {
                Holder.putReference(type, ENUM_VARS, null);
                return;
            }
            if (ENUM_CONSTANTS >= 0L) {
                Holder.putReference(type, ENUM_CONSTANTS, null);
            }
            if (ENUM_DIRECTORY >= 0L) {
                Holder.putReference(type, ENUM_DIRECTORY, null);
            }
        }

        private static Field named(Field[] fields, String name) {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().equals(name)) {
                    return fields[i];
                }
            }
            return null;
        }

    }

    /**
     * JEP 472/JEP 500 floors via {@code JavaLangAccess}.
     */
    private static final class Modules {

        static final MethodHandle addNativeAll, addNative, addFinalAll, tryFinal;

        static {
            try {
                MethodHandle nativeAll = null, nativeOne = null, finalAll = null, tryFin = null;
                if (JDK >= 22) {
                    Class<?> jlaClass = Class.forName("jdk.internal.access.JavaLangAccess");
                    Object jla = Holder.LOOKUP.findStatic(
                            Class.forName("jdk.internal.access.SharedSecrets"),
                            "getJavaLangAccess",
                            MethodType.methodType(jlaClass)).invoke();
                    nativeAll = Holder.LOOKUP.findVirtual(jlaClass, "addEnableNativeAccessToAllUnnamed", VOID).bindTo(jla);
                    MethodType oneType = JDK >= 26
                            ? MethodType.methodType(void.class, Module.class)
                            : MethodType.methodType(Module.class, Module.class);
                    nativeOne = Holder.LOOKUP.findVirtual(jlaClass, "addEnableNativeAccess", oneType)
                            .bindTo(jla)
                            .asType(MethodType.methodType(void.class, Module.class));
                    if (JDK >= 26) {
                        finalAll = Holder.LOOKUP.findVirtual(jlaClass, "addEnableFinalMutationToAllUnnamed", VOID).bindTo(jla);
                        tryFin = Holder.LOOKUP.findVirtual(jlaClass, "tryEnableFinalMutation",
                                MethodType.methodType(boolean.class, Module.class)).bindTo(jla);
                    }
                }
                addNativeAll = nativeAll;
                addNative = nativeOne;
                addFinalAll = finalAll;
                tryFinal = tryFin;
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to construct module-access handles", t);
            }
        }

        static void enableFinalFieldMutationFloor() {
            if (addFinalAll == null) {
                return;
            }
            invokeVoid(addFinalAll);
            enableFinalFieldMutation(Object.class.getModule());
        }

        static void enableFinalFieldMutation(Module module) {
            if (module == null || tryFinal == null) {
                return;
            }
            try {
                boolean ignored = (boolean) tryFinal.invokeExact(module);
            } catch (Throwable t) {
                throw rethrow(t);
            }
        }

        static void enableNativeAccessFloor() {
            if (addNativeAll == null) {
                return;
            }
            invokeVoid(addNativeAll);
        }

        static void enableNativeAccess(Module module) {
            if (module == null || addNativeAll == null) {
                return;
            }
            if (!module.isNamed()) {
                invokeVoid(addNativeAll);
                return;
            }
            invokeVoid(addNative, module);
        }

    }

    /**
     * {@code defineClass}/hidden/anonymous/{@code ensureInitialized}.
     */
    private static final class Define {

        static final MethodHandle classLoader$defineClass, iu$defineClass, iu$ensureClassInitialized;
        static final MethodHandle lookup$defineHiddenClass, lookup$ensureInitialized, unsafe$defineAnonymousClass;
        static final Class<?> classOptionClass;

        static {
            try {
                Lookup lookup = Holder.LOOKUP;
                classLoader$defineClass = lookup.findVirtual(ClassLoader.class, "defineClass",
                        MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class));
                Class<?> iuClass = Holder.INTERNAL_UNSAFE.getClass();
                Object iu = Holder.INTERNAL_UNSAFE;
                iu$defineClass = lookup.findVirtual(iuClass, "defineClass",
                        MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
                        .bindTo(iu);
                MethodHandle ensureIu = null;
                if (JDK < 15) {
                    ensureIu = lookup.findVirtual(iuClass, "ensureClassInitialized", MethodType.methodType(void.class, Class.class)).bindTo(iu);
                }
                iu$ensureClassInitialized = ensureIu;

                MethodHandle ensure = null;
                Class<?> optionClass = null;
                MethodHandle defineHidden = null;
                if (JDK >= 15) {
                    ensure = lookup.findVirtual(Lookup.class, "ensureInitialized", MethodType.methodType(Class.class, Class.class));
                    optionClass = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
                    Class<?> optionArray = Class.forName("[Ljava.lang.invoke.MethodHandles$Lookup$ClassOption;");
                    defineHidden = lookup.findVirtual(Lookup.class, "defineHiddenClass", MethodType.methodType(Lookup.class, byte[].class, boolean.class, optionArray))
                            .asFixedArity()
                            .asType(MethodType.methodType(Lookup.class, Lookup.class, byte[].class, boolean.class, Object.class));
                }
                lookup$ensureInitialized = ensure;
                classOptionClass = optionClass;
                lookup$defineHiddenClass = defineHidden;

                MethodHandle anon = null;
                if (JDK < 15) {
                    try {
                        anon = lookup.findVirtual(Unsafe.class, "defineAnonymousClass",
                                MethodType.methodType(Class.class, Class.class, byte[].class, Object[].class));
                    } catch (NoSuchMethodException ignored) { }
                }
                unsafe$defineAnonymousClass = anon;
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to construct define-class handles", t);
            }
        }

    }

    private static final class Agent {

        static final MethodHandle LOAD_AGENT;
        static File dummyJar;

        static {
            MethodHandle load = null;
            try {
                Class<?> instImpl = Class.forName("sun.instrument.InstrumentationImpl");
                load = Holder.LOOKUP.findStatic(instImpl, "loadAgent", MethodType.methodType(void.class, String.class));
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) { }
            LOAD_AGENT = load;
        }

        static File dummyJar() throws IOException {
            File jar = dummyJar;
            if (jar != null && jar.isFile()) {
                return jar;
            }
            synchronized (Agent.class) {
                jar = dummyJar;
                if (jar != null && jar.isFile()) {
                    return jar;
                }
                jar = File.createTempFile("imaginebreaker-agent", ".jar");
                jar.deleteOnExit();
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().putValue("Agent-Class", AgentProbe.class.getName());
                manifest.getMainAttributes().putValue("Launcher-Agent-Class", AgentProbe.class.getName());
                manifest.getMainAttributes().putValue("Can-Redefine-Classes", "true");
                manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
                JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), manifest);
                jos.close();
                dummyJar = jar;
                return jar;
            }
        }

    }

    private static final class ClassPath {

        static final Object UCP;
        static final MethodHandle ADD_URL, GET_URLS;

        static {
            try {
                Lookup lookup = Holder.LOOKUP;
                ClassLoader sys = ClassLoader.getSystemClassLoader();
                Object ucp;
                MethodHandle add;
                MethodHandle get;
                try {
                    Class<?> bcl = Class.forName("jdk.internal.loader.BuiltinClassLoader");
                    Class<?> ucpType = Class.forName("jdk.internal.loader.URLClassPath");
                    ucp = lookup.findGetter(bcl, "ucp", ucpType)
                            .asType(MethodType.methodType(Object.class, ClassLoader.class))
                            .invokeExact(sys);
                    add = lookup.findVirtual(ucpType, "addURL", MethodType.methodType(void.class, URL.class))
                            .asType(MethodType.methodType(void.class, Object.class, URL.class));
                    get = lookup.findVirtual(ucpType, "getURLs", MethodType.methodType(URL[].class))
                            .asType(MethodType.methodType(URL[].class, Object.class));
                } catch (Throwable ignored) {
                    ucp = fallbackUcp(sys);
                    Method addURL = findMethod(ucp.getClass(), "addURL", URL.class);
                    Method getURLs = findMethod(ucp.getClass(), "getURLs");
                    if (addURL == null || getURLs == null) {
                        throw new IllegalStateException("URLClassPath addURL/getURLs not found");
                    }
                    add = lookup.unreflect(addURL).asType(MethodType.methodType(void.class, Object.class, URL.class));
                    get = lookup.unreflect(getURLs).asType(MethodType.methodType(URL[].class, Object.class));
                }
                if (ucp == null) {
                    throw new IllegalStateException("URLClassPath field not found on " + sys.getClass().getName());
                }
                UCP = ucp;
                ADD_URL = add;
                GET_URLS = get;
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Unable to bind system classpath", t);
            }
        }

    }

    /**
     * Makes every package look open/exported to everyone.
     * Paired with {@code addExportsToAll0} so the VM agrees with {@link Module#isOpen(String)}.
     */
    private static final class WorldRejector extends AbstractMap<String, Set<Module>> {

        private static WorldRejector INSTANCE;

        private static void init(Set<Module> everyone) {
            INSTANCE = new WorldRejector(everyone);
        }

        private final Set<Module> everyone;

        private WorldRejector(Set<Module> everyone) {
            this.everyone = everyone;
        }

        @Override
        public Set<Module> get(Object key) {
            return everyone;
        }

        @Override
        public Set<Entry<String, Set<Module>>> entrySet() {
            return Collections.emptySet();
        }

    }

}
