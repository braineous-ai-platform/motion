package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        GraphView reasoningView = newGraphView();

        observation.setReasoningView(reasoningView);

        Assertions.assertSame(
                reasoningView,
                observation.getReasoningView());
    }

    @Test
    public void test_4() {
        Observation observation = new Observation();
        OperationalView operationalView = new OperationalView();
        GraphView reasoningView = newGraphView();

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
        GraphView reasoningView = newGraphView();

        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        Assertions.assertTrue(
                observation.toString().contains(
                        "operationalView=" + operationalView));
        Assertions.assertTrue(
                observation.toString().contains(
                        "reasoningView=" + reasoningView));
    }

    private GraphView newGraphView() {
        return new GraphView() {
            @Override
            public Fact getFactById(String id) {
                return null;
            }

            @Override
            public String toString() {
                return "TestGraphView";
            }
        };
    }
}
