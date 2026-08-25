package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

@Isolated
public class LookupTest {

    @Test
    public void trustedLookup() {
        Lookup lookup = Index.get().trustedLookup();
        Assertions.assertEquals(Object.class, lookup.lookupClass());
        Assertions.assertTrue(lookup.toString().contains("trusted"), lookup.toString());
        int modes = lookup.lookupModes();
        if (Runtime.version().major() >= 17) {
            Assertions.assertEquals(127, modes);
        } else {
            Assertions.assertTrue(modes >= 31, "lookupModes=" + modes);
        }
    }

    @Test
    public void trustedAllowedModes() throws Throwable {
        Index.get().clearFieldFilters();
        Field allowedModes = Lookup.class.getDeclaredField("allowedModes");
        int value = (int) Index.get().trustedLookup().unreflectGetter(allowedModes).invoke(Index.get().trustedLookup());
        Assertions.assertEquals(-1, value);
    }

}
