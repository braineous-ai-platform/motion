package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.ingestion.sinkprocessor.PerceptionPipelineTestHarness;
import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemAnalysis;
import ai.braineous.motion.perception.model.SystemMutation;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
import static io.restassured.RestAssured.given;

@QuarkusTest
public class DevxIT {

    private static final String HTTP_BASE = "http://localhost:8084";
    private static final String DEVX_PATH = "/devx";

    private static final String DB_NAME = "motion";
    private static final String ANALYSIS_GROUP_COLLECTION = "analysis_group_store";
    private static final String MUTATION_GROUP_COLLECTION = "mutation_group_store";
    private static final String ANALYSIS_QUERY_COLLECTION = "analysis_query_store";
    private static final String MUTATION_QUERY_COLLECTION = "mutation_query_store";
    private static final String ANALYSIS_COLLECTION = "analysis_store";
    private static final String MUTATION_COLLECTION = "mutation_store";

    private static final String ANALYSIS_GROUP = "devx-it-payment_analysis";
    private static final String MUTATION_GROUP = "devx-it-payment_mutation";

    private static final String AQ1 = "devx_it_aq1_fetch_options";
    private static final String AQ2 = "devx_it_aq2_rank_and_pick";
    private static final String AQ3 = "devx_it_aq3_customer_message";

    private static final String MQ1 = "devx_it_mq1_fetch_options";
    private static final String MQ2 = "devx_it_mq2_rank_and_pick";
    private static final String MQ3 = "devx_it_mq3_customer_message";

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
    AnalysisGroupStore analysisGroupStore;

    @Inject
    MutationGroupStore mutationGroupStore;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Inject
    MutationQueryStore mutationQueryStore;

    @Inject
    AnalysisOrchestrator analysisOrchestrator;

    @Inject
    MutationOrchestrator mutationOrchestrator;

    @Inject
    AnalysisStore analysisStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        deleteFixtures();

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
            CognitiveTransactionGroup analysisGroup = group(
                    ANALYSIS_GROUP,
                    artifact(AQ1, PAY_V1_SQL),
                    artifact(AQ2, PAY_V1_SQL),
                    artifact(AQ3, PAY_V1_SQL));
            CognitiveTransactionGroup mutationGroup = group(
                    MUTATION_GROUP,
                    artifact(MQ1, PAY_V1_SQL),
                    artifact(MQ2, PAY_V1_SQL),
                    artifact(MQ3, PAY_V1_SQL));

            List<CognitiveTransactionGroup> analysisSection =
                    new ArrayList<CognitiveTransactionGroup>();
            analysisSection.add(analysisGroup);
            List<CognitiveTransactionGroup> mutationSection =
                    new ArrayList<CognitiveTransactionGroup>();
            mutationSection.add(mutationGroup);

            DevxDefinition definition = new DevxDefinition();
            definition.setAnalysis(analysisSection);
            definition.setMutation(mutationSection);

            post(definition);

            CognitiveTransactionGroup storedAnalysis =
                    analysisGroupStore.get(ANALYSIS_GROUP);
            CognitiveTransactionGroup storedMutation =
                    mutationGroupStore.get(MUTATION_GROUP);
            Console.log("analysis.group", storedAnalysis.toJson());
            Console.log("mutation.group", storedMutation.toJson());

            Assertions.assertEquals(ANALYSIS_GROUP, storedAnalysis.getName());
            Assertions.assertEquals(MUTATION_GROUP, storedMutation.getName());
            Assertions.assertEquals(analysisGroup.toJson(), storedAnalysis.toJson());
            Assertions.assertEquals(mutationGroup.toJson(), storedMutation.toJson());
            Assertions.assertEquals(1L, count(ANALYSIS_GROUP_COLLECTION, ANALYSIS_GROUP));
            Assertions.assertEquals(1L, count(MUTATION_GROUP_COLLECTION, MUTATION_GROUP));

            List<String> analysisQueryNames = queryNames(storedAnalysis);
            List<String> mutationQueryNames = queryNames(storedMutation);
            Console.log("analysis.query.names", String.valueOf(analysisQueryNames));
            Console.log("mutation.query.names", String.valueOf(mutationQueryNames));

            Assertions.assertEquals(3, analysisQueryNames.size());
            Assertions.assertEquals(AQ1, analysisQueryNames.get(0));
            Assertions.assertEquals(AQ2, analysisQueryNames.get(1));
            Assertions.assertEquals(AQ3, analysisQueryNames.get(2));
            Assertions.assertEquals(3, mutationQueryNames.size());
            Assertions.assertEquals(MQ1, mutationQueryNames.get(0));
            Assertions.assertEquals(MQ2, mutationQueryNames.get(1));
            Assertions.assertEquals(MQ3, mutationQueryNames.get(2));

            assertPublishedQuery(analysisQueryStore, AQ1);
            assertPublishedQuery(analysisQueryStore, AQ2);
            assertPublishedQuery(analysisQueryStore, AQ3);
            assertPublishedQuery(mutationQueryStore, MQ1);
            assertPublishedQuery(mutationQueryStore, MQ2);
            assertPublishedQuery(mutationQueryStore, MQ3);
            Assertions.assertNull(analysisQueryStore.get(MQ1));
            Assertions.assertNull(analysisQueryStore.get(MQ2));
            Assertions.assertNull(analysisQueryStore.get(MQ3));
            Assertions.assertNull(mutationQueryStore.get(AQ1));
            Assertions.assertNull(mutationQueryStore.get(AQ2));
            Assertions.assertNull(mutationQueryStore.get(AQ3));

            Perception perception = payPerception();
            Observation observation = perception.getObservation();
            long analysisCountBefore = collection(ANALYSIS_COLLECTION).countDocuments();
            long mutationCountBefore = collection(MUTATION_COLLECTION).countDocuments();

            SystemAnalysis analysis =
                    this.analysisOrchestrator.analyze(perception, analysisQueryNames);
            Console.log("DevxIT.analysis", String.valueOf(analysis));

            Assertions.assertNotNull(analysis);
            Assertions.assertSame(perception, analysis.getPerception());
            Assertions.assertSame(observation, perception.getObservation());
            Assertions.assertEquals(
                    PAY_1001,
                    perception.getObservation()
                            .getObservable()
                            .getObservableFactAnchor()
                            .getId());
            Assertions.assertNotNull(analysis.getAnalysisId());
            Assertions.assertFalse(analysis.getAnalysisId().trim().isEmpty());
            persistedAnalysisId = analysis.getAnalysisId();

            String analysisResultJson = analysis.getCognitiveResultJson();
            Assertions.assertNotNull(analysisResultJson);
            Assertions.assertFalse(analysisResultJson.trim().isEmpty());
            assertExecution(analysisResultJson, AQ1, AQ2, AQ3);
            Assertions.assertFalse(analysisResultJson.contains(MQ1));
            Assertions.assertFalse(analysisResultJson.contains(MQ2));
            Assertions.assertFalse(analysisResultJson.contains(MQ3));
            Console.log("analysis.cognitiveResultJson", analysisResultJson);

            long analysisCountAfter = collection(ANALYSIS_COLLECTION).countDocuments();
            long persistedCount = collection(ANALYSIS_COLLECTION).countDocuments(
                    eq("analysisId", persistedAnalysisId));
            Assertions.assertEquals(analysisCountBefore + 1, analysisCountAfter);
            Assertions.assertEquals(1L, persistedCount);
            SystemAnalysis storedSystemAnalysis = analysisStore.get(persistedAnalysisId);
            Assertions.assertNotNull(storedSystemAnalysis);
            Assertions.assertEquals(
                    analysisResultJson,
                    storedSystemAnalysis.getCognitiveResultJson());

            SystemMutation mutation =
                    this.mutationOrchestrator.analyze(perception, mutationQueryNames);
            Console.log("DevxIT.mutation", String.valueOf(mutation));

            Assertions.assertNotNull(mutation);
            Assertions.assertSame(perception, mutation.getPerception());
            Assertions.assertSame(observation, perception.getObservation());
            Assertions.assertEquals(
                    PAY_1001,
                    perception.getObservation()
                            .getObservable()
                            .getObservableFactAnchor()
                            .getId());
            Assertions.assertNotNull(mutation.getMutationId());
            Assertions.assertFalse(mutation.getMutationId().trim().isEmpty());

            String mutationResultJson = mutation.getCognitiveResultJson();
            Assertions.assertNotNull(mutationResultJson);
            Assertions.assertFalse(mutationResultJson.trim().isEmpty());
            assertExecution(mutationResultJson, MQ1, MQ2, MQ3);
            Assertions.assertFalse(mutationResultJson.contains(AQ1));
            Assertions.assertFalse(mutationResultJson.contains(AQ2));
            Assertions.assertFalse(mutationResultJson.contains(AQ3));
            Console.log("mutation.cognitiveResultJson", mutationResultJson);

            long analysisCountAfterMutation =
                    collection(ANALYSIS_COLLECTION).countDocuments();
            long mutationCountAfter = collection(MUTATION_COLLECTION).countDocuments();
            long mutationById = collection(MUTATION_COLLECTION).countDocuments(
                    eq("mutationId", mutation.getMutationId()));
            Assertions.assertEquals(analysisCountAfter, analysisCountAfterMutation);
            Assertions.assertEquals(mutationCountBefore, mutationCountAfter);
            Assertions.assertEquals(0L, mutationById);

            assertPublishedQuery(analysisQueryStore, AQ1);
            assertPublishedQuery(analysisQueryStore, AQ2);
            assertPublishedQuery(analysisQueryStore, AQ3);
            assertPublishedQuery(mutationQueryStore, MQ1);
            assertPublishedQuery(mutationQueryStore, MQ2);
            assertPublishedQuery(mutationQueryStore, MQ3);
            Console.log("analysis.query.after", analysisQueryStore.get(AQ1));
            Console.log("mutation.query.after", mutationQueryStore.get(MQ1));
        } finally {
            deleteFixtures();
            if (persistedAnalysisId != null) {
                analysisStore.remove(persistedAnalysisId);
            }
            harness.resetGraphState();
        }
    }

    private void post(DevxDefinition definition) {
        String json = definition.toJson();
        Console.log("http.request", json);
        Response http = given()
                .baseUri(HTTP_BASE)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post(DEVX_PATH);
        Console.log("http.status", String.valueOf(http.statusCode()));
        Console.log("http.body", http.getBody().asString());
        Assertions.assertEquals(200, http.statusCode());
    }

    private void assertPublishedQuery(AnalysisQueryStore store, String name) {
        Assertions.assertEquals(PAY_V1_SQL, store.get(name));
        Assertions.assertEquals(1L, count(ANALYSIS_QUERY_COLLECTION, name));
    }

    private void assertPublishedQuery(MutationQueryStore store, String name) {
        Assertions.assertEquals(PAY_V1_SQL, store.get(name));
        Assertions.assertEquals(1L, count(MUTATION_QUERY_COLLECTION, name));
    }

    private void assertExecution(
            String cognitiveResultJson,
            String first,
            String second,
            String third) {
        Assertions.assertTrue(cognitiveResultJson.contains(first));
        Assertions.assertTrue(cognitiveResultJson.contains(second));
        Assertions.assertTrue(cognitiveResultJson.contains(third));
        Assertions.assertTrue(
                cognitiveResultJson.indexOf(first)
                        < cognitiveResultJson.indexOf(second));
        Assertions.assertTrue(
                cognitiveResultJson.indexOf(second)
                        < cognitiveResultJson.indexOf(third));
        Assertions.assertTrue(cognitiveResultJson.contains("queryExecutionJson"));
        Assertions.assertTrue(cognitiveResultJson.contains("requestJson"));
        Assertions.assertTrue(cognitiveResultJson.contains("approved"));
    }

    private List<String> queryNames(CognitiveTransactionGroup group) {
        List<String> names = new ArrayList<String>();
        List<CognitiveQueryArtifact> queries = group.getQueries();
        int index = 0;
        while (index < queries.size()) {
            CognitiveQueryArtifact query = queries.get(index);
            Assertions.assertEquals(PAY_V1_SQL, query.getCognitiveQuery());
            names.add(query.getName());
            index = index + 1;
        }
        return names;
    }

    private void deleteFixtures() {
        delete(ANALYSIS_GROUP_COLLECTION, ANALYSIS_GROUP);
        delete(MUTATION_GROUP_COLLECTION, MUTATION_GROUP);
        delete(ANALYSIS_QUERY_COLLECTION, AQ1);
        delete(ANALYSIS_QUERY_COLLECTION, AQ2);
        delete(ANALYSIS_QUERY_COLLECTION, AQ3);
        delete(MUTATION_QUERY_COLLECTION, MQ1);
        delete(MUTATION_QUERY_COLLECTION, MQ2);
        delete(MUTATION_QUERY_COLLECTION, MQ3);
    }

    private void delete(String collectionName, String name) {
        collection(collectionName).deleteMany(eq("name", name));
    }

    private long count(String collectionName, String name) {
        return collection(collectionName).countDocuments(eq("name", name));
    }

    private MongoCollection<Document> collection(String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(collectionName);
    }

    private CognitiveQueryArtifact artifact(String name, String cognitiveQuery) {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        artifact.setName(name);
        artifact.setCognitiveQuery(cognitiveQuery);
        return artifact;
    }

    private CognitiveTransactionGroup group(
            String name,
            CognitiveQueryArtifact first,
            CognitiveQueryArtifact second,
            CognitiveQueryArtifact third) {
        CognitiveTransactionGroup group = new CognitiveTransactionGroup();
        group.setName(name);
        List<CognitiveQueryArtifact> queries = new ArrayList<CognitiveQueryArtifact>();
        queries.add(first);
        queries.add(second);
        queries.add(third);
        group.setQueries(queries);
        return group;
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
}
