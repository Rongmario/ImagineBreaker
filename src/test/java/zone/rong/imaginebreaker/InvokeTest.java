package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Isolated
public class InvokeTest {

    @Test
    public void invokePrivateInstanceAndStatic() {
        ImagineBreaker ib = Index.get();
        Secret secret = new Secret(2);
        Method plus = ib.declaredMethod(Secret.class, "plus", int.class);
        Assertions.assertEquals(5, ib.invoke(secret, plus, 3));
        Method id = ib.declaredMethod(Secret.class, "id");
        Assertions.assertEquals(11, ib.invoke(null, id));
    }

    @Test
    public void constructPrivate() {
        ImagineBreaker ib = Index.get();
        Constructor<Secret> ctor = ib.declaredConstructor(Secret.class, int.class);
        Secret secret = ib.newInstance(ctor, 4);
        Assertions.assertEquals(4, secret.value);
    }

    @Test
    public void allocateSkipsConstructor() {
        ImagineBreaker ib = Index.get();
        Secret secret = ib.allocate(Secret.class);
        Assertions.assertEquals(0, secret.value);
    }

    static final class Secret {

        final int value;

        private Secret() {
            this(11);
        }

        private Secret(int value) {
            this.value = value;
        }

        private int plus(int n) {
            return value + n;
        }

        private static int id() {
            return 11;
        }

    }

}
