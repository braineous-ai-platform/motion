package ai.braineous.motion.perception.observation;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
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
public class ObservationOrchestratorIT {

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
                "ObservationOrchestratorIT",
                "substrate seeded F1-F6");
    }

    @Test
    public void test_1() {
        GraphSnapshot before =
                GraphBuilder.getInstance().snapshot();
        Set<String> beforeIds =
                copyNodeIds(before);

        Console.log(
                "ObservationOrchestratorIT",
                "persisted node count BEFORE battery="
                        + before.nodes().size());

        verifySingleKindCoverage();
        verifyRepresentativeSubsets();
        verifyExhaustiveSubsets();
        verifyMissingKindCoverage();
        verifyRequestOrderIndependence();
        verifyDuplicateRequests();
        verifyRepeatedObservationStability();

        GraphSnapshot after =
                GraphBuilder.getInstance().snapshot();
        Set<String> afterIds =
                copyNodeIds(after);

        Console.log(
                "ObservationOrchestratorIT",
                "persisted node count AFTER battery="
                        + after.nodes().size());

        assertEquals(
                before.nodes().size(),
                after.nodes().size());
        assertTrue(
                setsEqual(beforeIds, afterIds));

        Console.log(
                "ObservationOrchestratorIT",
                "Observation invariant battery complete");
    }

    private void verifySingleKindCoverage() {
        assertExactSelection(
                requested("F1"),
                expected("F1:1"));
        assertExactSelection(
                requested("F2"),
                expected("F2:1"));
        assertExactSelection(
                requested("F3"),
                expected("F3:1"));
        assertExactSelection(
                requested("F4"),
                expected("F4:1"));
        assertExactSelection(
                requested("F5"),
                expected("F5:1"));
        assertExactSelection(
                requested("F6"),
                expected("F6:1"));
    }

    private void verifyRepresentativeSubsets() {
        assertExactSelection(
                requested("F1", "F2"),
                expected("F1:1", "F2:1"));
        assertExactSelection(
                requested("F1", "F6"),
                expected("F1:1", "F6:1"));
        assertExactSelection(
                requested("F2", "F4"),
                expected("F2:1", "F4:1"));
        assertExactSelection(
                requested("F1", "F3", "F5"),
                expected("F1:1", "F3:1", "F5:1"));
        assertExactSelection(
                requested("F2", "F4", "F6"),
                expected("F2:1", "F4:1", "F6:1"));
        assertExactSelection(
                requested("F1", "F2", "F4", "F6"),
                expected("F1:1", "F2:1", "F4:1", "F6:1"));
        assertExactSelection(
                requested("F1", "F2", "F3", "F4", "F5", "F6"),
                expected(
                        "F1:1",
                        "F2:1",
                        "F3:1",
                        "F4:1",
                        "F5:1",
                        "F6:1"));
    }

    private void verifyExhaustiveSubsets() {
        String[] universe =
                new String[6];
        universe[0] = "F1";
        universe[1] = "F2";
        universe[2] = "F3";
        universe[3] = "F4";
        universe[4] = "F5";
        universe[5] = "F6";

        int mask = 0;
        while (mask < 64) {
            List<String> subset =
                    new ArrayList<String>();
            List<String> expectedIds =
                    new ArrayList<String>();

            int bit = 0;
            while (bit < 6) {
                int flag = 1 << bit;
                if ((mask & flag) != 0) {
                    String kind =
                            universe[bit];
                    subset.add(kind);
                    expectedIds.add(kind + ":1");
                }
                bit = bit + 1;
            }

            Observation observation =
                    observe(subset);
            assertExactSelection(
                    subset,
                    expectedIds,
                    observation,
                    true);

            mask = mask + 1;
        }
    }

    private void verifyMissingKindCoverage() {
        assertExactSelection(
                requested("F7"),
                expected());
        assertExactSelection(
                requested("F8"),
                expected());
        assertExactSelection(
                requested("F9"),
                expected());
        assertExactSelection(
                requested("F1", "F7", "F4"),
                expected("F1:1", "F4:1"));
        assertExactSelection(
                requested("F7", "F2", "F8", "F6", "F9"),
                expected("F2:1", "F6:1"));
    }

    private void verifyRequestOrderIndependence() {
        Set<String> expectedIds =
                new HashSet<String>();
        expectedIds.add("F1:1");
        expectedIds.add("F3:1");
        expectedIds.add("F6:1");

        assertExactSelection(
                requested("F1", "F3", "F6"),
                expected("F1:1", "F3:1", "F6:1"));
        assertExactSelection(
                requested("F6", "F1", "F3"),
                expected("F1:1", "F3:1", "F6:1"));
        assertExactSelection(
                requested("F3", "F6", "F1"),
                expected("F1:1", "F3:1", "F6:1"));

        Observation first =
                observe(requested("F1", "F3", "F6"));
        Observation second =
                observe(requested("F6", "F1", "F3"));
        Observation third =
                observe(requested("F3", "F6", "F1"));

        assertTrue(
                setsEqual(
                        copyNodeIds(first.getReasoningView()),
                        expectedIds));
        assertTrue(
                setsEqual(
                        copyNodeIds(second.getReasoningView()),
                        expectedIds));
        assertTrue(
                setsEqual(
                        copyNodeIds(third.getReasoningView()),
                        expectedIds));
    }

    private void verifyDuplicateRequests() {
        assertExactSelection(
                requested("F1", "F1", "F1"),
                expected("F1:1"));
        assertExactSelection(
                requested("F1", "F2", "F1", "F2", "F1"),
                expected("F1:1", "F2:1"));
    }

    private void verifyRepeatedObservationStability() {
        int index = 0;
        while (index < 5) {
            assertExactSelection(
                    requested("F2", "F5"),
                    expected("F2:1", "F5:1"));
            index = index + 1;
        }
    }

    private void assertExactSelection(
            List<String> requestedKinds,
            List<String> expectedIds) {
        Observation observation =
                observe(requestedKinds);
        assertExactSelection(
                requestedKinds,
                expectedIds,
                observation,
                false);
    }

    private void assertExactSelection(
            List<String> requestedKinds,
            List<String> expectedIds,
            Observation observation,
            boolean exhaustiveLog) {

        assertNotNull(observation);

        GraphSnapshot reasoningView =
                observation.getReasoningView();
        OperationalView operationalView =
                observation.getOperationalView();

        assertNotNull(reasoningView);
        assertNotNull(operationalView);

        List<Fact> observedView =
                operationalView.getObservedView();

        assertNotNull(observedView);
        assertTrue(
                reasoningView.edges().isEmpty());

        Set<String> expectedSet =
                new HashSet<String>();
        int expectedIndex = 0;
        while (expectedIndex < expectedIds.size()) {
            expectedSet.add(
                    expectedIds.get(expectedIndex));
            expectedIndex = expectedIndex + 1;
        }

        Set<String> rvIds =
                copyNodeIds(reasoningView);
        Set<String> ovIds =
                copyFactIds(observedView);

        if (exhaustiveLog) {
            Console.log(
                    "ObservationOrchestratorIT",
                    "subset requested="
                            + join(requestedKinds)
                            + " rv="
                            + join(rvIds)
                            + " ov="
                            + join(ovIds));
        } else {
            Console.log(
                    "ObservationOrchestratorIT",
                    "requested kinds="
                            + join(requestedKinds)
                            + " RV node ids="
                            + join(rvIds)
                            + " OV Fact ids="
                            + join(ovIds));
        }

        assertEquals(
                expectedSet.size(),
                rvIds.size());
        assertEquals(
                expectedSet.size(),
                ovIds.size());
        assertEquals(
                expectedSet.size(),
                reasoningView.nodes().size());
        assertEquals(
                expectedSet.size(),
                observedView.size());
        assertTrue(
                setsEqual(expectedSet, rvIds));
        assertTrue(
                setsEqual(expectedSet, ovIds));
        assertTrue(
                setsEqual(rvIds, ovIds));

        List<Fact> reasoningFacts =
                new ArrayList<Fact>();
        reasoningFacts.addAll(
                reasoningView.nodes().values());

        int rvIndex = 0;
        while (rvIndex < reasoningFacts.size()) {
            Fact rvFact =
                    reasoningFacts.get(rvIndex);
            Fact ovFact =
                    findById(
                            observedView,
                            rvFact.getId());
            assertNotNull(ovFact);
            assertSame(rvFact, ovFact);
            rvIndex = rvIndex + 1;
        }

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

        observable.setFacts(facts);

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

    private List<String> expected(
            String... ids) {
        List<String> expectedIds =
                new ArrayList<String>();
        int index = 0;
        while (index < ids.length) {
            expectedIds.add(ids[index]);
            index = index + 1;
        }
        return expectedIds;
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

    private Fact findById(
            List<Fact> facts,
            String factId) {
        int index = 0;
        while (index < facts.size()) {
            Fact fact =
                    facts.get(index);
            if (factId.equals(fact.getId())) {
                return fact;
            }
            index = index + 1;
        }
        return null;
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
            List<String> values) {
        String result = "";
        int index = 0;
        while (index < values.size()) {
            if (index > 0) {
                result = result + ",";
            }
            result = result + values.get(index);
            index = index + 1;
        }
        return result;
    }

    private String join(
            Set<String> values) {
        List<String> list =
                new ArrayList<String>();
        list.addAll(values);
        return join(list);
    }
}
