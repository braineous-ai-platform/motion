package ai.braineous.motion.perception.cognition;

import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class CognitiveOperationOrchestratorIT {

    @Inject
    CognitiveOperationOrchestrator cognitiveOperationOrchestrator;

    @Test
    public void test_1() {
        Console.log(
                "CognitiveOperationOrchestratorIT",
                "component injected");

        Assertions.assertNotNull(cognitiveOperationOrchestrator);
    }
}
