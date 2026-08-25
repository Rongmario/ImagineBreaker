package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.lang.reflect.Field;

@Isolated
public class FieldAccessTest {

    @Test
    public void getAndSetInstanceFields() {
        ImagineBreaker ib = Index.get();
        Box box = new Box();
        box.flag = true;
        box.n = 4;
        box.label = "a";

        Field flag = ib.declaredField(Box.class, "flag");
        Field n = ib.declaredField(Box.class, "n");
        Field label = ib.declaredField(Box.class, "label");

        Assertions.assertEquals(Boolean.TRUE, ib.get(box, flag));
        Assertions.assertEquals(4, ib.get(box, n));
        Assertions.assertEquals("a", ib.get(box, label));

        ib.set(box, flag, Boolean.FALSE);
        ib.set(box, n, 9);
        ib.set(box, label, "b");
        Assertions.assertEquals(Boolean.FALSE, ib.get(box, flag));
        Assertions.assertEquals(9, ib.get(box, n));
        Assertions.assertEquals("b", ib.get(box, label));
    }

    @Test
    public void copyInstanceField() {
        ImagineBreaker ib = Index.get();
        Box from = new Box();
        from.n = 12;
        Box to = new Box();
        ib.copy(from, to, ib.declaredField(Box.class, "n"));
        Assertions.assertEquals(12, to.n);
    }

    @Test
    public void setStaticFinal() {
        ImagineBreaker ib = Index.get();
        Field field = ib.declaredField(Box.class, "CONST");
        Object previous = ib.get(null, field);
        try {
            ib.set(null, field, "mutated");
            Assertions.assertEquals("mutated", ib.get(null, field));
        } finally {
            ib.set(null, field, previous);
        }
    }

    @Test
    public void fieldOffsetMatchesUnsafe() {
        ImagineBreaker ib = Index.get();
        Field n = ib.declaredField(Box.class, "n");
        Assertions.assertEquals(ib.unsafe().objectFieldOffset(n), ib.fieldOffset(n));
        Field c = ib.declaredField(Box.class, "CONST");
        Assertions.assertEquals(ib.unsafe().staticFieldOffset(c), ib.fieldOffset(c));
    }

    static final class Box {

        static final String CONST = "const";
        boolean flag;
        int n;
        String label;

    }

}
