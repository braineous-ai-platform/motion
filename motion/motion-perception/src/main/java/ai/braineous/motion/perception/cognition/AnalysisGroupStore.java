package ai.braineous.motion.perception.cognition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import io.braineous.motion.core.model.MotionBaseModel;
import io.quarkus.arc.Unremovable;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
@Unremovable
public class AnalysisGroupStore {

    private static final String DEFAULT_DB_NAME = "motion";
    private static final String DEFAULT_COLLECTION_NAME = "analysis_group_store";

    @Inject
    MongoClient mongoClient;

    private String dbName = DEFAULT_DB_NAME;
    private String collectionName = DEFAULT_COLLECTION_NAME;

    public AnalysisGroupStore() {
    }

    AnalysisGroupStore(
            MongoClient mongoClient,
            String dbName,
            String collectionName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    public void put(CognitiveTransactionGroup group) {
        if (group == null) {
            return;
        }

        String key = safe(group.getName());
        if (key == null) {
            return;
        }

        Document replacement = Document.parse(group.toJson());

        getCollection().replaceOne(
                eq("name", key),
                replacement,
                new ReplaceOptions().upsert(true));
    }

    public CognitiveTransactionGroup get(String name) {
        String key = safe(name);
        if (key == null) {
            return null;
        }

        Document document = getCollection().find(eq("name", key)).first();
        if (document == null) {
            return null;
        }

        document.remove("_id");

        return MotionBaseModel.fromJson(
                document.toJson(),
                CognitiveTransactionGroup.class);
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
