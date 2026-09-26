package ai.braineous.motion.perception.support;

import ai.braineous.motion.ingestion.eventprocessor.infra.kafka.MotionEventEmitter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.braineous.motion.core.model.MotionEvent;
import org.bson.Document;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class PerceptionPipelineTestHarness {

    private static final String OPERATIONAL_VIEW_DATABASE = "motion";
    private static final String OPERATIONAL_VIEW_COLLECTION = "operational_view";
    private static final String CGO_DATABASE = "cgo";
    private static final String CGO_NODE_COLLECTION = "cgo_nodes";
    private static final String CGO_EDGE_COLLECTION = "cgo_edges";
    private static final long REALITY_TIMEOUT_NANOS =
            Duration.ofSeconds(20).toNanos();
    private static final long POLL_SLEEP_MILLIS = 1000L;

    private final MotionEventEmitter motionEventEmitter;
    private final MongoClient mongoClient;

    public PerceptionPipelineTestHarness(
            MotionEventEmitter motionEventEmitter,
            MongoClient mongoClient) {
        this.motionEventEmitter = motionEventEmitter;
        this.mongoClient = mongoClient;
    }

    public void emit(MotionEvent motionEvent) {
        motionEventEmitter.emit(motionEvent);
    }

    public MotionEvent newEvent() {
        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId(
                "perception-event-" + UUID.randomUUID().toString());
        motionEvent.setEventType("ORDER_UPDATED");
        motionEvent.setOriginTime("2026-06-06T12:00:00Z");
        motionEvent.setSubjectId(
                "perception-subject-" + UUID.randomUUID().toString());
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation("UPDATE");
        motionEvent.setPayloadJson("{\"status\":\"UPDATED\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"motion\"}");

        return motionEvent;
    }

    public void awaitReality(MotionEvent motionEvent) {
        String eventId = motionEvent.getEventId();
        String frameId = derivedFrameId(motionEvent);
        String cgoFactId = "MotionEvent:" + eventId;

        MongoCollection<Document> operationalViewCollection =
                operationalViewCollection();
        MongoCollection<Document> cgoNodeCollection =
                cgoNodeCollection();

        boolean operationalViewFound = false;
        boolean cgoNodeFound = false;
        long deadline = System.nanoTime() + REALITY_TIMEOUT_NANOS;

        while ((!operationalViewFound || !cgoNodeFound)
                && System.nanoTime() < deadline) {
            if (!operationalViewFound) {
                operationalViewFound = operationalViewPersisted(
                        operationalViewCollection,
                        frameId,
                        eventId);
            }

            if (!cgoNodeFound) {
                cgoNodeFound = cgoNodePersisted(
                        cgoNodeCollection,
                        cgoFactId);
            }

            if (operationalViewFound && cgoNodeFound) {
                return;
            }

            sleepQuietly();
        }

        throw new AssertionError(
                "Perception pipeline reality did not complete"
                        + " eventId=" + eventId
                        + " frameId=" + frameId
                        + " ovFound=" + operationalViewFound
                        + " cgoFound=" + cgoNodeFound);
    }

    public void clearReality() {
        operationalViewCollection().deleteMany(new Document());
        cgoNodeCollection().deleteMany(new Document());
        cgoEdgeCollection().deleteMany(new Document());
    }

    private String derivedFrameId(MotionEvent motionEvent) {
        String subjectId = motionEvent.getSubjectId();
        String subjectType = motionEvent.getSubjectType();
        String frameType = subjectType + "_OPERATION_FRAME";
        String routingKey =
                subjectId + ":" + subjectType + ":" + frameType;

        return routingKey + ":frame";
    }

    private MongoCollection<Document> operationalViewCollection() {
        MongoDatabase database =
                mongoClient.getDatabase(OPERATIONAL_VIEW_DATABASE);

        return database.getCollection(OPERATIONAL_VIEW_COLLECTION);
    }

    private MongoCollection<Document> cgoNodeCollection() {
        MongoDatabase database =
                mongoClient.getDatabase(CGO_DATABASE);

        return database.getCollection(CGO_NODE_COLLECTION);
    }

    private MongoCollection<Document> cgoEdgeCollection() {
        MongoDatabase database =
                mongoClient.getDatabase(CGO_DATABASE);

        return database.getCollection(CGO_EDGE_COLLECTION);
    }

    private boolean operationalViewPersisted(
            MongoCollection<Document> collection,
            String frameId,
            String eventId) {

        for (Document document : collection.find(eq("viewId", frameId))) {
            if (sourceEventIdsContain(document, eventId)) {
                return true;
            }
        }

        return false;
    }

    private boolean sourceEventIdsContain(
            Document document,
            String eventId) {

        List<String> sourceEventIds =
                document.getList("sourceEventIds", String.class);

        if (sourceEventIds == null) {
            return false;
        }

        int index = 0;
        while (index < sourceEventIds.size()) {
            if (eventId.equals(sourceEventIds.get(index))) {
                return true;
            }
            index = index + 1;
        }

        return false;
    }

    private boolean cgoNodePersisted(
            MongoCollection<Document> collection,
            String cgoFactId) {

        Document document =
                collection.find(eq("factId", cgoFactId)).first();

        if (document == null) {
            return false;
        }

        return true;
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(POLL_SLEEP_MILLIS);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new AssertionError(
                    "Perception pipeline reality wait interrupted",
                    interruptedException);
        }
    }
}
