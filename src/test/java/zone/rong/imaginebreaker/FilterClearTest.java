package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterClearTest {

    @Test
    public void clearMethodFilters() {
        Assertions.assertDoesNotThrow(() -> Index.get().clearMethodFilters());
    }

    @Test
    public void clearFieldFilters() {
        Assertions.assertDoesNotThrow(() -> Index.get().clearFieldFilters());
    }

}
