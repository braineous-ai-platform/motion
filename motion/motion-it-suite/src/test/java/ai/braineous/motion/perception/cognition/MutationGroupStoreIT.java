package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
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
public class MutationGroupStoreIT {

    private static final String DB_NAME = "motion";
    private static final String COLLECTION_NAME = "mutation_group_store";
    private static final String GROUP_NAME = "mutation-group-store-it-payment";

    @Inject
    MutationGroupStore mutationGroupStore;

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {
        MongoCollection<Document> collection = groups();
        collection.deleteMany(eq("name", GROUP_NAME));

        try {
            Console.log("mutation_group_store.db", DB_NAME);
            Console.log("mutation_group_store.collection", COLLECTION_NAME);

            CognitiveTransactionGroup original = group(
                    GROUP_NAME,
                    artifact("select_action", "select mutation_decision from llm"),
                    artifact("confirm_action", "select confirmation from llm"));

            mutationGroupStore.put(original);

            CognitiveTransactionGroup stored = mutationGroupStore.get(GROUP_NAME);
            long countAfterPut = collection.countDocuments(eq("name", GROUP_NAME));
            Console.log("put.original", stored.toJson());
            Console.log("count.afterPut", String.valueOf(countAfterPut));

            Assertions.assertEquals(1L, countAfterPut);
            assertGroup(original, stored);

            CognitiveTransactionGroup replacement = group(
                    GROUP_NAME,
                    artifact("record_action", "select record from llm"),
                    artifact("audit_action", "select audit from llm"));

            mutationGroupStore.put(replacement);

            CognitiveTransactionGroup replaced = mutationGroupStore.get(GROUP_NAME);
            long countAfterReplace = collection.countDocuments(eq("name", GROUP_NAME));
            Console.log("put.replacement", replaced.toJson());
            Console.log("count.afterReplace", String.valueOf(countAfterReplace));

            Assertions.assertEquals(1L, countAfterReplace);
            assertGroup(replacement, replaced);

            mutationGroupStore.remove(GROUP_NAME);

            CognitiveTransactionGroup afterRemove = mutationGroupStore.get(GROUP_NAME);
            long countAfterRemove = collection.countDocuments(eq("name", GROUP_NAME));
            Console.log("get.afterRemove", String.valueOf(afterRemove));
            Console.log("count.afterRemove", String.valueOf(countAfterRemove));

            Assertions.assertNull(afterRemove);
            Assertions.assertEquals(0L, countAfterRemove);
        } finally {
            collection.deleteMany(eq("name", GROUP_NAME));
        }
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
            CognitiveQueryArtifact second) {
        List<CognitiveQueryArtifact> queries = new ArrayList<CognitiveQueryArtifact>();
        queries.add(first);
        queries.add(second);

        CognitiveTransactionGroup group = new CognitiveTransactionGroup();
        group.setName(name);
        group.setQueries(queries);
        return group;
    }

    private void assertGroup(
            CognitiveTransactionGroup expected,
            CognitiveTransactionGroup actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getName(), actual.getName());
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

    private MongoCollection<Document> groups() {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        return database.getCollection(COLLECTION_NAME);
    }
}
