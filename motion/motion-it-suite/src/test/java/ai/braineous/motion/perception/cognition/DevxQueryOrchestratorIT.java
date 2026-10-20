package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
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
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@QuarkusTest
public class DevxQueryOrchestratorIT {

    private static final String DB_NAME = "motion";
    private static final String ANALYSIS_GROUP_COLLECTION = "analysis_group_store";
    private static final String MUTATION_GROUP_COLLECTION = "mutation_group_store";
    private static final String ANALYSIS_QUERY_COLLECTION = "analysis_query_store";
    private static final String MUTATION_QUERY_COLLECTION = "mutation_query_store";

    private static final String PAYMENT_ANALYSIS = "devx-qo-it-payment_analysis";
    private static final String PAYMENT_SUMMARY = "devx-qo-it-payment_summary";
    private static final String PAYMENT_MUTATION = "devx-qo-it-payment_mutation";
    private static final String PAYMENT_CONFIRMATION = "devx-qo-it-payment_confirmation";

    private static final String ANALYZE_PAYMENT = "devx-qo-it-analyze_payment";
    private static final String EVALUATE_RISK = "devx-qo-it-evaluate_risk";
    private static final String SUMMARIZE_ANALYSIS = "devx-qo-it-summarize_analysis";
    private static final String SELECT_ACTION = "devx-qo-it-select_action";
    private static final String CONFIRM_ACTION = "devx-qo-it-confirm_action";
    private static final String RECORD_ACTION = "devx-qo-it-record_action";

    private static final String ANALYZE_SQL = "select analysis_decision from llm";
    private static final String RISK_SQL = "select risk_decision from llm";
    private static final String SUMMARY_SQL = "select summary from llm";
    private static final String SELECT_SQL = "select mutation_decision from llm";
    private static final String CONFIRM_SQL = "select confirmation from llm";
    private static final String RECORD_SQL = "select record from llm";

    private static final String ANALYZE_SQL_REVISED = "select revised_analysis_decision from llm";
    private static final String RISK_SQL_REVISED = "select revised_risk_decision from llm";
    private static final String SUMMARY_SQL_REVISED = "select revised_summary from llm";
    private static final String SELECT_SQL_REVISED = "select revised_mutation_decision from llm";
    private static final String CONFIRM_SQL_REVISED = "select revised_confirmation from llm";
    private static final String RECORD_SQL_REVISED = "select revised_record from llm";

    private static final String ANALYZE_SQL_ANALYSIS_ONLY = "select analysis_only_decision from llm";
    private static final String RISK_SQL_ANALYSIS_ONLY = "select analysis_only_risk from llm";
    private static final String SUMMARY_SQL_ANALYSIS_ONLY = "select analysis_only_summary from llm";

    private static final String SELECT_SQL_MUTATION_ONLY = "select mutation_only_decision from llm";
    private static final String CONFIRM_SQL_MUTATION_ONLY = "select mutation_only_confirmation from llm";
    private static final String RECORD_SQL_MUTATION_ONLY = "select mutation_only_record from llm";

    @Inject
    DevxQueryOrchestrator devxQueryOrchestrator;

    @Inject
    AnalysisGroupStore analysisGroupStore;

    @Inject
    MutationGroupStore mutationGroupStore;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Inject
    MutationQueryStore mutationQueryStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        deleteFixtures();

        try {
            Console.log("devx_query_orchestrator.db", DB_NAME);
            Console.log("analysis_group_store.collection", ANALYSIS_GROUP_COLLECTION);
            Console.log("mutation_group_store.collection", MUTATION_GROUP_COLLECTION);
            Console.log("analysis_query_store.collection", ANALYSIS_QUERY_COLLECTION);
            Console.log("mutation_query_store.collection", MUTATION_QUERY_COLLECTION);

            CognitiveTransactionGroup paymentAnalysis = group(
                    PAYMENT_ANALYSIS,
                    artifact(ANALYZE_PAYMENT, ANALYZE_SQL),
                    artifact(EVALUATE_RISK, RISK_SQL));
            CognitiveTransactionGroup paymentSummary = group(
                    PAYMENT_SUMMARY,
                    artifact(SUMMARIZE_ANALYSIS, SUMMARY_SQL));
            CognitiveTransactionGroup paymentMutation = group(
                    PAYMENT_MUTATION,
                    artifact(SELECT_ACTION, SELECT_SQL),
                    artifact(CONFIRM_ACTION, CONFIRM_SQL));
            CognitiveTransactionGroup paymentConfirmation = group(
                    PAYMENT_CONFIRMATION,
                    artifact(RECORD_ACTION, RECORD_SQL));

            DevxDefinition original = definition(
                    section(paymentAnalysis, paymentSummary),
                    section(paymentMutation, paymentConfirmation));

            devxQueryOrchestrator.publish(original);

            CognitiveTransactionGroup storedAnalysis = analysisGroupStore.get(PAYMENT_ANALYSIS);
            CognitiveTransactionGroup storedSummary = analysisGroupStore.get(PAYMENT_SUMMARY);
            CognitiveTransactionGroup storedMutation = mutationGroupStore.get(PAYMENT_MUTATION);
            CognitiveTransactionGroup storedConfirmation = mutationGroupStore.get(PAYMENT_CONFIRMATION);
            Console.log("analysis.group.payment_analysis", storedAnalysis.toJson());
            Console.log("analysis.group.payment_summary", storedSummary.toJson());
            Console.log("mutation.group.payment_mutation", storedMutation.toJson());
            Console.log("mutation.group.payment_confirmation", storedConfirmation.toJson());

            assertGroup(paymentAnalysis, storedAnalysis);
            assertGroup(paymentSummary, storedSummary);
            assertGroup(paymentMutation, storedMutation);
            assertGroup(paymentConfirmation, storedConfirmation);
            assertOne(ANALYSIS_GROUP_COLLECTION, PAYMENT_ANALYSIS);
            assertOne(ANALYSIS_GROUP_COLLECTION, PAYMENT_SUMMARY);
            assertOne(MUTATION_GROUP_COLLECTION, PAYMENT_MUTATION);
            assertOne(MUTATION_GROUP_COLLECTION, PAYMENT_CONFIRMATION);

            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, ANALYZE_PAYMENT, ANALYZE_SQL);
            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, EVALUATE_RISK, RISK_SQL);
            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, SUMMARIZE_ANALYSIS, SUMMARY_SQL);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, SELECT_ACTION, SELECT_SQL);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, CONFIRM_ACTION, CONFIRM_SQL);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, RECORD_ACTION, RECORD_SQL);
            assertAbsent(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, SELECT_ACTION);
            assertAbsent(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, CONFIRM_ACTION);
            assertAbsent(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, RECORD_ACTION);
            assertAbsent(mutationQueryStore, MUTATION_QUERY_COLLECTION, ANALYZE_PAYMENT);
            assertAbsent(mutationQueryStore, MUTATION_QUERY_COLLECTION, EVALUATE_RISK);
            assertAbsent(mutationQueryStore, MUTATION_QUERY_COLLECTION, SUMMARIZE_ANALYSIS);

            CognitiveTransactionGroup revisedAnalysis = group(
                    PAYMENT_ANALYSIS,
                    artifact(ANALYZE_PAYMENT, ANALYZE_SQL_REVISED),
                    artifact(EVALUATE_RISK, RISK_SQL_REVISED));
            CognitiveTransactionGroup revisedSummary = group(
                    PAYMENT_SUMMARY,
                    artifact(SUMMARIZE_ANALYSIS, SUMMARY_SQL_REVISED));
            CognitiveTransactionGroup revisedMutation = group(
                    PAYMENT_MUTATION,
                    artifact(SELECT_ACTION, SELECT_SQL_REVISED),
                    artifact(CONFIRM_ACTION, CONFIRM_SQL_REVISED));
            CognitiveTransactionGroup revisedConfirmation = group(
                    PAYMENT_CONFIRMATION,
                    artifact(RECORD_ACTION, RECORD_SQL_REVISED));

            devxQueryOrchestrator.publish(definition(
                    section(revisedAnalysis, revisedSummary),
                    section(revisedMutation, revisedConfirmation)));

            CognitiveTransactionGroup replacedAnalysis = analysisGroupStore.get(PAYMENT_ANALYSIS);
            CognitiveTransactionGroup replacedSummary = analysisGroupStore.get(PAYMENT_SUMMARY);
            CognitiveTransactionGroup replacedMutation = mutationGroupStore.get(PAYMENT_MUTATION);
            CognitiveTransactionGroup replacedConfirmation = mutationGroupStore.get(PAYMENT_CONFIRMATION);
            Console.log("upsert.analysis.payment_analysis", replacedAnalysis.toJson());
            Console.log("upsert.mutation.payment_mutation", replacedMutation.toJson());
            Console.log("upsert.analysis_query.count", String.valueOf(count(ANALYSIS_QUERY_COLLECTION, ANALYZE_PAYMENT)));
            Console.log("upsert.mutation_query.count", String.valueOf(count(MUTATION_QUERY_COLLECTION, SELECT_ACTION)));

            assertGroup(revisedAnalysis, replacedAnalysis);
            assertGroup(revisedSummary, replacedSummary);
            assertGroup(revisedMutation, replacedMutation);
            assertGroup(revisedConfirmation, replacedConfirmation);
            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, ANALYZE_PAYMENT, ANALYZE_SQL_REVISED);
            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, EVALUATE_RISK, RISK_SQL_REVISED);
            assertQuery(analysisQueryStore, ANALYSIS_QUERY_COLLECTION, SUMMARIZE_ANALYSIS, SUMMARY_SQL_REVISED);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, SELECT_ACTION, SELECT_SQL_REVISED);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, CONFIRM_ACTION, CONFIRM_SQL_REVISED);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, RECORD_ACTION, RECORD_SQL_REVISED);
            assertOne(ANALYSIS_GROUP_COLLECTION, PAYMENT_ANALYSIS);
            assertOne(ANALYSIS_GROUP_COLLECTION, PAYMENT_SUMMARY);
            assertOne(MUTATION_GROUP_COLLECTION, PAYMENT_MUTATION);
            assertOne(MUTATION_GROUP_COLLECTION, PAYMENT_CONFIRMATION);

            String mutationBeforeAnalysisOnly = replacedMutation.toJson();
            String confirmationBeforeAnalysisOnly = replacedConfirmation.toJson();

            CognitiveTransactionGroup analysisOnlyGroup = group(
                    PAYMENT_ANALYSIS,
                    artifact(ANALYZE_PAYMENT, ANALYZE_SQL_ANALYSIS_ONLY),
                    artifact(EVALUATE_RISK, RISK_SQL_ANALYSIS_ONLY));
            CognitiveTransactionGroup summaryOnlyGroup = group(
                    PAYMENT_SUMMARY,
                    artifact(SUMMARIZE_ANALYSIS, SUMMARY_SQL_ANALYSIS_ONLY));

            devxQueryOrchestrator.publish(definition(
                    section(analysisOnlyGroup, summaryOnlyGroup),
                    null));

            assertGroup(analysisOnlyGroup, analysisGroupStore.get(PAYMENT_ANALYSIS));
            assertGroup(summaryOnlyGroup, analysisGroupStore.get(PAYMENT_SUMMARY));
            assertQuery(
                    analysisQueryStore,
                    ANALYSIS_QUERY_COLLECTION,
                    ANALYZE_PAYMENT,
                    ANALYZE_SQL_ANALYSIS_ONLY);
            Assertions.assertEquals(
                    mutationBeforeAnalysisOnly,
                    mutationGroupStore.get(PAYMENT_MUTATION).toJson());
            Assertions.assertEquals(
                    confirmationBeforeAnalysisOnly,
                    mutationGroupStore.get(PAYMENT_CONFIRMATION).toJson());
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, SELECT_ACTION, SELECT_SQL_REVISED);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, CONFIRM_ACTION, CONFIRM_SQL_REVISED);
            assertQuery(mutationQueryStore, MUTATION_QUERY_COLLECTION, RECORD_ACTION, RECORD_SQL_REVISED);
            assertOne(MUTATION_GROUP_COLLECTION, PAYMENT_MUTATION);
            assertOne(MUTATION_QUERY_COLLECTION, SELECT_ACTION);
            Console.log(
                    "partial.analysis_only.mutation_unchanged",
                    mutationGroupStore.get(PAYMENT_MUTATION).toJson());

            String analysisBeforeMutationOnly = analysisGroupStore.get(PAYMENT_ANALYSIS).toJson();
            String summaryBeforeMutationOnly = analysisGroupStore.get(PAYMENT_SUMMARY).toJson();

            CognitiveTransactionGroup mutationOnlyGroup = group(
                    PAYMENT_MUTATION,
                    artifact(SELECT_ACTION, SELECT_SQL_MUTATION_ONLY),
                    artifact(CONFIRM_ACTION, CONFIRM_SQL_MUTATION_ONLY));
            CognitiveTransactionGroup confirmationOnlyGroup = group(
                    PAYMENT_CONFIRMATION,
                    artifact(RECORD_ACTION, RECORD_SQL_MUTATION_ONLY));

            devxQueryOrchestrator.publish(definition(
                    null,
                    section(mutationOnlyGroup, confirmationOnlyGroup)));

            assertGroup(mutationOnlyGroup, mutationGroupStore.get(PAYMENT_MUTATION));
            assertGroup(confirmationOnlyGroup, mutationGroupStore.get(PAYMENT_CONFIRMATION));
            assertQuery(
                    mutationQueryStore,
                    MUTATION_QUERY_COLLECTION,
                    SELECT_ACTION,
                    SELECT_SQL_MUTATION_ONLY);
            Assertions.assertEquals(
                    analysisBeforeMutationOnly,
                    analysisGroupStore.get(PAYMENT_ANALYSIS).toJson());
            Assertions.assertEquals(
                    summaryBeforeMutationOnly,
                    analysisGroupStore.get(PAYMENT_SUMMARY).toJson());
            assertQuery(
                    analysisQueryStore,
                    ANALYSIS_QUERY_COLLECTION,
                    ANALYZE_PAYMENT,
                    ANALYZE_SQL_ANALYSIS_ONLY);
            assertQuery(
                    analysisQueryStore,
                    ANALYSIS_QUERY_COLLECTION,
                    EVALUATE_RISK,
                    RISK_SQL_ANALYSIS_ONLY);
            assertQuery(
                    analysisQueryStore,
                    ANALYSIS_QUERY_COLLECTION,
                    SUMMARIZE_ANALYSIS,
                    SUMMARY_SQL_ANALYSIS_ONLY);
            assertOne(ANALYSIS_GROUP_COLLECTION, PAYMENT_ANALYSIS);
            assertOne(ANALYSIS_QUERY_COLLECTION, ANALYZE_PAYMENT);
            Console.log(
                    "partial.mutation_only.analysis_unchanged",
                    analysisGroupStore.get(PAYMENT_ANALYSIS).toJson());
        } finally {
            deleteFixtures();
        }
    }

    private void assertGroup(
            CognitiveTransactionGroup expected,
            CognitiveTransactionGroup actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.toJson(), actual.toJson());
        Assertions.assertNotNull(actual.getQueries());
        Assertions.assertEquals(expected.getQueries().size(), actual.getQueries().size());

        int index = 0;
        while (index < expected.getQueries().size()) {
            CognitiveQueryArtifact expectedQuery = expected.getQueries().get(index);
            CognitiveQueryArtifact actualQuery = actual.getQueries().get(index);
            Assertions.assertEquals(expectedQuery.getName(), actualQuery.getName());
            Assertions.assertEquals(
                    expectedQuery.getCognitiveQuery(),
                    actualQuery.getCognitiveQuery());
            index = index + 1;
        }
    }

    private void assertQuery(
            AnalysisQueryStore store,
            String collectionName,
            String name,
            String cognitiveQuery) {
        Assertions.assertEquals(cognitiveQuery, store.get(name));
        assertOne(collectionName, name);
    }

    private void assertQuery(
            MutationQueryStore store,
            String collectionName,
            String name,
            String cognitiveQuery) {
        Assertions.assertEquals(cognitiveQuery, store.get(name));
        assertOne(collectionName, name);
    }

    private void assertAbsent(
            AnalysisQueryStore store,
            String collectionName,
            String name) {
        Assertions.assertNull(store.get(name));
        Assertions.assertEquals(0L, count(collectionName, name));
    }

    private void assertAbsent(
            MutationQueryStore store,
            String collectionName,
            String name) {
        Assertions.assertNull(store.get(name));
        Assertions.assertEquals(0L, count(collectionName, name));
    }

    private void assertOne(String collectionName, String name) {
        Assertions.assertEquals(1L, count(collectionName, name));
    }

    private long count(String collectionName, String name) {
        return collection(collectionName).countDocuments(eq("name", name));
    }

    private void deleteFixtures() {
        delete(ANALYSIS_GROUP_COLLECTION, PAYMENT_ANALYSIS);
        delete(ANALYSIS_GROUP_COLLECTION, PAYMENT_SUMMARY);
        delete(MUTATION_GROUP_COLLECTION, PAYMENT_MUTATION);
        delete(MUTATION_GROUP_COLLECTION, PAYMENT_CONFIRMATION);
        delete(ANALYSIS_QUERY_COLLECTION, ANALYZE_PAYMENT);
        delete(ANALYSIS_QUERY_COLLECTION, EVALUATE_RISK);
        delete(ANALYSIS_QUERY_COLLECTION, SUMMARIZE_ANALYSIS);
        delete(MUTATION_QUERY_COLLECTION, SELECT_ACTION);
        delete(MUTATION_QUERY_COLLECTION, CONFIRM_ACTION);
        delete(MUTATION_QUERY_COLLECTION, RECORD_ACTION);
    }

    private void delete(String collectionName, String name) {
        collection(collectionName).deleteMany(eq("name", name));
    }

    private MongoCollection<Document> collection(String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(collectionName);
    }

    private DevxDefinition definition(
            List<CognitiveTransactionGroup> analysis,
            List<CognitiveTransactionGroup> mutation) {
        DevxDefinition definition = new DevxDefinition();
        if (analysis != null) {
            definition.setAnalysis(analysis);
        }
        if (mutation != null) {
            definition.setMutation(mutation);
        }
        return definition;
    }

    private CognitiveQueryArtifact artifact(String name, String cognitiveQuery) {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        artifact.setName(name);
        artifact.setCognitiveQuery(cognitiveQuery);
        return artifact;
    }

    private CognitiveTransactionGroup group(
            String name,
            CognitiveQueryArtifact... queries) {
        CognitiveTransactionGroup group = new CognitiveTransactionGroup();
        group.setName(name);
        List<CognitiveQueryArtifact> ordered = new ArrayList<CognitiveQueryArtifact>();
        int index = 0;
        while (index < queries.length) {
            ordered.add(queries[index]);
            index = index + 1;
        }
        group.setQueries(ordered);
        return group;
    }

    private List<CognitiveTransactionGroup> section(CognitiveTransactionGroup... groups) {
        List<CognitiveTransactionGroup> section = new ArrayList<CognitiveTransactionGroup>();
        int index = 0;
        while (index < groups.length) {
            section.add(groups[index]);
            index = index + 1;
        }
        return section;
    }
}
