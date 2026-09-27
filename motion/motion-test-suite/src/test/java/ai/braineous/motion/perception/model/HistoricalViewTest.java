package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class HistoricalViewTest {

    @Test
    public void test_1() {
        HistoricalView historicalView = new HistoricalView();

        Assertions.assertNull(historicalView.getWindow());
        Assertions.assertNull(historicalView.getOperationalView());
        Assertions.assertNull(historicalView.getReasoningView());
    }

    @Test
    public void test_2() {
        HistoricalView historicalView = new HistoricalView();
        HistoricalViewWindow window = new HistoricalViewWindow();
        OperationalView operationalView = new OperationalView();
        GraphSnapshot reasoningView = newGraphSnapshot();

        historicalView.setWindow(window);
        historicalView.setOperationalView(operationalView);
        historicalView.setReasoningView(reasoningView);

        Assertions.assertSame(window, historicalView.getWindow());
        Assertions.assertSame(
                operationalView,
                historicalView.getOperationalView());
        Assertions.assertSame(
                reasoningView,
                historicalView.getReasoningView());
    }

    @Test
    public void test_3() {
        HistoricalView historicalView = new HistoricalView();
        HistoricalViewWindow window = new HistoricalViewWindow();
        OperationalView operationalView = new OperationalView();
        GraphSnapshot reasoningView = newGraphSnapshot();

        historicalView.setWindow(window);
        historicalView.setOperationalView(operationalView);
        historicalView.setReasoningView(reasoningView);

        String representation = historicalView.toString();
        Assertions.assertTrue(representation.contains("HistoricalView"));
        Assertions.assertTrue(representation.contains("window="));
        Assertions.assertTrue(representation.contains("operationalView="));
        Assertions.assertTrue(representation.contains("reasoningView="));
    }

    private GraphSnapshot newGraphSnapshot() {
        return new GraphSnapshot(
                new HashMap<String, Fact>(),
                new HashMap<String, Edge>());
    }
}
