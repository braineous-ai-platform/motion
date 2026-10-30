package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.braineous.dd.llm.pg.model.TxStepResult;
import io.braineous.dd.llm.query.client.QueryResult;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
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
public class CognitiveQueryExecutorIT {

    private static final String DB_NAME = "motion";
    private static final String COLLECTION_NAME = "analysis_query_store";

    private static final String Q1 = "cqe_it_q1_fetch_options";
    private static final String Q2 = "cqe_it_q2_rank_and_pick";
    private static final String Q3 = "cqe_it_q3_customer_message";

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
                    + "control "
                    + "method = 'decide_payment_capture', "
                    + "parameters = 'CustomerAccount.status, PaymentMethod.type, RiskProfile.level, MerchantPolicy.capture', "
                    + "expression = 'CustomerAccount.status is equal to ACTIVE and PaymentMethod.type is equal to CARD and RiskProfile.level is equal to LOW and MerchantPolicy.capture is equal to AUTO', "
                    + "spec = 'When the expression is true, the result is allow_capture. Otherwise, the result is deny_capture.', "
                    + "result = ''";

    @Inject
    CognitiveQueryExecutor cognitiveQueryExecutor;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        MongoCollection<Document> collection = queries();
        collection.deleteMany(eq("name", Q1));
        collection.deleteMany(eq("name", Q2));
        collection.deleteMany(eq("name", Q3));

        PerceptionPipelineTestHarness harness =
                new PerceptionPipelineTestHarness(this.mongoClient);
        harness.resetGraphState();

        GraphBuilder graphBuilder = GraphBuilder.getInstance();
        graphBuilder.addNode(payFact(PAY_1001, PAY_1001_TEXT));
        graphBuilder.addNode(payFact(CUST_2001, CUST_2001_TEXT));
        graphBuilder.addNode(payFact(PM_3001, PM_3001_TEXT));
        graphBuilder.addNode(payFact(RISK_4001, RISK_4001_TEXT));
        graphBuilder.addNode(payFact(POL_5001, POL_5001_TEXT));

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

            TxExecutionResult txResult =
                    this.cognitiveQueryExecutor.analyze(perception, queryNames);

            Console.log("CognitiveQueryExecutorIT.test_1", String.valueOf(txResult));

            Assertions.assertNotNull(txResult);
            Assertions.assertNotNull(txResult.getStepResults());
            Assertions.assertEquals(3, txResult.getStepResults().size());

            TxStepResult s1 = txResult.getStepResults().get(0);
            TxStepResult s2 = txResult.getStepResults().get(1);
            TxStepResult s3 = txResult.getStepResults().get(2);

            Assertions.assertEquals(Q1, s1.getId());
            Assertions.assertEquals(Q2, s2.getId());
            Assertions.assertEquals(Q3, s3.getId());

            assertAuthoritativeStep(s1);
            assertAuthoritativeStep(s2);
            assertAuthoritativeStep(s3);

            Assertions.assertNotNull(txResult.getGateResult());
            Assertions.assertTrue(txResult.isApproved());
            Console.log("it.tx.gate", String.valueOf(txResult.getGateResult()));
        } finally {
            collection.deleteMany(eq("name", Q1));
            collection.deleteMany(eq("name", Q2));
            collection.deleteMany(eq("name", Q3));
            harness.resetGraphState();
        }
    }

    private void assertAuthoritativeStep(TxStepResult step) {
        Assertions.assertNotNull(step);
        Assertions.assertTrue(step.isOk());

        QueryResult queryResult = step.getQueryResult();
        Assertions.assertNotNull(queryResult);
        Assertions.assertTrue(queryResult.isOk());
        Assertions.assertNotNull(queryResult.getQueryExecutionJson());
        Assertions.assertNotNull(queryResult.getRequestJson());

        String payload = queryResult.toJson().toString();
        Assertions.assertFalse(payload.trim().isEmpty());
        Assertions.assertTrue(payload.contains("queryExecutionJson"));
        Assertions.assertTrue(payload.contains("requestJson"));
        Console.log("it.step.payload", "id=" + step.getId() + " payload=" + payload);
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
        operationalView.setViewId("ov-cqe-it-pay-v1");
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
        return database.getCollection(COLLECTION_NAME);
    }
}
