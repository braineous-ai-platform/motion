package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemAnalysis;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@QuarkusTest
public class AnalysisStoreIT {

    private static final String DB_NAME = "motion";
    private static final String COLLECTION_NAME = "analysis_store";
    private static final String ANALYSIS_ID = "analysis-store-it-pay-v1";
    private static final String CREATED_AT = "2026-10-12T16:15:00Z";
    private static final String CREATED_AT_REPLACEMENT = "2026-10-12T16:16:00Z";

    private static final String PAY_V1_SQL =
            "select decision, reason, code "
                    + "from llm "
                    + "where factId = 'PaymentRequest:PAY-1001' "
                    + "and relatedFactIds = 'CustomerAccount:CUST-2001,"
                    + "PaymentMethod:PM-3001,"
                    + "RiskProfile:RISK-4001,"
                    + "MerchantPolicy:POL-5001' "
                    + "control intent = 'decide_payment_capture', "
                    + "action = 'determine', "
                    + "subject = 'primary_payment_request', "
                    + "decision = 'allow_capture', "
                    + "basis = 'related_system_facts', "
                    + "goal = 'decision'";

    private static final String PAY_V1_SQL_REPLACEMENT =
            "select replaced_decision from llm where factId = 'PaymentRequest:PAY-1001'";

    @Inject
    AnalysisStore analysisStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        MongoCollection<Document> collection = analyses();
        collection.deleteMany(eq("analysisId", ANALYSIS_ID));

        try {
            Console.log("analysis_store.db", DB_NAME);
            Console.log("analysis_store.collection", COLLECTION_NAME);

            SystemAnalysis original = newAnalysis(
                    CREATED_AT,
                    PAY_V1_SQL,
                    "ov-analysis-store-it-1");

            analysisStore.put(original);

            SystemAnalysis stored = analysisStore.get(ANALYSIS_ID);
            long countAfterPut = collection.countDocuments(eq("analysisId", ANALYSIS_ID));
            Console.log("put.original", stored.toJson());
            Console.log("count.afterPut", String.valueOf(countAfterPut));

            Assertions.assertEquals(1L, countAfterPut);
            assertCanonicalRoundTrip(
                    original,
                    stored);

            SystemAnalysis replacement = newAnalysis(
                    CREATED_AT_REPLACEMENT,
                    PAY_V1_SQL_REPLACEMENT,
                    "ov-analysis-store-it-2");

            analysisStore.put(replacement);

            SystemAnalysis replaced = analysisStore.get(ANALYSIS_ID);
            long countAfterReplace = collection.countDocuments(eq("analysisId", ANALYSIS_ID));
            Console.log("put.replacement", replaced.toJson());
            Console.log("count.afterReplace", String.valueOf(countAfterReplace));

            Assertions.assertEquals(1L, countAfterReplace);
            assertCanonicalRoundTrip(
                    replacement,
                    replaced);

            analysisStore.remove(ANALYSIS_ID);

            SystemAnalysis afterRemove = analysisStore.get(ANALYSIS_ID);
            long countAfterRemove = collection.countDocuments(eq("analysisId", ANALYSIS_ID));
            Console.log("get.afterRemove", String.valueOf(afterRemove));
            Console.log("count.afterRemove", String.valueOf(countAfterRemove));

            Assertions.assertNull(afterRemove);
            Assertions.assertEquals(0L, countAfterRemove);
        } finally {
            collection.deleteMany(eq("analysisId", ANALYSIS_ID));
        }
    }

    private SystemAnalysis newAnalysis(
            String createdAt,
            String cognitiveQuery,
            String viewId) {
        Fact pay = new Fact(
                "PaymentRequest:PAY-1001",
                "{\"id\":\"PaymentRequest:PAY-1001\"}");
        Fact customer = new Fact(
                "CustomerAccount:CUST-2001",
                "{\"id\":\"CustomerAccount:CUST-2001\"}");
        Fact method = new Fact(
                "PaymentMethod:PM-3001",
                "{\"id\":\"PaymentMethod:PM-3001\"}");
        Fact risk = new Fact(
                "RiskProfile:RISK-4001",
                "{\"id\":\"RiskProfile:RISK-4001\"}");
        Fact policy = new Fact(
                "MerchantPolicy:POL-5001",
                "{\"id\":\"MerchantPolicy:POL-5001\"}");

        List<Fact> facts = new ArrayList<Fact>();
        facts.add(pay);
        facts.add(customer);
        facts.add(method);
        facts.add(risk);
        facts.add(policy);

        Observable observable = new Observable();
        observable.setObservableFactAnchor(pay);
        observable.setObservableFacts(facts);

        OperationalView operationalView = new OperationalView();
        operationalView.setViewId(viewId);
        operationalView.setObservedView(facts);

        Map<String, Fact> nodes = new HashMap<String, Fact>();
        nodes.put(pay.getId(), pay);
        nodes.put(customer.getId(), customer);
        nodes.put(method.getId(), method);
        nodes.put(risk.getId(), risk);
        nodes.put(policy.getId(), policy);

        Map<String, Edge> edges = new HashMap<String, Edge>();
        putDirectedEdge(edges, pay, customer, "E:PAY-1001->CUST-2001");
        putDirectedEdge(edges, pay, method, "E:PAY-1001->PM-3001");
        putDirectedEdge(edges, pay, risk, "E:PAY-1001->RISK-4001");
        putDirectedEdge(edges, pay, policy, "E:PAY-1001->POL-5001");

        GraphSnapshot reasoningView = new GraphSnapshot(nodes, edges);

        Observation observation = new Observation();
        observation.setObservable(observable);
        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        Perception perception = new Perception();
        perception.setObservation(observation);

        SystemAnalysis analysis = new SystemAnalysis();
        analysis.setAnalysisId(ANALYSIS_ID);
        analysis.setCreatedAt(createdAt);
        analysis.setCognitiveQuery(cognitiveQuery);
        analysis.setPerception(perception);

        return analysis;
    }

    private void putDirectedEdge(
            Map<String, Edge> edges,
            Fact from,
            Fact to,
            String edgeId) {
        Edge edge = new Edge();
        edge.setId(edgeId);
        edge.setFromFactId(from.getId());
        edge.setToFactId(to.getId());
        edge.setMode("relational");
        edge.setScore(1.0);
        edges.put(edgeId, edge);
    }

    private void assertCanonicalRoundTrip(
            SystemAnalysis expected,
            SystemAnalysis actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getAnalysisId(), actual.getAnalysisId());
        Assertions.assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
        Assertions.assertEquals(expected.getCognitiveQuery(), actual.getCognitiveQuery());

        Perception expectedPerception = expected.getPerception();
        Perception actualPerception = actual.getPerception();
        Assertions.assertNotNull(actualPerception);

        Observation expectedObservation = expectedPerception.getObservation();
        Observation actualObservation = actualPerception.getObservation();
        Assertions.assertNotNull(actualObservation);

        Observable expectedObservable = expectedObservation.getObservable();
        Observable actualObservable = actualObservation.getObservable();
        Assertions.assertNotNull(actualObservable);
        Assertions.assertEquals(
                expectedObservable.getObservableFactAnchor().getId(),
                actualObservable.getObservableFactAnchor().getId());
        Assertions.assertEquals(
                expectedObservable.getObservableFacts().size(),
                actualObservable.getObservableFacts().size());

        int i = 0;
        while (i < expectedObservable.getObservableFacts().size()) {
            Fact expectedFact = expectedObservable.getObservableFacts().get(i);
            Fact actualFact = actualObservable.getObservableFacts().get(i);
            Assertions.assertEquals(expectedFact.getId(), actualFact.getId());
            Assertions.assertEquals(expectedFact.getText(), actualFact.getText());
            i = i + 1;
        }

        OperationalView expectedOv = expectedObservation.getOperationalView();
        OperationalView actualOv = actualObservation.getOperationalView();
        Assertions.assertNotNull(actualOv);
        Assertions.assertEquals(expectedOv.getViewId(), actualOv.getViewId());
        Assertions.assertEquals(
                expectedOv.getObservedView().size(),
                actualOv.getObservedView().size());

        int j = 0;
        while (j < expectedOv.getObservedView().size()) {
            Fact expectedFact = expectedOv.getObservedView().get(j);
            Fact actualFact = actualOv.getObservedView().get(j);
            Assertions.assertEquals(expectedFact.getId(), actualFact.getId());
            Assertions.assertEquals(expectedFact.getText(), actualFact.getText());
            j = j + 1;
        }

        GraphSnapshot expectedRv = expectedObservation.getReasoningView();
        GraphSnapshot actualRv = actualObservation.getReasoningView();
        Assertions.assertNotNull(actualRv);
        Assertions.assertEquals(expectedRv.nodes().size(), actualRv.nodes().size());
        Assertions.assertEquals(expectedRv.edges().size(), actualRv.edges().size());

        for (Map.Entry<String, Fact> entry : expectedRv.nodes().entrySet()) {
            Fact actualNode = actualRv.nodes().get(entry.getKey());
            Assertions.assertNotNull(actualNode);
            Assertions.assertEquals(entry.getValue().getId(), actualNode.getId());
            Assertions.assertEquals(entry.getValue().getText(), actualNode.getText());
        }

        for (Map.Entry<String, Edge> entry : expectedRv.edges().entrySet()) {
            Edge expectedEdge = entry.getValue();
            Edge actualEdge = actualRv.edges().get(entry.getKey());
            Assertions.assertNotNull(actualEdge);
            Assertions.assertEquals(expectedEdge.getId(), actualEdge.getId());
            Assertions.assertEquals(expectedEdge.getFromFactId(), actualEdge.getFromFactId());
            Assertions.assertEquals(expectedEdge.getToFactId(), actualEdge.getToFactId());
            Assertions.assertEquals(expectedEdge.getMode(), actualEdge.getMode());
        }
    }

    private MongoCollection<Document> analyses() {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(COLLECTION_NAME);
    }
}
