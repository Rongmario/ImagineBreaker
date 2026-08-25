package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
public class DisguiseTest {

    @Test
    public void disguiseAsJavaBase() {
        Module original = DisguiseTest.class.getModule();
        Assertions.assertNotEquals("java.base", original.getName());

        Index.get().disguiseAsModule(DisguiseTest.class, Object.class);
        try {
            Assertions.assertSame(Object.class.getModule(), DisguiseTest.class.getModule());
            Assertions.assertEquals("java.base", DisguiseTest.class.getModule().getName());
        } finally {
            Index.get().disguiseAsModule(DisguiseTest.class, original);
        }
        Assertions.assertSame(original, DisguiseTest.class.getModule());
    }

    @Test
    public void disguiseRestoredAfterAction() {
        Module original = DisguiseTest.class.getModule();
        Index.get().disguiseAsModule(DisguiseTest.class, Object.class, () ->
                Assertions.assertEquals("java.base", DisguiseTest.class.getModule().getName()));
        Assertions.assertSame(original, DisguiseTest.class.getModule());
    }

    @Test
    public void disguiseRestoredAfterThrowingAction() {
        Module original = DisguiseTest.class.getModule();
        Assertions.assertThrows(IllegalStateException.class, () ->
                Index.get().disguiseAsModule(DisguiseTest.class, Object.class, () -> {
                    throw new IllegalStateException("boom");
                }));
        Assertions.assertSame(original, DisguiseTest.class.getModule());
    }

}
