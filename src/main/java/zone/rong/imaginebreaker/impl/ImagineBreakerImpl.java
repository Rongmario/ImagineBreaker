package zone.rong.imaginebreaker.impl;

import sun.reflect.ReflectionFactory;
import zone.rong.imaginebreaker.Index;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * TODO: likely will break when <a href="https://openjdk.org/projects/amber/design-notes/towards-better-serialization">this</a> is fully realized
 */
public final class ImagineBreakerImpl implements ImagineBreaker {

    private Lookup trustedLookup;

    @Override
    public Lookup trustedLookup() {
        if (this.trustedLookup == null) {
            try {
                ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
                Field factoryField = ReflectionFactory.class.getDeclaredField("delegate");
                factoryField.setAccessible(true);
                Object factory = factoryField.get(null);

                Constructor<?> ctor = reflectionFactory.newConstructorForSerialization(Lookup.class, Lookup.class.getDeclaredConstructor(Class.class));
                Lookup factoryLookup = (Lookup) ctor.newInstance(factory.getClass());

                Method newFieldAccessorMethod = factory.getClass().getMethod("newFieldAccessor", Field.class, boolean.class);
                MethodHandle newFieldAccessorHandle = factoryLookup.unreflect(newFieldAccessorMethod);

                Method getMethod = newFieldAccessorMethod.getReturnType().getMethod("get", Object.class);
                MethodHandle getMethodHandle = factoryLookup.unreflect(getMethod);

                Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
                Object implLookupFieldAccessor = newFieldAccessorHandle.invoke(factory, implLookupField, false);

                this.trustedLookup = (Lookup) getMethodHandle.invoke(implLookupFieldAccessor, null);
            } catch (Throwable t) {
                throw new IllegalStateException("Unable to get trusted lookup instance", t);
            }
        }
        return this.trustedLookup;
    }

    @Override
    public void clearFieldFilters() {
        Map<Class, ?> fieldFilterMap = (Map<Class, ?>) Holder.reflection$fieldFilterMap.get();
        Holder.reflection$fieldFilterMap.set((Map) null);
        fieldFilterMap.keySet().forEach(clazz -> Holder.clearReflectionCache(clazz));
    }

    @Override
    public void clearMethodFilters() {
        Map<Class, ?> methodFilterMap = (Map<Class, ?>) Holder.reflection$methodFilterMap.get();
        Holder.reflection$methodFilterMap.set((Map) null);
        methodFilterMap.keySet().forEach(clazz -> Holder.clearReflectionCache(clazz));
    }

    private static final class Holder {

        private static final ImagineBreaker $ = Index.get();
        private static final VarHandle class$module, class$reflectionData, reflection$fieldFilterMap, reflection$methodFilterMap;
        private static final MethodHandle semeru$class$setReflectCache;

        static {
            try {
                class$module = $.trustedLookup().findVarHandle(Class.class, "module", Module.class);
                class$reflectionData = Index.isSemeru() ? null : $.trustedLookup().findVarHandle(Class.class, "reflectionData", SoftReference.class);

                Class<?> reflectionClass = Class.forName("jdk.internal.reflect.Reflection");
                reflection$fieldFilterMap = $.trustedLookup().findStaticVarHandle(reflectionClass, "fieldFilterMap", Map.class);
                reflection$methodFilterMap = $.trustedLookup().findStaticVarHandle(reflectionClass, "methodFilterMap", Map.class);

                semeru$class$setReflectCache = Index.isSemeru() ? $.trustedLookup().findSetter(Class.class, "reflectCache", Class.forName("java.lang.Class$ReflectCache")) : null;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to construct handles", e);
            }
        }

        private static void clearReflectionCache(Class<?> clazz) {
            if (class$reflectionData == null) {
                try {
                    semeru$class$setReflectCache.invoke(clazz, null);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to clear reflection cache", e);
                }
            } else {
                class$reflectionData.setVolatile(clazz, (SoftReference<?>) null);
            }
        }

    }

}
