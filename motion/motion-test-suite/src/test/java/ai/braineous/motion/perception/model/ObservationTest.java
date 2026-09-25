package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class ObservationTest {

    @Test
    public void test_1() {
        Observation observation = new Observation();

        Assertions.assertNull(observation.getOperationalView());
        Assertions.assertNull(observation.getReasoningView());
    }

    @Test
    public void test_2() {
        Observation observation = new Observation();
        OperationalView operationalView = new OperationalView();

        observation.setOperationalView(operationalView);

        Assertions.assertSame(
                operationalView,
                observation.getOperationalView());
    }

    @Test
    public void test_3() {
        Observation observation = new Observation();
        GraphSnapshot reasoningView = newGraphSnapshot();

        observation.setReasoningView(reasoningView);

        Assertions.assertSame(
                reasoningView,
                observation.getReasoningView());
    }

    @Test
    public void test_4() {
        Observation observation = new Observation();
        OperationalView operationalView = new OperationalView();
        GraphSnapshot reasoningView = newGraphSnapshot();

        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        Assertions.assertSame(
                operationalView,
                observation.getOperationalView());
        Assertions.assertSame(
                reasoningView,
                observation.getReasoningView());
    }

    @Test
    public void test_5() {
        Observation observation = new Observation();
        OperationalView operationalView = new OperationalView();
        operationalView.setViewId("operational-view-1");
        GraphSnapshot reasoningView = newGraphSnapshot();

        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        Assertions.assertTrue(
                observation.toString().contains(
                        "operationalView=" + operationalView));
        Assertions.assertTrue(
                observation.toString().contains(
                        "reasoningView=" + reasoningView));
    }

    private GraphSnapshot newGraphSnapshot() {
        return new GraphSnapshot(
                new HashMap<String, Fact>(),
                new HashMap<String, Edge>());
    }
}
