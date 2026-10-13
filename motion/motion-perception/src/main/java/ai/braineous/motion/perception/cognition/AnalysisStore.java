package ai.braineous.motion.perception.cognition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import ai.braineous.motion.perception.model.SystemAnalysis;
import io.braineous.motion.core.model.MotionBaseModel;
import io.quarkus.arc.Unremovable;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
@Unremovable
public class AnalysisStore {

    private static final String DEFAULT_DB_NAME = "motion";
    private static final String DEFAULT_COLLECTION_NAME = "analysis_store";

    @Inject
    MongoClient mongoClient;

    private String dbName = DEFAULT_DB_NAME;
    private String collectionName = DEFAULT_COLLECTION_NAME;

    public AnalysisStore() {
    }

    AnalysisStore(
            MongoClient mongoClient,
            String dbName,
            String collectionName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    public void put(SystemAnalysis analysis) {
        if (analysis == null) {
            return;
        }

        String key = safe(analysis.getAnalysisId());
        if (key == null) {
            return;
        }

        Document replacement = Document.parse(analysis.toJson());

        getCollection().replaceOne(
                eq("analysisId", key),
                replacement,
                new ReplaceOptions().upsert(true));
    }

    public SystemAnalysis get(String analysisId) {
        String key = safe(analysisId);
        if (key == null) {
            return null;
        }

        Document document = getCollection().find(eq("analysisId", key)).first();
        if (document == null) {
            return null;
        }

        document.remove("_id");

        return MotionBaseModel.fromJson(
                document.toJson(),
                SystemAnalysis.class);
    }

    public void remove(String analysisId) {
        String key = safe(analysisId);
        if (key == null) {
            return;
        }

        getCollection().deleteOne(eq("analysisId", key));
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
