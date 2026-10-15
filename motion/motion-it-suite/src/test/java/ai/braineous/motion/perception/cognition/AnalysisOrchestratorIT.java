package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemAnalysis;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@QuarkusTest
public class AnalysisOrchestratorIT {

    private static final String DB_NAME = "motion";
    private static final String QUERY_COLLECTION_NAME = "analysis_query_store";
    private static final String ANALYSIS_COLLECTION_NAME = "analysis_store";

    private static final String Q1 = "ao_it_q1_fetch_options";
    private static final String Q2 = "ao_it_q2_rank_and_pick";
    private static final String Q3 = "ao_it_q3_customer_message";

    private static final String PAY_1001 = "PaymentRequest:PAY-1001";
    private static final String CUST_2001 = "CustomerAccount:CUST-2001";
    private static final String PM_3001 = "PaymentMethod:PM-3001";
    private static final String RISK_4001 = "RiskProfile:RISK-4001";
    private static final String POL_5001 = "MerchantPolicy:POL-5001";

    private static final String PAY_1001_TEXT =
            "{\"id\":\"PaymentRequest:PAY-1001\",\"kind\":\"PaymentRequest\",\"mode\":\"atomic\",\"amount\":\"125.00\",\"currency\":\"USD\"}";
    private static final String CUST_2001_TEXT =
            "{\"id\":\"CustomerAccount:CUST-2001\",\"kind\":\"CustomerAccount\",\"mode\":\"atomic\",\"status\":\"ACTIVE\"}";
    private static final String PM_3001_TEXT =
            "{\"id\":\"PaymentMethod:PM-3001\",\"kind\":\"PaymentMethod\",\"mode\":\"atomic\",\"type\":\"CARD\"}";
    private static final String RISK_4001_TEXT =
            "{\"id\":\"RiskProfile:RISK-4001\",\"kind\":\"RiskProfile\",\"mode\":\"atomic\",\"level\":\"LOW\"}";
    private static final String POL_5001_TEXT =
            "{\"id\":\"MerchantPolicy:POL-5001\",\"kind\":\"MerchantPolicy\",\"mode\":\"atomic\",\"capture\":\"AUTO\"}";

    private static final String PAY_V1_SQL =
            "select decision, reason, code "
                    + "from llm "
                    + "where factId = '" + PAY_1001 + "' "
                    + "and relatedFactIds = '"
                    + CUST_2001 + ","
                    + PM_3001 + ","
                    + RISK_4001 + ","
                    + POL_5001 + "' "
                    + "control intent = 'decide_payment_capture', "
                    + "action = 'determine', "
                    + "subject = 'primary_payment_request', "
                    + "decision = 'allow_capture', "
                    + "basis = 'related_system_facts', "
                    + "goal = 'decision'";

    @Inject
    AnalysisOrchestrator analysisOrchestrator;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Inject
    AnalysisStore analysisStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        MongoCollection<Document> queryCollection = queries();
        MongoCollection<Document> analysisCollection = analyses();

        queryCollection.deleteMany(eq("name", Q1));
        queryCollection.deleteMany(eq("name", Q2));
        queryCollection.deleteMany(eq("name", Q3));

        PerceptionPipelineTestHarness harness =
                new PerceptionPipelineTestHarness(this.mongoClient);
        harness.resetGraphState();

        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        graphBuilder.addNode(payFact(PAY_1001, PAY_1001_TEXT));
        graphBuilder.addNode(payFact(CUST_2001, CUST_2001_TEXT));
        graphBuilder.addNode(payFact(PM_3001, PM_3001_TEXT));
        graphBuilder.addNode(payFact(RISK_4001, RISK_4001_TEXT));
        graphBuilder.addNode(payFact(POL_5001, POL_5001_TEXT));

        String persistedAnalysisId = null;
        try {
            analysisQueryStore.put(Q1, PAY_V1_SQL);
            analysisQueryStore.put(Q2, PAY_V1_SQL);
            analysisQueryStore.put(Q3, PAY_V1_SQL);

            Assertions.assertEquals(PAY_V1_SQL, analysisQueryStore.get(Q1));
            Assertions.assertEquals(PAY_V1_SQL, analysisQueryStore.get(Q2));
            Assertions.assertEquals(PAY_V1_SQL, analysisQueryStore.get(Q3));

            Perception perception = payPerception();
            List<String> queryNames = new ArrayList<String>();
            queryNames.add(Q1);
            queryNames.add(Q2);
            queryNames.add(Q3);

            long analysisCountBefore = analysisCollection.countDocuments();

            SystemAnalysis analysis =
                    this.analysisOrchestrator.analyze(perception, queryNames);

            Console.log("AnalysisOrchestratorIT.test_1", String.valueOf(analysis));

            Assertions.assertNotNull(analysis);
            Assertions.assertSame(perception, analysis.getPerception());
            Assertions.assertNotNull(analysis.getAnalysisId());
            Assertions.assertFalse(analysis.getAnalysisId().trim().isEmpty());
            persistedAnalysisId = analysis.getAnalysisId();

            String cognitiveResultJson = analysis.getCognitiveResultJson();
            Assertions.assertNotNull(cognitiveResultJson);
            Assertions.assertFalse(cognitiveResultJson.trim().isEmpty());

            Assertions.assertTrue(cognitiveResultJson.contains(Q1));
            Assertions.assertTrue(cognitiveResultJson.contains(Q2));
            Assertions.assertTrue(cognitiveResultJson.contains(Q3));
            Assertions.assertTrue(
                    cognitiveResultJson.indexOf(Q1)
                            < cognitiveResultJson.indexOf(Q2));
            Assertions.assertTrue(
                    cognitiveResultJson.indexOf(Q2)
                            < cognitiveResultJson.indexOf(Q3));
            Assertions.assertTrue(cognitiveResultJson.contains("queryExecutionJson"));
            Assertions.assertTrue(cognitiveResultJson.contains("requestJson"));
            Assertions.assertTrue(cognitiveResultJson.contains("approved"));

            Console.log(
                    "it.cognitiveResultJson",
                    cognitiveResultJson);
            Console.log(
                    "it.analysisId",
                    String.valueOf(analysis.getAnalysisId()));

            long analysisCountAfter = analysisCollection.countDocuments();
            long persistedCount = analysisCollection.countDocuments(
                    eq("analysisId", persistedAnalysisId));
            Console.log(
                    "it.analysis_store.count.before",
                    String.valueOf(analysisCountBefore));
            Console.log(
                    "it.analysis_store.count.after",
                    String.valueOf(analysisCountAfter));
            Console.log(
                    "it.analysis_store.count.byId",
                    String.valueOf(persistedCount));

            Assertions.assertEquals(analysisCountBefore + 1, analysisCountAfter);
            Assertions.assertEquals(1, persistedCount);

            SystemAnalysis stored = analysisStore.get(persistedAnalysisId);
            Assertions.assertNotNull(stored);
            Assertions.assertEquals(persistedAnalysisId, stored.getAnalysisId());
            Assertions.assertEquals(
                    cognitiveResultJson,
                    stored.getCognitiveResultJson());
            Console.log("it.analysis_store.get", stored.toJson());
        } finally {
            queryCollection.deleteMany(eq("name", Q1));
            queryCollection.deleteMany(eq("name", Q2));
            queryCollection.deleteMany(eq("name", Q3));
            if (persistedAnalysisId != null) {
                analysisStore.remove(persistedAnalysisId);
            }
            harness.resetGraphState();
        }
    }

    private Perception payPerception() {
        Fact pay = new Fact(PAY_1001, PAY_1001_TEXT);
        Fact customer = new Fact(CUST_2001, CUST_2001_TEXT);
        Fact method = new Fact(PM_3001, PM_3001_TEXT);
        Fact risk = new Fact(RISK_4001, RISK_4001_TEXT);
        Fact policy = new Fact(POL_5001, POL_5001_TEXT);

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
        operationalView.setViewId("ov-ao-it-pay-v1");
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
        return perception;
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

    private Fact payFact(String id, String text) {
        return new Fact(id, text, new HashSet<String>(), "atomic");
    }

    private MongoCollection<Document> queries() {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(QUERY_COLLECTION_NAME);
    }

    private MongoCollection<Document> analyses() {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(ANALYSIS_COLLECTION_NAME);
    }
}
