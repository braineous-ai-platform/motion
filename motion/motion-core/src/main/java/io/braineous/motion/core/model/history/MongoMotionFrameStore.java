package io.braineous.motion.core.model.history;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoMotionFrameStore implements MotionFrameStore {

    public static final String DEFAULT_DB_NAME = "motion";
    public static final String DEFAULT_COLLECTION_NAME = "motion_frame_store";

    private MongoClient mongoClient;
    private String dbName;
    private String collectionName;
    private Gson gson;

    public MongoMotionFrameStore() {
        this.dbName = DEFAULT_DB_NAME;
        this.collectionName = DEFAULT_COLLECTION_NAME;
        this.mongoClient = MongoClients.create("mongodb://localhost:27017");
        this.gson = new Gson();
    }

    public MongoMotionFrameStore(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.dbName = DEFAULT_DB_NAME;
        this.collectionName = DEFAULT_COLLECTION_NAME;
        this.gson = new Gson();
    }

    public MongoMotionFrameStore(MongoClient mongoClient, String dbName, String collectionName) {
        if (mongoClient == null) {
            throw new IllegalArgumentException("mongoClient cannot be null");
        }
        if (dbName == null || dbName.trim().isEmpty()) {
            throw new IllegalArgumentException("dbName cannot be null/empty");
        }
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("collectionName cannot be null/empty");
        }

        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.collectionName = collectionName;
        this.gson = new Gson();
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void addRecord(MotionFrameRecord record) {
        if (record == null) {
            return;
        }

        Document doc = new Document();
        doc.put("recordId", safe(record.getRecordId()));
        doc.put("contextId", safe(record.getContextId()));
        doc.put("contextType", safe(record.getContextType()));
        doc.put("subjectId", safe(record.getSubjectId()));
        doc.put("subjectType", safe(record.getSubjectType()));
        doc.put("frameId", safe(record.getFrameId()));
        doc.put("frameType", safe(record.getFrameType()));
        doc.put("windowStart", safe(record.getWindowStart()));
        doc.put("windowEnd", safe(record.getWindowEnd()));
        doc.put("sequence", safe(record.getSequence()));
        doc.put("record", Document.parse(gson.toJson(record)));

        getCollection().insertOne(doc);
    }

    @Override
    public MotionFrameRecord findByFrameId(String frameId) {
        if (frameId == null) {
            return null;
        }

        String needle = frameId.trim();
        if (needle.isEmpty()) {
            return null;
        }

        Document doc = getCollection().find(eq("frameId", needle)).first();

        return readRecord(doc);
    }

    @Override
    public List<MotionFrameRecord> findBySubject(String subjectId, String subjectType) {
        List<MotionFrameRecord> out = new ArrayList<MotionFrameRecord>();

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

        FindIterable<Document> docs = getCollection().find(filter);

        for (Document doc : docs) {
            MotionFrameRecord record = readRecord(doc);
            if (record != null) {
                out.add(record);
            }
        }

        return out;
    }

    @Override
    public List<MotionFrameRecord> findByContextId(String contextId) {
        List<MotionFrameRecord> out = new ArrayList<MotionFrameRecord>();

        if (contextId == null) {
            return out;
        }

        String cid = contextId.trim();
        if (cid.isEmpty()) {
            return out;
        }

        FindIterable<Document> docs = getCollection().find(eq("contextId", cid));

        for (Document doc : docs) {
            MotionFrameRecord record = readRecord(doc);
            if (record != null) {
                out.add(record);
            }
        }

        return out;
    }

    @Override
    public List<MotionFrameRecord> getAll() {
        List<MotionFrameRecord> out = new ArrayList<MotionFrameRecord>();

        for (Document doc : getCollection().find()) {
            MotionFrameRecord record = readRecord(doc);
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
        MongoDatabase db = mongoClient.getDatabase(dbName);
        return db.getCollection(collectionName);
    }

    private MotionFrameRecord readRecord(Document doc) {
        if (doc == null) {
            return null;
        }

        Document recordDoc = (Document) doc.get("record");
        if (recordDoc == null) {
            return null;
        }

        return gson.fromJson(recordDoc.toJson(), MotionFrameRecord.class);
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