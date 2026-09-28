package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.observation.ObservationOrchestrator;
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
public class CompareIT {

    @Inject
    MongoClient mongoClient;

    @Inject
    ObservationOrchestrator observationOrchestrator;

    private PerceptionPipelineTestHarness harness;

    @BeforeAll
    public void setUpSubstrate() {
        harness =
                new PerceptionPipelineTestHarness(
                        mongoClient);

        harness.seed();

        Console.log(
                "CompareIT",
                "substrate seeded F1-F6");
    }

    @Test
    public void test_1() {
        Observation observation =
                observe(
                        requested("F1", "F2", "F3", "F4", "F5", "F6"));

        assertNotNull(observation);
        assertNotNull(observation.getOperationalView());
        assertNotNull(observation.getReasoningView());

        Perception left =
                new Perception();
        left.setObservation(observation);

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

        assertTrue(
                setsEqual(
                        expectedIds,
                        copyNodeIds(reasoningView)));

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

        HistoricalView right =
                new HistoricalView();
        right.setWindow(window);
        right.setOperationalView(operationalView);
        right.setReasoningView(reasoningView);

        Compare compare =
                new Compare();
        compare.setLeft(left);
        compare.setRight(right);

        assertSame(left, compare.getLeft());
        assertSame(right, compare.getRight());
        assertSame(
                observation,
                compare.getLeft().getObservation());
        assertSame(
                window,
                compare.getRight().getWindow());
        assertSame(
                operationalView,
                compare.getRight().getOperationalView());
        assertSame(
                reasoningView,
                compare.getRight().getReasoningView());

        GraphSnapshot leftReasoningView =
                compare.getLeft().getObservation().getReasoningView();
        OperationalView leftOperationalView =
                compare.getLeft().getObservation().getOperationalView();
        GraphSnapshot rightReasoningView =
                compare.getRight().getReasoningView();
        OperationalView rightOperationalView =
                compare.getRight().getOperationalView();

        assertNotNull(leftReasoningView);
        assertNotNull(leftOperationalView);
        assertNotNull(leftOperationalView.getObservedView());

        GraphSnapshot persistedGraph =
                GraphBuilder.getInstance().snapshot();
        assertTrue(
                setsEqual(
                        copyEdgeIds(persistedGraph),
                        copyEdgeIds(leftReasoningView)));
        assertTrue(
                setsEqual(
                        copyEdgeIds(persistedGraph),
                        copyEdgeIds(rightReasoningView)));

        assertTrue(
                setsEqual(
                        expectedIds,
                        copyNodeIds(leftReasoningView)));
        assertTrue(
                setsEqual(
                        expectedIds,
                        copyFactIds(
                                leftOperationalView.getObservedView())));
        assertTrue(
                setsEqual(
                        expectedIds,
                        copyNodeIds(rightReasoningView)));
        assertTrue(
                setsEqual(
                        expectedIds,
                        copyFactIds(
                                rightOperationalView.getObservedView())));

        assertExactFactIdentity(
                leftOperationalView.getObservedView(),
                leftReasoningView);
        assertExactFactIdentity(
                rightOperationalView.getObservedView(),
                rightReasoningView);

        Console.log(
                "CompareIT",
                "left RV="
                        + join(copyNodeIds(leftReasoningView))
                        + " left OV="
                        + join(copyFactIds(
                                leftOperationalView.getObservedView()))
                        + " right RV="
                        + join(copyNodeIds(rightReasoningView))
                        + " right OV="
                        + join(copyFactIds(
                                rightOperationalView.getObservedView())));
    }

    private Observation observe(
            List<String> requestedKinds) {
        Observable observable =
                new Observable();

        List<Fact> facts =
                new ArrayList<Fact>();

        int index = 0;
        while (index < requestedKinds.size()) {
            facts.add(
                    new Fact(
                            requestedKinds.get(index),
                            ""));
            index = index + 1;
        }

        observable.setObservableFacts(facts);

        return observationOrchestrator.observe(
                observable);
    }

    private List<String> requested(
            String... kinds) {
        List<String> requestedKinds =
                new ArrayList<String>();
        int index = 0;
        while (index < kinds.length) {
            requestedKinds.add(kinds[index]);
            index = index + 1;
        }
        return requestedKinds;
    }

    private void assertExactFactIdentity(
            List<Fact> observedView,
            GraphSnapshot reasoningView) {
        int ovIndex = 0;
        while (ovIndex < observedView.size()) {
            Fact ovFact =
                    observedView.get(ovIndex);
            Fact rvFact =
                    reasoningView.nodes().get(
                            ovFact.getId());
            assertNotNull(rvFact);
            assertSame(rvFact, ovFact);
            ovIndex = ovIndex + 1;
        }
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
