package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

@Isolated
public class FinalMutationTest {

    static final class Box {

        final int x;

        Box(int x) {
            this.x = x;
        }

    }

    @BeforeEach
    void enableMutation() {
        Index.get().enableFinalFieldMutation();
    }

    @Test
    public void instanceFinalViaFieldSet() throws Exception {
        Box box = new Box(1);
        Field field = Box.class.getDeclaredField("x");
        field.setAccessible(true);
        field.setInt(box, 99);
        Assertions.assertEquals(99, field.getInt(box));
    }

    @Test
    public void instanceFinalViaTrustedUnreflectSetter() throws Throwable {
        Box box = new Box(1);
        Field field = Box.class.getDeclaredField("x");
        MethodHandle setter = Index.get().trustedLookup().unreflectSetter(field);
        setter.invoke(box, 77);
        Assertions.assertEquals(77, field.getInt(box));
    }

}
