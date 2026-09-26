package ai.braineous.motion.perception.observation;

import ai.braineous.motion.ingestion.eventprocessor.orchestrator.MotionIngestionOrchestrator;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.support.PerceptionPipelineTestHarness;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ObservationOrchestratorIT {

    @Inject
    MotionIngestionOrchestrator motionIngestionOrchestrator;

    @Inject
    ObservationOrchestrator observationOrchestrator;

    private PerceptionPipelineTestHarness harness;

    @BeforeEach
    public void setUp() {
        harness =
                new PerceptionPipelineTestHarness(
                        motionIngestionOrchestrator);

        harness.send();
    }

    @Test
    public void test_1() {
        Observable observable =
                new Observable();

        Fact requestedFactKind =
                new Fact("FLIGHT", "");

        List<Fact> requestedFacts =
                new ArrayList<Fact>();

        requestedFacts.add(
                requestedFactKind);

        observable.setFacts(
                requestedFacts);

        Console.log(
                "ObservationOrchestratorIT",
                "Observable=" + observable);

        Observation observation =
                observationOrchestrator.observe(
                        observable);

        Console.log(
                "ObservationOrchestratorIT",
                "Observation=" + observation);

        assertNotNull(observation);
        assertNotNull(observation.getReasoningView());
        assertNotNull(observation.getOperationalView());
    }
}
