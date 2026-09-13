package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class SinkProcessorIT {

    @Inject
    MongoClient mongoClient;

    @Test
    public void test_1() {

        MongoDatabase database = mongoClient.getDatabase("motion_it");
        MongoCollection<Document> collection =
                database.getCollection("operational_view_sink_processor");

        collection.deleteMany(new Document());

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

            MotionEvent event1 = new MotionEvent();
            event1.setEventId("event-1");

            MotionEvent event2 = new MotionEvent();
            event2.setEventId("event-2");

            MotionEvent event3 = new MotionEvent();
            event3.setEventId("event-3");

            motionFrame.addMotionEvent(event1);
            motionFrame.addMotionEvent(event2);
            motionFrame.addMotionEvent(event3);

            Console.log(
                    "SinkProcessorIT.input",
                    motionFrame.toJson());

            sinkProcessor.process(motionFrame);

            long matchingDocuments =
                    collection.countDocuments(eq("viewId", "frame-1"));
            Document persistedDocument =
                    collection.find(eq("viewId", "frame-1")).first();

            assertEquals(1L, matchingDocuments);
            assertNotNull(persistedDocument);

            Console.log(
                    "SinkProcessorIT.output",
                    persistedDocument.toJson());

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

            collection.deleteMany(new Document());

            sinkProcessor.process(null);

            assertEquals(0L, collection.countDocuments());
        } finally {
            collection.deleteMany(new Document());
        }
    }
}
