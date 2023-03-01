package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class RemoveAllReflectionFiltersNativelyTest {

    @Test
    public void run() {
        Assertions.assertThrows(NoSuchFieldException.class, () -> Field.class.getDeclaredField("modifiers"));
        NativeImagineBreaker.removeAllReflectionFilters();
        Assertions.assertDoesNotThrow(() -> Field.class.getDeclaredField("modifiers"));
    }

}
