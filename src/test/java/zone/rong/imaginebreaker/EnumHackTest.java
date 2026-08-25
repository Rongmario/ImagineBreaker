package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

@Isolated
public class EnumHackTest {

    @Test
    public void addEnumConstant() {
        ImagineBreaker ib = Index.get();
        Assertions.assertEquals(2, Flavor.values().length);
        Flavor extra = ib.newEnum(Flavor.class, "SALT", 2, new Class<?>[0], new Object[0]);
        Assertions.assertEquals("SALT", extra.name());
        Assertions.assertEquals(2, extra.ordinal());
        ib.addEnum(Flavor.class, extra);
        Flavor[] values = Flavor.values();
        Assertions.assertEquals(3, values.length);
        Assertions.assertSame(extra, values[2]);
        Assertions.assertEquals(3, Flavor.class.getEnumConstants().length);
    }

    @Test
    public void newEnumWithPayload() {
        ImagineBreaker ib = Index.get();
        Named extra = ib.newEnum(Named.class, "C", 2, new Class<?>[] { String.class }, new Object[] { "cee" });
        Assertions.assertEquals("cee", extra.label);
        ib.addEnum(Named.class, extra);
        Assertions.assertEquals(3, Named.values().length);
        Assertions.assertEquals("cee", Named.values()[2].label);
    }

    enum Flavor {

        VANILLA,
        CHOCOLATE

    }

    enum Named {

        A("a"),
        B("b");

        final String label;

        Named(String label) {
            this.label = label;
        }

    }

}
