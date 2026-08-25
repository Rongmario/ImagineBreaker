package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

@Isolated
public class NativeAccessTest {

    @Test
    public void unnamedModuleHasNativeAccess() throws Throwable {
        Assumptions.assumeTrue(Runtime.version().major() >= 22);
        Index.get().enableNativeAccess();
        Lookup lookup = Index.get().trustedLookup();
        VarHandle enableNativeAccess = lookup.findVarHandle(Module.class, "enableNativeAccess", boolean.class);
        Module allUnnamed = (Module) lookup.findStaticVarHandle(Module.class, "ALL_UNNAMED_MODULE", Module.class).get();
        Assertions.assertTrue((boolean) enableNativeAccess.get(allUnnamed));
    }

    @Test
    public void unnamedModuleHasFinalMutationOn26() throws Throwable {
        Assumptions.assumeTrue(Runtime.version().major() >= 26);
        Index.get().enableFinalFieldMutation();
        Lookup lookup = Index.get().trustedLookup();
        VarHandle enableFinalMutation = lookup.findVarHandle(Module.class, "enableFinalMutation", boolean.class);
        Module allUnnamed = (Module) lookup.findStaticVarHandle(Module.class, "ALL_UNNAMED_MODULE", Module.class).get();
        Assertions.assertTrue((boolean) enableFinalMutation.get(allUnnamed));
        Assertions.assertTrue((boolean) enableFinalMutation.get(Object.class.getModule()));
    }

}
