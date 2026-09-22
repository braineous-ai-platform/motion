package ai.braineous.motion.perception;

import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PerceptionOrchestratorIT {

    @Inject
    PerceptionOrchestrator perceptionOrchestrator;

    @Test
    public void test_1() {
        Console.log(
                "PerceptionOrchestratorIT",
                "component injected");

        Assertions.assertNotNull(perceptionOrchestrator);
    }
}
