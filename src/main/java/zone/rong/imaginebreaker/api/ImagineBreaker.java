package zone.rong.imaginebreaker.api;

import java.lang.invoke.MethodHandles.Lookup;

public interface ImagineBreaker {

    Lookup trustedLookup();

    default Lookup trustedLookup(Class<?> lookupClass) {
        return trustedLookup(lookupClass, null);
    }

    default Lookup trustedLookup(ClassLoader classLoader) {
        try {
            return trustedLookup(Class.forName("java.lang.Object", false, classLoader));
        } catch (ClassNotFoundException e) {
            return null; // Could not find Object???
        }
    }

    Lookup trustedLookup(Class<?> lookupClass, Class<?> prevLookupClass);

    void clearFieldFilters();

    void clearMethodFilters();

}
