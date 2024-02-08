package zone.rong.imaginebreaker;

final class NativeLoader {

    public static void load() {
        try {
            System.loadLibrary("libImagineBreaker");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private NativeLoader() { }

}
