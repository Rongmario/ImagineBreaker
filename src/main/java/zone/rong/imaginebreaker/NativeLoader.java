package zone.rong.imaginebreaker;

final class NativeLoader {

    public static void load() {
        try {
            System.loadLibrary("imagineBreakerNatives");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private NativeLoader() { }

}
