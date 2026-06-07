package ai.braineous.motion.ingestion.timeprocessor.store;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class MongoMotionHappeningStore implements MotionHappeningStore {

    public static final String DEFAULT_DB_NAME = "motion";
    public static final String DEFAULT_COLLECTION_NAME = "motion_happening_store";

    @Inject
    MongoClient mongoClient;

    private String dbName;
    private String collectionName;
    private Gson gson;

    public MongoMotionHappeningStore() {
        this.dbName = DEFAULT_DB_NAME;
        this.collectionName = DEFAULT_COLLECTION_NAME;
        this.gson = new Gson();
    }

    //---------------------------------

    @Override
    public void addRecord(MotionHappeningRecord record) {

        if (record == null) {
            return;
        }

        Document doc = new Document();

        doc.put("recordId", safe(record.getRecordId()));
        doc.put("routingKey", safe(record.getRoutingKey()));

        doc.put("subjectId", safe(record.getSubjectId()));
        doc.put("subjectType", safe(record.getSubjectType()));

        doc.put("frameId", safe(record.getFrameId()));
        doc.put("frameType", safe(record.getFrameType()));

        doc.put("status", safe(record.getStatus()));

        doc.put(
                "record",
                Document.parse(gson.toJson(record)));

        getCollection().insertOne(doc);
    }

    @Override
    public MotionHappeningRecord findByRoutingKey(String routingKey) {

        if (routingKey == null) {
            return null;
        }

        String needle = routingKey.trim();

        if (needle.isEmpty()) {
            return null;
        }

        Document doc =
                getCollection()
                        .find(eq("routingKey", needle))
                        .first();

        return readRecord(doc);
    }

    @Override
    public MotionHappeningRecord findByFrameId(String frameId) {

        if (frameId == null) {
            return null;
        }

        String needle = frameId.trim();

        if (needle.isEmpty()) {
            return null;
        }

        Document doc =
                getCollection()
                        .find(eq("frameId", needle))
                        .first();

        return readRecord(doc);
    }

    @Override
    public List<MotionHappeningRecord> findBySubject(
            String subjectId,
            String subjectType) {

        List<MotionHappeningRecord> out =
                new ArrayList<MotionHappeningRecord>();

        if (subjectId == null || subjectType == null) {
            return out;
        }

        String sid = subjectId.trim();
        String stype = subjectType.trim();

        if (sid.isEmpty() || stype.isEmpty()) {
            return out;
        }

        Document filter = new Document();
        filter.put("subjectId", sid);
        filter.put("subjectType", stype);

        FindIterable<Document> docs =
                getCollection().find(filter);

        for (Document doc : docs) {

            MotionHappeningRecord record =
                    readRecord(doc);

            if (record != null) {
                out.add(record);
            }
        }

        return out;
    }

    @Override
    public List<MotionHappeningRecord> getAll() {

        List<MotionHappeningRecord> out =
                new ArrayList<MotionHappeningRecord>();

        for (Document doc : getCollection().find()) {

            MotionHappeningRecord record =
                    readRecord(doc);

            if (record != null) {
                out.add(record);
            }
        }

        return out;
    }

    @Override
    public void clear() {
        getCollection().deleteMany(new Document());
    }

    private MongoCollection<Document> getCollection() {

        MongoDatabase db =
                mongoClient.getDatabase(dbName);

        return db.getCollection(collectionName);
    }

    private MotionHappeningRecord readRecord(Document doc) {

        if (doc == null) {
            return null;
        }

        Document recordDoc =
                (Document) doc.get("record");

        if (recordDoc == null) {
            return null;
        }

        return gson.fromJson(
                recordDoc.toJson(),
                MotionHappeningRecord.class);
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