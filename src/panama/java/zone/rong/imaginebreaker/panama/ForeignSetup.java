package zone.rong.imaginebreaker.panama;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.*;

// TODO
class ForeignSetup {

    static final MemorySegment mainVmPointer;
    static final MethodHandle getEnv, getClass, getStaticFieldId, getStaticFieldObject;

    static {
        // Load java library
        Runtime.getRuntime().loadLibrary("java");

        var javaHomePath = System.getProperty("java.home");
        var jvmLibName = System.mapLibraryName("jvm");

        var jvmLibPath = javaHomePath + "/lib/server/" + jvmLibName;
        if (!new File(jvmLibPath).exists()) {
            jvmLibPath = javaHomePath + "/bin/server/" + jvmLibName;
        }
        // Load jvm library
        Runtime.getRuntime().load(jvmLibPath);

        try {
            var JNI_GetCreatedJavaVM_MH = Linker.nativeLinker()
                    .downcallHandle(FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS))
                    .bindTo(SymbolLookup.loaderLookup().find("JNI_GetCreatedJavaVMs").get());
            var global = Arena.global();
            var vm = global.allocate(ADDRESS);
            var numVMs = global.allocate(JAVA_INT, 0);
            int _ = (int) JNI_GetCreatedJavaVM_MH.invokeExact(vm, 1, numVMs);
            mainVmPointer = vm.get(ADDRESS, 0);
            getEnv = Linker.nativeLinker()
                    .downcallHandle(FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT))
                    .bindTo(SymbolLookup.loaderLookup().find("JNU_GetEnv").get());
            getClass = Linker.nativeLinker().downcallHandle(FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
            getStaticFieldId = Linker.nativeLinker().downcallHandle(FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
            getStaticFieldObject = Linker.nativeLinker().downcallHandle(FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS, ADDRESS));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
