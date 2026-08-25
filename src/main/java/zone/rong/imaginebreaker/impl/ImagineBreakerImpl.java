package zone.rong.imaginebreaker.impl;

import sun.reflect.ReflectionFactory;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;

/**
 * Trusted lookup via {@link ReflectionFactory#newConstructorForSerialization}
 * on {@code Lookup}'s {@code TRUSTED} constructor, then {@code IMPL_LOOKUP}.
 * Likely to break when
 * <a href="https://openjdk.org/projects/amber/design-notes/towards-better-serialization">better serialization</a>
 * is fully realized.
 * {@code PanamaImagineBreaker} is the alternative.
 */
public final class ImagineBreakerImpl extends AbstractImagineBreaker {

    @Override
    protected Lookup obtainTrustedLookup() {
        try {
            ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
            Constructor<?> serial;
            Lookup trusted;
            if (Runtime.version().major() >= 14) {
                serial = reflectionFactory.newConstructorForSerialization(
                        Lookup.class, Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class));
                trusted = (Lookup) serial.newInstance(Object.class, null, -1);
            } else {
                serial = reflectionFactory.newConstructorForSerialization(
                        Lookup.class, Lookup.class.getDeclaredConstructor(Class.class, int.class));
                trusted = (Lookup) serial.newInstance(Object.class, -1);
            }
            return (Lookup) trusted.findStaticGetter(Lookup.class, "IMPL_LOOKUP", Lookup.class).invokeExact();
        } catch (Throwable t) {
            throw new IllegalStateException("Unable to get trusted lookup instance", t);
        }
    }

}
