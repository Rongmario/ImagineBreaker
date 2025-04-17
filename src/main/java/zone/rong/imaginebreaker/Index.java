package zone.rong.imaginebreaker;

import org.jspecify.annotations.NonNull;
import zone.rong.imaginebreaker.api.ImagineBreaker;
import zone.rong.imaginebreaker.impl.ImagineBreakerImpl;

import java.util.Locale;

public final class Index {

    private static final boolean isSemeru = System.getProperty("java.vm.vendor").toLowerCase(Locale.ENGLISH).contains("openj9");

    private static ImagineBreaker impl;

    @NonNull
    public static ImagineBreaker get() {
        if (impl == null) {
            impl = new ImagineBreakerImpl();
        }
        return impl;
    }

    public static boolean isSemeru() {
        return isSemeru;
    }

    @Deprecated
    public static boolean isOpenJ9() {
        return isSemeru();
    }

}
