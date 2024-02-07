package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InaccessibleObjectException;

public class OpenModulesTest {

    @Test
    public void openBootModules() {
        Assertions.assertThrows(InaccessibleObjectException.class, this::retrieveStringBackingArray);
        ImagineBreaker.openBootModules();
        Assertions.assertDoesNotThrow(this::retrieveStringBackingArray);
    }

    private byte[] retrieveStringBackingArray() throws ReflectiveOperationException {
        var value = String.class.getDeclaredField("value");
        value.setAccessible(true);
        return (byte[]) value.get("");
    }

}
