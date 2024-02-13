package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

public class ModuleTest {

    @Test
    public void openBootModules() throws ReflectiveOperationException {
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
