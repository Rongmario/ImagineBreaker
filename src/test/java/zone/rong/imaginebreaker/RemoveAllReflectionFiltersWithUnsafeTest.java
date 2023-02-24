package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class RemoveAllReflectionFiltersWithUnsafeTest {

    @Test
    public void run() {
        Assertions.assertThrows(NoSuchFieldException.class, () -> Field.class.getDeclaredField("modifiers"));
        ImagineBreaker.removeAllReflectionFiltersUnsafely();
        Assertions.assertDoesNotThrow(() -> Field.class.getDeclaredField("modifiers"));
    }

}
