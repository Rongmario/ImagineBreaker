package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class RemoveFieldFiltersTest {

    @Test
    public void removeFieldFilters() {
        Assertions.assertThrows(NoSuchFieldException.class, this::retrieveModifiersField);
        ImagineBreaker.removeFieldReflectionFilters();
        Assertions.assertDoesNotThrow(this::retrieveModifiersField);
    }

    private Field retrieveModifiersField() throws NoSuchFieldException {
        return Field.class.getDeclaredField("modifiers");
    }

}
