package zone.rong.imaginebreaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Locale;

final class NativeLoader {

    public static final OperatingSystem OS;
    public static final int ARCH_BITS;

    enum OperatingSystem {

        Windows,
        MacOSX,
        Linux

    }

    static {
        String osName = null;
        try {
            osName = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
        } catch (final SecurityException ignore) { }
        if (osName == null) {
            throw new RuntimeException("Unknown OS, cannot load natives.");
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            OS = OperatingSystem.MacOSX;
        } else if (osName.contains("win")) {
            OS = OperatingSystem.Windows;
        } else if (osName.contains("nux")) {
            OS = OperatingSystem.Linux;
        } else if (osName.contains("sunos") || osName.contains("solaris")) {
            throw new RuntimeException("Solaris not supported, cannot load natives.");
        } else if (osName.contains("bsd") || osName.contains("nix") || osName.contains("aix")) {
            throw new RuntimeException("Other Unix-Based OS's not supported, cannot load natives.");
        } else {
            throw new RuntimeException("Unknown OS, cannot load natives.");
        }
        int archBits = 64;
        final var dataModel = System.getProperty("sun.arch.data.model");
        if (dataModel != null && dataModel.contains("32")) {
            archBits = 32;
        } else {
            final var osArch = System.getProperty("os.arch");
            if (osArch != null && ((osArch.contains("86") && !osArch.contains("64")) || osArch.contains("32"))) {
                archBits = 32;
            }
        }
        ARCH_BITS = archBits;
    }

    public static void load() {
        try {
            System.loadLibrary(getRawLibraryName());
        } catch (Throwable t) {
            loadFromJar(getLibraryName());
        }
    }

    private static void loadFromJar(final String libraryResourcePath) {
        File tempFile = null;
        boolean tempFileIsPosix = false;
        Exception exception = null;
        try (var is = ImagineBreaker.class.getResourceAsStream(libraryResourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Could not find library within jar: " + libraryResourcePath);
            }
            // Extract library to temp file
            final var filename = libraryResourcePath.substring(libraryResourcePath.lastIndexOf('/') + 1);
            final int dotIdx = filename.indexOf('.');
            final var baseName = dotIdx < 0 ? filename : filename.substring(0, dotIdx);
            final var suffix = dotIdx < 0 ? ".so" : filename.substring(dotIdx);
            tempFile = File.createTempFile(baseName + "_", suffix);
            tempFile.deleteOnExit();
            try {
                if (tempFile.toPath().getFileSystem().supportedFileAttributeViews().contains("posix")) {
                    tempFileIsPosix = true;
                }
            } catch (final Exception ignore) { }
            final var buffer = new byte[8192];
            try (final var os = new FileOutputStream(tempFile)) {
                for (int readBytes; (readBytes = is.read(buffer)) != -1;) {
                    os.write(buffer, 0, readBytes);
                }
            }
            System.load(tempFile.getAbsolutePath());
        } catch (Exception e) {
            exception = e;
        }
        if (tempFile != null && tempFileIsPosix) {
            tempFile.delete();
        }
        if (exception != null) {
            throw new RuntimeException("Could not load library " + libraryResourcePath + " : " + exception);
        }
    }

    private static String getRawLibraryName() {
        return "libImagineBreaker";
    }

    private static String getLibraryName() {
        var name = "libImagineBreaker_";
        var arch = "x" + ARCH_BITS;
        var ext = OS == OperatingSystem.Windows ? ".dll" : OS == OperatingSystem.MacOSX ? ".dylib" : ".so";
        return name + arch + ext;
    }

    private NativeLoader() { }

}
