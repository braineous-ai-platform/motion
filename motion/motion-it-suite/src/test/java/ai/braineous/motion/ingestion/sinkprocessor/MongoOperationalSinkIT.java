package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.observe.Console;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class MongoOperationalSinkIT {

    @Inject
    MongoClient mongoClient;

    @Test
    void test_1() {

        MongoDatabase database = mongoClient.getDatabase("motion_it");
        MongoCollection<Document> collection =
                database.getCollection("operational_view_roundtrip");

        collection.deleteMany(new Document());

        try {
            Map<String, Object> state = new HashMap<String, Object>();
            state.put("status", "ACTIVE");

            List<String> sourceFrameIds = new ArrayList<String>();
            sourceFrameIds.add("frame-1");

            List<String> sourceEventIds = new ArrayList<String>();
            sourceEventIds.add("event-1");
            sourceEventIds.add("event-2");

            OperationalView operationalView = new OperationalView();
            operationalView.setViewId("view-1");
            operationalView.setRoutingKey(
                    "subject-1:subject-type:subject-type_OPERATION_FRAME");
            operationalView.setContextId("context-1");
            operationalView.setWindowStart("2026-09-12T10:00:00Z");
            operationalView.setWindowEnd("2026-09-12T11:00:00Z");
            operationalView.setState(state);
            operationalView.setSourceFrameIds(sourceFrameIds);
            operationalView.setSourceEventIds(sourceEventIds);
            operationalView.setMaterializedAt("2026-09-12T11:00:01Z");

            MongoOperationalSink sink =
                    new MongoOperationalSink(
                            mongoClient,
                            "motion_it",
                            "operational_view_roundtrip");

            sink.write(operationalView);

            long matchingDocuments =
                    collection.countDocuments(eq("viewId", "view-1"));
            Document persistedDocument =
                    collection.find(eq("viewId", "view-1")).first();
            Document expectedDocument =
                    Document.parse(operationalView.toJson());

            assertEquals(1L, matchingDocuments);
            assertNotNull(persistedDocument);

            Console.log(
                    "storedOperationalView",
                    persistedDocument.toJson());

            assertEquals("view-1", persistedDocument.getString("viewId"));
            assertEquals(
                    "subject-1:subject-type:subject-type_OPERATION_FRAME",
                    persistedDocument.getString("routingKey"));
            assertEquals(
                    "context-1",
                    persistedDocument.getString("contextId"));
            assertEquals(
                    "2026-09-12T10:00:00Z",
                    persistedDocument.getString("windowStart"));
            assertEquals(
                    "2026-09-12T11:00:00Z",
                    persistedDocument.getString("windowEnd"));
            assertEquals(
                    expectedDocument.get("state"),
                    persistedDocument.get("state"));
            assertEquals(
                    expectedDocument.get("sourceFrameIds"),
                    persistedDocument.get("sourceFrameIds"));
            assertEquals(
                    expectedDocument.get("sourceEventIds"),
                    persistedDocument.get("sourceEventIds"));
            assertEquals(
                    "2026-09-12T11:00:01Z",
                    persistedDocument.getString("materializedAt"));
        } finally {
            collection.deleteMany(new Document());
        }
    }
}
