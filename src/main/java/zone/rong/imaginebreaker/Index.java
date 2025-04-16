package zone.rong.imaginebreaker;

import org.jspecify.annotations.NonNull;
import zone.rong.imaginebreaker.api.ImagineBreaker;
import zone.rong.imaginebreaker.impl.ImagineBreakerImpl;

public final class Index {

    private static ImagineBreaker impl;

    @NonNull
    public static ImagineBreaker get() {
        if (impl == null) {
            impl = new ImagineBreakerImpl();
        }
        return impl;
    }

}
