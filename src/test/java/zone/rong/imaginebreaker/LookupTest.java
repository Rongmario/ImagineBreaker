package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LookupTest {

    @Test
    public void test() {
        Assertions.assertDoesNotThrow(() -> Index.get().trustedLookup());
    }

}
