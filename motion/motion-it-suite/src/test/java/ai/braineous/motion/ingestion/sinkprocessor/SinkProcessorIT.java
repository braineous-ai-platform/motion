package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.observe.Console;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionTimeWindow;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class SinkProcessorIT {

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {

        System.setProperty(
                "cgo.mongodb.uri",
                "mongodb://localhost:27018");

        MongoDatabase operationalDatabase =
                mongoClient.getDatabase("motion_it");
        MongoCollection<Document> operationalCollection =
                operationalDatabase.getCollection(
                        "operational_view_sink_processor");

        MongoDatabase cgoDatabase =
                mongoClient.getDatabase("cgo");
        MongoCollection<Document> nodeCollection =
                cgoDatabase.getCollection("cgo_nodes");
        MongoCollection<Document> edgeCollection =
                cgoDatabase.getCollection("cgo_edges");

        operationalCollection.deleteMany(new Document());
        nodeCollection.deleteMany(new Document());
        edgeCollection.deleteMany(new Document());
        GraphBuilder.getInstance().clear();

        try {
            MongoOperationalSink isolatedMongoOperationalSink =
                    new MongoOperationalSink(
                            mongoClient,
                            "motion_it",
                            "operational_view_sink_processor");

            SinkProcessor sinkProcessor =
                    new SinkProcessor(
                            new OperationalViewMaterializer(),
                            isolatedMongoOperationalSink);

            MotionFrame motionFrame = new MotionFrame();
            motionFrame.setFrameId("frame-1");
            motionFrame.setFrameType("ORDER_OPERATION_FRAME");

            MotionTimeWindow timeWindow = new MotionTimeWindow();
            timeWindow.setWindowStart("2026-09-13T10:00:00Z");
            timeWindow.setWindowEnd("2026-09-13T10:05:00Z");
            motionFrame.setTimeWindow(timeWindow);

            motionFrame.setSequence("1");
            motionFrame.setStatus("OPEN");
            motionFrame.setMetadataJson("{\"runtime\":\"motion\"}");

            MotionEvent event1 =
                    newEvent("event-1", "ORDER_CREATED", "CREATED");
            MotionEvent event2 =
                    newEvent("event-2", "ORDER_UPDATED", "UPDATED");
            MotionEvent event3 =
                    newEvent("event-3", "ORDER_SHIPPED", "SHIPPED");

            motionFrame.addMotionEvent(event1);
            motionFrame.addMotionEvent(event2);
            motionFrame.addMotionEvent(event3);

            Console.log(
                    "SinkProcessorIT.input",
                    motionFrame.toJson());

            sinkProcessor.process(motionFrame);

            long matchingDocuments =
                    operationalCollection.countDocuments(
                            eq("viewId", "frame-1"));
            Document persistedDocument =
                    operationalCollection.find(
                            eq("viewId", "frame-1")).first();

            assertEquals(1L, matchingDocuments);
            assertNotNull(persistedDocument);

            Console.log(
                    "SinkProcessorIT.ov.document",
                    persistedDocument.toJson());
            Console.log(
                    "SinkProcessorIT.ov.count",
                    String.valueOf(matchingDocuments));

            assertEquals("frame-1", persistedDocument.getString("viewId"));
            assertNull(persistedDocument.get("routingKey"));
            assertNull(persistedDocument.get("contextId"));
            assertEquals(
                    "2026-09-13T10:00:00Z",
                    persistedDocument.getString("windowStart"));
            assertEquals(
                    "2026-09-13T10:05:00Z",
                    persistedDocument.getString("windowEnd"));

            Document state =
                    persistedDocument.get("state", Document.class);

            assertNotNull(state);
            assertEquals(4, state.size());
            assertEquals(
                    "ORDER_OPERATION_FRAME",
                    state.getString("frameType"));
            assertEquals("1", state.getString("sequence"));
            assertEquals("OPEN", state.getString("status"));
            assertEquals(
                    "{\"runtime\":\"motion\"}",
                    state.getString("metadataJson"));

            List<String> sourceFrameIds =
                    persistedDocument.getList(
                            "sourceFrameIds",
                            String.class);

            assertNotNull(sourceFrameIds);
            assertEquals(1, sourceFrameIds.size());
            assertEquals("frame-1", sourceFrameIds.get(0));

            List<String> sourceEventIds =
                    persistedDocument.getList(
                            "sourceEventIds",
                            String.class);

            assertNotNull(sourceEventIds);
            assertEquals(3, sourceEventIds.size());
            assertEquals("event-1", sourceEventIds.get(0));
            assertEquals("event-2", sourceEventIds.get(1));
            assertEquals("event-3", sourceEventIds.get(2));

            String materializedAt =
                    persistedDocument.getString("materializedAt");

            assertNotNull(materializedAt);
            Instant.parse(materializedAt);

            long nodeCount = nodeCollection.countDocuments();
            long edgeCount = edgeCollection.countDocuments();

            Console.log(
                    "SinkProcessorIT.cgo.nodeCount",
                    String.valueOf(nodeCount));
            Console.log(
                    "SinkProcessorIT.cgo.edgeCount",
                    String.valueOf(edgeCount));

            for (Document node : nodeCollection.find()) {
                Console.log(
                        "SinkProcessorIT.cgo.node",
                        node.toJson());
            }

            for (Document edge : edgeCollection.find()) {
                Console.log(
                        "SinkProcessorIT.cgo.edge",
                        edge.toJson());
            }

            assertEquals(4L, nodeCount);
            assertEquals(3L, edgeCount);

            assertNode(nodeCollection, "MotionFrame:frame-1");
            assertNode(nodeCollection, "MotionEvent:event-1");
            assertNode(nodeCollection, "MotionEvent:event-2");
            assertNode(nodeCollection, "MotionEvent:event-3");

            assertEdge(
                    edgeCollection,
                    "Edge:MotionFrame:frame-1->MotionEvent:event-1",
                    "MotionEvent:event-1");
            assertEdge(
                    edgeCollection,
                    "Edge:MotionFrame:frame-1->MotionEvent:event-2",
                    "MotionEvent:event-2");
            assertEdge(
                    edgeCollection,
                    "Edge:MotionFrame:frame-1->MotionEvent:event-3",
                    "MotionEvent:event-3");

            for (Document edge : edgeCollection.find()) {
                assertFalse(
                        edge.getString("fromFactId")
                                .startsWith("MotionEvent:"));
            }

            operationalCollection.deleteMany(new Document());
            nodeCollection.deleteMany(new Document());
            edgeCollection.deleteMany(new Document());
            GraphBuilder.getInstance().clear();

            sinkProcessor.process(null);

            assertEquals(0L, operationalCollection.countDocuments());
            assertEquals(0L, nodeCollection.countDocuments());
            assertEquals(0L, edgeCollection.countDocuments());
        } finally {
            operationalCollection.deleteMany(new Document());
            nodeCollection.deleteMany(new Document());
            edgeCollection.deleteMany(new Document());
            GraphBuilder.getInstance().clear();
            System.clearProperty("cgo.mongodb.uri");
        }
    }

    private void assertNode(
            MongoCollection<Document> nodeCollection,
            String factId) {

        Document node =
                nodeCollection.find(eq("factId", factId)).first();

        assertNotNull(node);
        assertEquals("atomic", node.getString("mode"));
    }

    private void assertEdge(
            MongoCollection<Document> edgeCollection,
            String edgeId,
            String toFactId) {

        Document edge =
                edgeCollection.find(eq("edgeId", edgeId)).first();

        assertNotNull(edge);
        assertEquals("relational", edge.getString("mode"));
        assertEquals(
                "MotionFrame:frame-1",
                edge.getString("fromFactId"));
        assertEquals(toFactId, edge.getString("toFactId"));
    }

    private MotionEvent newEvent(
            String eventId,
            String eventType,
            String operation) {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId(eventId);
        motionEvent.setEventType(eventType);
        motionEvent.setOriginTime("2026-09-13T10:01:00Z");
        motionEvent.setReceivedAt("2026-09-13T10:01:01Z");
        motionEvent.setSubjectId("ORDER-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation(operation);
        motionEvent.setPayloadJson(
                "{\"status\":\"" + operation + "\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("FAILURE_RECOVERY");
        replaySignal.setReasonCode("REPLAY_NOT_REQUIRED");
        replaySignal.setMessage(
                "Replay is not required for accepted Motion event");
        replaySignal.setMotionEventId(eventId);
        motionEvent.setReplaySignal(replaySignal);

        return motionEvent;
    }
}
