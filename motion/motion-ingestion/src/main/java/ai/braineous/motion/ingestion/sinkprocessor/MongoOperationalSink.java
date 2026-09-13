package ai.braineous.motion.ingestion.sinkprocessor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;

@ApplicationScoped
public class MongoOperationalSink implements OperationalSink {

    private static final String DEFAULT_DB_NAME = "motion";
    private static final String DEFAULT_COLLECTION_NAME = "operational_view";

    @Inject
    MongoClient mongoClient;

    private String dbName = DEFAULT_DB_NAME;
    private String collectionName = DEFAULT_COLLECTION_NAME;

    public MongoOperationalSink() {
    }

    MongoOperationalSink(
            MongoClient mongoClient,
            String dbName,
            String collectionName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    @Override
    public void write(OperationalView operationalView) {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection =
                database.getCollection(collectionName);

        Document document = Document.parse(operationalView.toJson());

        collection.insertOne(document);
    }
}
