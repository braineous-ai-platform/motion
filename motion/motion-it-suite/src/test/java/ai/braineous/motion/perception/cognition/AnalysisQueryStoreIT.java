package ai.braineous.motion.perception.cognition;

import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.mongodb.client.model.Filters.eq;

@QuarkusTest
public class AnalysisQueryStoreIT {

    private static final String DB_NAME = "motion";
    private static final String COLLECTION_NAME = "analysis_query_store";
    private static final String QUERY_NAME = "pay_v1";

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
    AnalysisQueryStore analysisQueryStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        MongoCollection<Document> collection = queries();
        collection.deleteMany(eq("name", QUERY_NAME));

        try {
            Console.log("analysis_query_store.db", DB_NAME);
            Console.log("analysis_query_store.collection", COLLECTION_NAME);

            analysisQueryStore.put(QUERY_NAME, PAY_V1_SQL);

            String stored = analysisQueryStore.get(QUERY_NAME);
            long countAfterPut = collection.countDocuments(eq("name", QUERY_NAME));
            Console.log("put.original", stored);
            Console.log("count.afterPut", String.valueOf(countAfterPut));

            Assertions.assertEquals(PAY_V1_SQL, stored);
            Assertions.assertEquals(1L, countAfterPut);

            analysisQueryStore.put(QUERY_NAME, PAY_V1_SQL_REPLACEMENT);

            String replaced = analysisQueryStore.get(QUERY_NAME);
            long countAfterReplace = collection.countDocuments(eq("name", QUERY_NAME));
            Console.log("put.replacement", replaced);
            Console.log("count.afterReplace", String.valueOf(countAfterReplace));

            Assertions.assertEquals(PAY_V1_SQL_REPLACEMENT, replaced);
            Assertions.assertEquals(1L, countAfterReplace);

            analysisQueryStore.remove(QUERY_NAME);

            String afterRemove = analysisQueryStore.get(QUERY_NAME);
            long countAfterRemove = collection.countDocuments(eq("name", QUERY_NAME));
            Console.log("get.afterRemove", String.valueOf(afterRemove));
            Console.log("count.afterRemove", String.valueOf(countAfterRemove));

            Assertions.assertNull(afterRemove);
            Assertions.assertEquals(0L, countAfterRemove);
        } finally {
            collection.deleteMany(eq("name", QUERY_NAME));
        }
    }

    private MongoCollection<Document> queries() {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(COLLECTION_NAME);
    }
}
