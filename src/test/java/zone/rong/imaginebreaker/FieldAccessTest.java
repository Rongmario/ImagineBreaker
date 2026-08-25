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
    public void getAndSetPrimitiveFields() {
        ImagineBreaker ib = Index.get();
        Box box = new Box();
        box.flag = true;
        box.b = 1;
        box.s = 2;
        box.c = 'a';
        box.n = 3;
        box.l = 4L;
        box.f = 5.5F;
        box.d = 6.5D;

        Field flag = ib.declaredField(Box.class, "flag");
        Field b = ib.declaredField(Box.class, "b");
        Field s = ib.declaredField(Box.class, "s");
        Field c = ib.declaredField(Box.class, "c");
        Field n = ib.declaredField(Box.class, "n");
        Field l = ib.declaredField(Box.class, "l");
        Field f = ib.declaredField(Box.class, "f");
        Field d = ib.declaredField(Box.class, "d");

        Assertions.assertTrue(ib.getBoolean(box, flag));
        Assertions.assertEquals((byte) 1, ib.getByte(box, b));
        Assertions.assertEquals((short) 2, ib.getShort(box, s));
        Assertions.assertEquals('a', ib.getChar(box, c));
        Assertions.assertEquals(3, ib.getInt(box, n));
        Assertions.assertEquals(4L, ib.getLong(box, l));
        Assertions.assertEquals(5.5F, ib.getFloat(box, f));
        Assertions.assertEquals(6.5D, ib.getDouble(box, d));

        ib.setBoolean(box, flag, false);
        ib.setByte(box, b, (byte) 11);
        ib.setShort(box, s, (short) 12);
        ib.setChar(box, c, 'z');
        ib.setInt(box, n, 13);
        ib.setLong(box, l, 14L);
        ib.setFloat(box, f, 15.5F);
        ib.setDouble(box, d, 16.5D);

        Assertions.assertFalse(box.flag);
        Assertions.assertEquals((byte) 11, box.b);
        Assertions.assertEquals((short) 12, box.s);
        Assertions.assertEquals('z', box.c);
        Assertions.assertEquals(13, box.n);
        Assertions.assertEquals(14L, box.l);
        Assertions.assertEquals(15.5F, box.f);
        Assertions.assertEquals(16.5D, box.d);
    }

    @Test
    public void primitiveAccessRejectsWideningConversions() {
        ImagineBreaker ib = Index.get();
        Box box = new Box();
        box.b = 3;
        box.n = 4;

        Field b = ib.declaredField(Box.class, "b");
        Field n = ib.declaredField(Box.class, "n");
        Field l = ib.declaredField(Box.class, "l");
        Field d = ib.declaredField(Box.class, "d");

        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.getInt(box, b));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.getLong(box, n));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.setByte(box, n, (byte) 5));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.set(box, l, Integer.valueOf(6)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.setFloat(box, d, 7.5F));
    }

    @Test
    public void invalidFieldAccessThrowsBeforeUsingUnsafe() {
        ImagineBreaker ib = Index.get();
        Box box = new Box();
        Field n = ib.declaredField(Box.class, "n");
        Field label = ib.declaredField(Box.class, "label");

        Assertions.assertThrows(NullPointerException.class, () -> ib.get(null, n));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.get(new Object(), n));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.getBoolean(box, n));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.getInt(box, label));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.setInt(box, label, 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.set(box, n, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.set(box, n, "not an int"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ib.set(box, label, new Object()));
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
    public void setStaticFinalPrimitive() {
        ImagineBreaker ib = Index.get();
        Field field = ib.declaredField(Box.class, "INT_CONST");
        int previous = ib.getInt(null, field);
        try {
            ib.setInt(null, field, 42);
            Assertions.assertEquals(42, ib.getInt(null, field));
        } finally {
            ib.setInt(null, field, previous);
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
        static final int INT_CONST = 7;
        boolean flag;
        byte b;
        short s;
        char c;
        int n;
        long l;
        float f;
        double d;
        String label;

    }

}
