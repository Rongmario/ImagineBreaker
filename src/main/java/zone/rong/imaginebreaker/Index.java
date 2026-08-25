package zone.rong.imaginebreaker;

import zone.rong.imaginebreaker.api.ImagineBreaker;
import zone.rong.imaginebreaker.impl.ImagineBreakerImpl;

import java.util.Locale;

public final class Index {

    private static final boolean isSemeru = System.getProperty("java.vm.vendor").toLowerCase(Locale.ENGLISH).contains("openj9");

    public static ImagineBreaker get() {
        return PrivilegeEnclave.IMPL;
    }

    public static boolean isSemeru() {
        return isSemeru;
    }

    /**
     * @deprecated use {@link #isSemeru()}
     */
    @Deprecated
    public static boolean isOpenJ9() {
        return isSemeru();
    }

    private static final class PrivilegeEnclave {

        private static final ImagineBreaker IMPL = new ImagineBreakerImpl();

    }

}
