package ai.braineous.motion.perception.observation;

import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ObservationOrchestratorIT {

    @Inject
    ObservationOrchestrator observationOrchestrator;

    @Test
    public void test_1() {
        Console.log(
                "ObservationOrchestratorIT",
                "component injected");

        Assertions.assertNotNull(observationOrchestrator);
    }
}
