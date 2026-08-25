package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.lang.instrument.Instrumentation;

@Isolated
public class AgentTest {

    @Test
    public void instrumentationOrUnsupported() {
        try {
            Instrumentation instrumentation = Index.get().instrumentation();
            Assertions.assertNotNull(instrumentation);
            Assertions.assertTrue(instrumentation.getAllLoadedClasses().length > 0);
        } catch (IllegalStateException e) {
            Assumptions.assumeTrue(false, "dynamic agent loading refused: " + e.getMessage());
        }
    }

}
