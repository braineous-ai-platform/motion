package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HistoricalViewIT {

    @Inject
    MongoClient mongoClient;

    private PerceptionPipelineTestHarness harness;

    @BeforeAll
    public void setUpSubstrate() {
        harness =
                new PerceptionPipelineTestHarness(
                        mongoClient);

        harness.seed();

        Console.log(
                "HistoricalViewIT",
                "substrate seeded F1-F6");
    }

    @Test
    public void test_1() {
        HistoricalViewWindow window =
                new HistoricalViewWindow();
        window.setWindowSeconds(3600L);
        Instant anchor =
                window.calculateWindow();

        assertNotNull(anchor);
        assertEquals(
                anchor.toString(),
                window.getTo());
        assertEquals(
                anchor.minusSeconds(3600L).toString(),
                window.getFrom());

        GraphSnapshot reasoningView =
                GraphBuilder.getInstance().snapshot();

        assertNotNull(reasoningView);
        assertEquals(6, reasoningView.nodes().size());

        Set<String> expectedIds =
                new HashSet<String>();
        expectedIds.add("F1:1");
        expectedIds.add("F2:1");
        expectedIds.add("F3:1");
        expectedIds.add("F4:1");
        expectedIds.add("F5:1");
        expectedIds.add("F6:1");

        Set<String> rvIds =
                copyNodeIds(reasoningView);
        assertTrue(
                setsEqual(expectedIds, rvIds));

        List<Fact> observedView =
                new ArrayList<Fact>();
        List<String> nodeKeys =
                new ArrayList<String>();
        nodeKeys.addAll(
                reasoningView.nodes().keySet());

        int index = 0;
        while (index < nodeKeys.size()) {
            Fact seededFact =
                    reasoningView.nodes().get(
                            nodeKeys.get(index));
            assertNotNull(seededFact);
            observedView.add(seededFact);
            index = index + 1;
        }

        OperationalView operationalView =
                new OperationalView();
        operationalView.setObservedView(observedView);

        HistoricalView historicalView =
                new HistoricalView();
        historicalView.setWindow(window);
        historicalView.setOperationalView(operationalView);
        historicalView.setReasoningView(reasoningView);

        assertSame(
                window,
                historicalView.getWindow());
        assertSame(
                operationalView,
                historicalView.getOperationalView());
        assertSame(
                reasoningView,
                historicalView.getReasoningView());

        List<Fact> historicalObservedView =
                historicalView.getOperationalView().getObservedView();
        GraphSnapshot historicalReasoningView =
                historicalView.getReasoningView();

        assertNotNull(historicalObservedView);
        assertEquals(6, historicalObservedView.size());
        assertEquals(
                6,
                historicalReasoningView.nodes().size());
        assertTrue(
                setsEqual(
                        copyEdgeIds(reasoningView),
                        copyEdgeIds(historicalReasoningView)));

        Set<String> ovIds =
                copyFactIds(historicalObservedView);
        assertTrue(
                setsEqual(expectedIds, ovIds));
        assertTrue(
                setsEqual(
                        copyNodeIds(historicalReasoningView),
                        ovIds));

        int ovIndex = 0;
        while (ovIndex < historicalObservedView.size()) {
            Fact ovFact =
                    historicalObservedView.get(ovIndex);
            Fact rvFact =
                    historicalReasoningView.nodes().get(
                            ovFact.getId());
            assertNotNull(rvFact);
            assertSame(rvFact, ovFact);
            ovIndex = ovIndex + 1;
        }

        Console.log(
                "HistoricalViewIT",
                "window from="
                        + historicalView.getWindow().getFrom()
                        + " to="
                        + historicalView.getWindow().getTo()
                        + " RV node ids="
                        + join(copyNodeIds(historicalReasoningView))
                        + " OV Fact ids="
                        + join(ovIds));
    }

    private Set<String> copyNodeIds(
            GraphSnapshot snapshot) {
        Set<String> ids =
                new HashSet<String>();
        List<String> keys =
                new ArrayList<String>();
        keys.addAll(snapshot.nodes().keySet());
        int index = 0;
        while (index < keys.size()) {
            ids.add(keys.get(index));
            index = index + 1;
        }
        return ids;
    }

    private Set<String> copyEdgeIds(
            GraphSnapshot snapshot) {
        Set<String> ids =
                new HashSet<String>();
        List<String> keys =
                new ArrayList<String>();
        keys.addAll(snapshot.edges().keySet());
        int index = 0;
        while (index < keys.size()) {
            ids.add(keys.get(index));
            index = index + 1;
        }
        return ids;
    }

    private Set<String> copyFactIds(
            List<Fact> facts) {
        Set<String> ids =
                new HashSet<String>();
        int index = 0;
        while (index < facts.size()) {
            ids.add(facts.get(index).getId());
            index = index + 1;
        }
        return ids;
    }

    private boolean setsEqual(
            Set<String> left,
            Set<String> right) {
        if (left.size() != right.size()) {
            return false;
        }

        List<String> leftValues =
                new ArrayList<String>();
        leftValues.addAll(left);

        int index = 0;
        while (index < leftValues.size()) {
            if (right.contains(leftValues.get(index)) == false) {
                return false;
            }
            index = index + 1;
        }

        List<String> rightValues =
                new ArrayList<String>();
        rightValues.addAll(right);

        index = 0;
        while (index < rightValues.size()) {
            if (left.contains(rightValues.get(index)) == false) {
                return false;
            }
            index = index + 1;
        }

        return true;
    }

    private String join(
            Set<String> values) {
        List<String> list =
                new ArrayList<String>();
        list.addAll(values);
        String result = "";
        int index = 0;
        while (index < list.size()) {
            if (index > 0) {
                result = result + ",";
            }
            result = result + list.get(index);
            index = index + 1;
        }
        return result;
    }
}
