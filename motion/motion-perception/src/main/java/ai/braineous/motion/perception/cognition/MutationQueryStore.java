package ai.braineous.motion.perception.cognition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import io.quarkus.arc.Unremovable;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
@Unremovable
public class MutationQueryStore {

    private static final String DEFAULT_DB_NAME = "motion";
    private static final String DEFAULT_COLLECTION_NAME = "mutation_query_store";

    @Inject
    MongoClient mongoClient;

    private String dbName = DEFAULT_DB_NAME;
    private String collectionName = DEFAULT_COLLECTION_NAME;

    public MutationQueryStore() {
    }

    MutationQueryStore(
            MongoClient mongoClient,
            String dbName,
            String collectionName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    public void put(String name, String sql) {
        String key = safe(name);
        if (key == null) {
            return;
        }

        Document replacement = new Document();
        replacement.put("name", key);
        replacement.put("sql", sql);

        getCollection().replaceOne(
                eq("name", key),
                replacement,
                new ReplaceOptions().upsert(true));
    }

    public String get(String name) {
        String key = safe(name);
        if (key == null) {
            return null;
        }

        Document document = getCollection().find(eq("name", key)).first();
        if (document == null) {
            return null;
        }

        return document.getString("sql");
    }

    public void remove(String name) {
        String key = safe(name);
        if (key == null) {
            return;
        }

        getCollection().deleteOne(eq("name", key));
    }

    private MongoCollection<Document> getCollection() {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        return database.getCollection(collectionName);
    }

    private String safe(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}
