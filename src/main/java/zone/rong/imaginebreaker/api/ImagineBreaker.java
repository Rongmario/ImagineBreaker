package zone.rong.imaginebreaker.api;

import java.lang.invoke.MethodHandles.Lookup;

public interface ImagineBreaker {

    Lookup trustedLookup();

    void clearFieldFilters();

    void clearMethodFilters();

}
