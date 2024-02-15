package zone.rong.imaginebreaker.module;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.lang.reflect.Field;

@Isolated
public class ModuleTest {

    @Test
    public void openBootModules() {
        Assertions.assertThrows(RuntimeException.class, this::retrieveStringBackingArray);
        ImagineBreaker.openBootModules();
        Assertions.assertDoesNotThrow(this::retrieveStringBackingArray);
    }

    private byte[] retrieveStringBackingArray() throws ReflectiveOperationException {
        Field value = String.class.getDeclaredField("value");
        value.setAccessible(true);
        return (byte[]) value.get("");
    }

}
