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
import java.util.Set;

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
    @SuppressWarnings("unchecked")
    public void clearFieldFilters() {
        Map<Class<?>, Set<String>> fieldFilterMap = (Map<Class<?>, Set<String>>) Holder.reflection$fieldFilterMap.get();
        Holder.reflection$fieldFilterMap.set((Map<Class<?>, Set<String>>) null);
        fieldFilterMap.keySet().forEach(Holder::clearReflectionCache);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearMethodFilters() {
        Map<Class<?>, Set<String>> methodFilterMap = (Map<Class<?>, Set<String>> ) Holder.reflection$methodFilterMap.get();
        Holder.reflection$methodFilterMap.set((Map<Class<?>, Set<String>>) null);
        methodFilterMap.keySet().forEach(Holder::clearReflectionCache);
    }

    @Override
    public Lookup trustedLookup(Class<?> lookupClass, Class<?> prevLookupClass) {
        try {
            return (Lookup) Holder.lookup$newLookup.invoke(lookupClass, prevLookupClass, Holder.lookup$trust.get(this.trustedLookup()));
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get trusted lookup instance", t);
        }
    }

    private static final class Holder {

        private static final ImagineBreaker $ = Index.get();
        private static final VarHandle class$module, class$reflectionData, reflection$fieldFilterMap, reflection$methodFilterMap, lookup$trust;
        private static final MethodHandle semeru$class$setReflectCache, lookup$newLookup;

        static {
            try {
                class$module = $.trustedLookup().findVarHandle(Class.class, "module", Module.class);
                class$reflectionData = Index.isSemeru() ? null : $.trustedLookup().findVarHandle(Class.class, "reflectionData", SoftReference.class);

                Class<?> reflectionClass = Class.forName("jdk.internal.reflect.Reflection");
                Lookup reflectionLookup = $.trustedLookup().in(reflectionClass);
                reflection$fieldFilterMap = reflectionLookup.findStaticVarHandle(reflectionClass, "fieldFilterMap", Map.class);
                reflection$methodFilterMap = reflectionLookup.findStaticVarHandle(reflectionClass, "methodFilterMap", Map.class);

                semeru$class$setReflectCache = Index.isSemeru() ? $.trustedLookup().findSetter(Class.class, "reflectCache", Class.forName("java.lang.Class$ReflectCache")) : null;

                Lookup lookupLookup = $.trustedLookup().in(Lookup.class);
                lookup$newLookup = lookupLookup.unreflect(Lookup.class.getDeclaredMethod("newLookup", Class.class, Class.class, int.class));
                lookup$trust = lookupLookup.findVarHandle(Lookup.class, "allowedModes", int.class);
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
