package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import ai.braineous.rag.prompt.observe.Console;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class CGOECESinkIT {

    private static final String CGO_MONGO_URI =
            "mongodb://localhost:27018";
    private static final String CGO_DATABASE = "cgo";
    private static final String CGO_NODE_COLLECTION = "cgo_nodes";
    private static final String CGO_EDGE_COLLECTION = "cgo_edges";

    @Inject
    MongoClient mongoClient;

    private MongoCollection<Document> nodeCollection;
    private MongoCollection<Document> edgeCollection;

    @BeforeEach
    public void setup() {
        GraphBuilder.getInstance().clear();

        MongoDatabase database =
                mongoClient.getDatabase(CGO_DATABASE);

        this.nodeCollection =
                database.getCollection(CGO_NODE_COLLECTION);
        this.edgeCollection =
                database.getCollection(CGO_EDGE_COLLECTION);

        this.nodeCollection.deleteMany(new Document());
        this.edgeCollection.deleteMany(new Document());
    }

    @AfterEach
    public void tearDown() {
        this.nodeCollection.deleteMany(new Document());
        this.edgeCollection.deleteMany(new Document());
        GraphBuilder.getInstance().clear();
    }

    @Test
    public void test_1() {
        MotionFrame motionFrame = newFrame();

        motionFrame.addMotionEvent(
                newEvent("event-1", "ORDER_CREATED", "CREATED"));
        motionFrame.addMotionEvent(
                newEvent("event-2", "ORDER_UPDATED", "UPDATED"));
        motionFrame.addMotionEvent(
                newEvent("event-3", "ORDER_SHIPPED", "SHIPPED"));

        Console.log(
                "CGOECESinkIT.input.motionFrame",
                motionFrame.toJson());

        CGOECESink sink = new CGOECESink();
        GraphView graphView = sink.write(motionFrame);

        assertNotNull(graphView);
        assertTrue(graphView instanceof GraphSnapshot);

        GraphSnapshot graphSnapshot = (GraphSnapshot) graphView;

        Console.log(
                "CGOECESinkIT.graph.type",
                graphView.getClass().getName());
        Console.log(
                "CGOECESinkIT.graph.nodeCount",
                String.valueOf(graphSnapshot.nodes().size()));
        Console.log(
                "CGOECESinkIT.graph.edgeCount",
                String.valueOf(graphSnapshot.edges().size()));

        List<String> nodeIds =
                new ArrayList<String>(graphSnapshot.nodes().keySet());
        List<String> edgeIds =
                new ArrayList<String>(graphSnapshot.edges().keySet());

        Collections.sort(nodeIds);
        Collections.sort(edgeIds);

        for (String nodeId : nodeIds) {
            Fact fact = graphSnapshot.nodes().get(nodeId);

            Console.log("CGOECESinkIT.graph.node.id", fact.getId());
            Console.log("CGOECESinkIT.graph.node.mode", fact.getMode());
            Console.log("CGOECESinkIT.graph.node.text", fact.getText());
        }

        for (String edgeId : edgeIds) {
            Edge edge = graphSnapshot.edges().get(edgeId);

            Console.log("CGOECESinkIT.graph.edge.id", edge.getId());
            Console.log("CGOECESinkIT.graph.edge.mode", edge.getMode());
            Console.log("CGOECESinkIT.graph.edge.from", edge.getFromFactId());
            Console.log("CGOECESinkIT.graph.edge.to", edge.getToFactId());
            Console.log("CGOECESinkIT.graph.edge.text", edge.getText());
        }

        assertEquals(4, graphSnapshot.nodes().size());
        assertEquals(3, graphSnapshot.edges().size());

        assertNode(graphSnapshot, "MotionFrame:frame-1");
        assertNode(graphSnapshot, "MotionEvent:event-1");
        assertNode(graphSnapshot, "MotionEvent:event-2");
        assertNode(graphSnapshot, "MotionEvent:event-3");

        assertEdge(
                graphSnapshot,
                "Edge:MotionFrame:frame-1->MotionEvent:event-1",
                "MotionEvent:event-1");
        assertEdge(
                graphSnapshot,
                "Edge:MotionFrame:frame-1->MotionEvent:event-2",
                "MotionEvent:event-2");
        assertEdge(
                graphSnapshot,
                "Edge:MotionFrame:frame-1->MotionEvent:event-3",
                "MotionEvent:event-3");

        Console.log("CGOECESinkIT.mongo.database", CGO_DATABASE);
        Console.log(
                "CGOECESinkIT.mongo.nodeCollection",
                CGO_NODE_COLLECTION);
        Console.log(
                "CGOECESinkIT.mongo.edgeCollection",
                CGO_EDGE_COLLECTION);

        long persistedNodeCount = this.nodeCollection.countDocuments();
        long persistedEdgeCount = this.edgeCollection.countDocuments();

        Console.log(
                "CGOECESinkIT.mongo.nodeCount",
                String.valueOf(persistedNodeCount));
        Console.log(
                "CGOECESinkIT.mongo.edgeCount",
                String.valueOf(persistedEdgeCount));

        List<Document> persistedNodes = new ArrayList<Document>();
        for (Document document : this.nodeCollection.find()) {
            persistedNodes.add(document);
        }

        List<Document> persistedEdges = new ArrayList<Document>();
        for (Document document : this.edgeCollection.find()) {
            persistedEdges.add(document);
        }

        Collections.sort(
                persistedNodes,
                new Comparator<Document>() {
                    @Override
                    public int compare(Document left, Document right) {
                        return left.getString("factId")
                                .compareTo(right.getString("factId"));
                    }
                });
        Collections.sort(
                persistedEdges,
                new Comparator<Document>() {
                    @Override
                    public int compare(Document left, Document right) {
                        return left.getString("edgeId")
                                .compareTo(right.getString("edgeId"));
                    }
                });

        for (Document document : persistedNodes) {
            Console.log(
                    "CGOECESinkIT.mongo.node",
                    document.toJson());
        }

        for (Document document : persistedEdges) {
            Console.log(
                    "CGOECESinkIT.mongo.edge",
                    document.toJson());
        }

        assertEquals(4L, persistedNodeCount);
        assertEquals(3L, persistedEdgeCount);

        assertPersistedNode("MotionFrame:frame-1");
        assertPersistedNode("MotionEvent:event-1");
        assertPersistedNode("MotionEvent:event-2");
        assertPersistedNode("MotionEvent:event-3");

        assertPersistedEdge(
                "Edge:MotionFrame:frame-1->MotionEvent:event-1",
                "MotionEvent:event-1");
        assertPersistedEdge(
                "Edge:MotionFrame:frame-1->MotionEvent:event-2",
                "MotionEvent:event-2");
        assertPersistedEdge(
                "Edge:MotionFrame:frame-1->MotionEvent:event-3",
                "MotionEvent:event-3");

        for (Document document : persistedEdges) {
            assertFalse(
                    document.getString("fromFactId")
                            .startsWith("MotionEvent:"));
        }
    }

    private void assertNode(
            GraphSnapshot graphSnapshot,
            String nodeId) {
        Fact fact = graphSnapshot.nodes().get(nodeId);

        assertNotNull(fact);
        assertEquals(nodeId, fact.getId());
        assertEquals("atomic", fact.getMode());
    }

    private void assertEdge(
            GraphSnapshot graphSnapshot,
            String edgeId,
            String toFactId) {
        Edge edge = graphSnapshot.edges().get(edgeId);

        assertNotNull(edge);
        assertEquals(edgeId, edge.getId());
        assertEquals("relational", edge.getMode());
        assertEquals("MotionFrame:frame-1", edge.getFromFactId());
        assertEquals(toFactId, edge.getToFactId());
    }

    private void assertPersistedNode(String factId) {
        Document document =
                this.nodeCollection.find(eq("factId", factId)).first();

        assertNotNull(document);
        assertEquals(factId, document.getString("id"));
        assertEquals("atomic", document.getString("mode"));
    }

    private void assertPersistedEdge(
            String edgeId,
            String toFactId) {
        Document document =
                this.edgeCollection.find(eq("edgeId", edgeId)).first();

        assertNotNull(document);
        assertEquals(edgeId, document.getString("id"));
        assertEquals("relational", document.getString("mode"));
        assertEquals(
                "MotionFrame:frame-1",
                document.getString("fromFactId"));
        assertEquals(toFactId, document.getString("toFactId"));
    }

    private MotionFrame newFrame() {
        MotionFrame motionFrame = new MotionFrame();

        motionFrame.setFrameId("frame-1");
        motionFrame.setFrameType("ORDER_OPERATION_FRAME");

        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-09-12T10:00:00Z");
        timeWindow.setWindowEnd("2026-09-12T10:05:00Z");
        motionFrame.setTimeWindow(timeWindow);

        motionFrame.setSequence("1");
        motionFrame.setStatus("OPEN");
        motionFrame.setMetadataJson("{\"runtime\":\"motion\"}");

        return motionFrame;
    }

    private MotionEvent newEvent(
            String eventId,
            String eventType,
            String operation) {
        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId(eventId);
        motionEvent.setEventType(eventType);
        motionEvent.setOriginTime("2026-09-12T10:01:00Z");
        motionEvent.setReceivedAt("2026-09-12T10:01:01Z");
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
