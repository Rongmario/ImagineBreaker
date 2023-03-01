package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.InaccessibleObjectException;

public class OpenAllBaseModulesNativelyTest {

    @Test
    public void run() {
        Assertions.assertThrows(InaccessibleObjectException.class, () -> File.class.getDeclaredField("status").setAccessible(true));
        NativeImagineBreaker.openBaseModules();
        Assertions.assertDoesNotThrow(() -> File.class.getDeclaredField("status").setAccessible(true));
    }

}
