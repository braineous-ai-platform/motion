package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningRecord;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningStore;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionFrame;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class MotionFrameResolverIT {

    @Inject
    MotionFrameResolver resolver;

    @Inject
    MotionHappeningStore store;

    @Test
    void test_1_resolve_existingRecord_returnsExistingMotionFrame() {

        store.clear();

        MotionFrameRoutingKey routingKey =
                newRoutingKey(
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "ORDER_OPERATION_FRAME");

        MotionHappeningRecord record =
                newRecord(
                        "record-1",
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "frame-existing-1");

        store.addRecord(record);

        MotionFrame frame =
                resolver.resolve(routingKey);

        Console.log("record", record.toString());
        Console.log("routingKey", routingKey.toString());
        Console.log("frame", frame.toString());

        assertNotNull(frame);

        assertEquals(
                "frame-existing-1",
                frame.getFrameId());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                frame.getFrameType());

        assertEquals(
                "OPEN",
                frame.getStatus());

        assertEquals(
                "1",
                frame.getSequence());
    }

    @Test
    void test_2_resolve_missingRecord_createsNewMotionFrame() {

        store.clear();

        MotionFrameRoutingKey routingKey =
                newRoutingKey(
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "ORDER_OPERATION_FRAME");

        MotionFrame frame =
                resolver.resolve(routingKey);

        Console.log("routingKey", routingKey.toString());
        Console.log("frame", frame.toString());

        assertNotNull(frame);

        assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME:frame",
                frame.getFrameId());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                frame.getFrameType());

        assertEquals(
                "1",
                frame.getSequence());

        assertEquals(
                "OPEN",
                frame.getStatus());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                frame.getMetadataJson());
    }

    @Test
    void test_3_resolve_nullRoutingKey_returnsNull() {

        store.clear();

        MotionFrame frame =
                resolver.resolve(null);

        Console.log("routingKey", String.valueOf((Object) null));
        Console.log("frame", String.valueOf(frame));

        assertNull(frame);
    }

    @Test
    void test_4_resolve_nullRoutingKeyValue_returnsNull() {

        store.clear();

        MotionFrameRoutingKey routingKey =
                newRoutingKey(
                        null,
                        "ORDER-1001",
                        "ORDER",
                        "ORDER_OPERATION_FRAME");

        MotionFrame frame =
                resolver.resolve(routingKey);

        Console.log("routingKey", routingKey.toString());
        Console.log("frame", String.valueOf(frame));

        assertNull(frame);
    }

    @Test
    void test_5_resolve_blankRoutingKeyValue_returnsNull() {

        store.clear();

        MotionFrameRoutingKey routingKey =
                newRoutingKey(
                        "   ",
                        "ORDER-1001",
                        "ORDER",
                        "ORDER_OPERATION_FRAME");

        MotionFrame frame =
                resolver.resolve(routingKey);

        Console.log("routingKey", routingKey.toString());
        Console.log("frame", String.valueOf(frame));

        assertNull(frame);
    }

    @Test
    void test_6_resolve_existingRecordWithoutMotionFrame_createsNewMotionFrame() {

        store.clear();

        MotionFrameRoutingKey routingKey =
                newRoutingKey(
                        "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                        "ORDER-1001",
                        "ORDER",
                        "ORDER_OPERATION_FRAME");

        MotionHappeningRecord record =
                new MotionHappeningRecord();

        record.setRecordId("record-1");
        record.setRoutingKey("ORDER-1001:ORDER:ORDER_OPERATION_FRAME");
        record.setSubjectId("ORDER-1001");
        record.setSubjectType("ORDER");
        record.setFrameId("frame-missing-payload");
        record.setFrameType("ORDER_OPERATION_FRAME");
        record.setStatus("OPEN");

        store.addRecord(record);

        MotionFrame frame =
                resolver.resolve(routingKey);

        Console.log("record", record.toString());
        Console.log("routingKey", routingKey.toString());
        Console.log("frame", frame.toString());

        assertNotNull(frame);

        assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME:frame",
                frame.getFrameId());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                frame.getFrameType());

        assertEquals(
                "OPEN",
                frame.getStatus());

        assertEquals(
                "1",
                frame.getSequence());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                frame.getMetadataJson());
    }

    private MotionFrameRoutingKey newRoutingKey(
            String routingKeyValue,
            String subjectId,
            String subjectType,
            String frameType) {

        MotionFrameRoutingKey routingKey =
                new MotionFrameRoutingKey();

        routingKey.setRoutingKey(routingKeyValue);
        routingKey.setSubjectId(subjectId);
        routingKey.setSubjectType(subjectType);
        routingKey.setFrameType(frameType);

        return routingKey;
    }

    private MotionHappeningRecord newRecord(
            String recordId,
            String routingKey,
            String subjectId,
            String subjectType,
            String frameId) {

        MotionFrame frame =
                new MotionFrame();

        frame.setFrameId(frameId);
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-05-22T10:00:00Z");
        frame.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionHappeningRecord record =
                new MotionHappeningRecord();

        record.setRecordId(recordId);
        record.setRoutingKey(routingKey);
        record.setSubjectId(subjectId);
        record.setSubjectType(subjectType);
        record.setFrameId(frameId);
        record.setFrameType("ORDER_OPERATION_FRAME");
        record.setWindowStart("2026-05-22T10:00:00Z");
        record.setWindowEnd("2026-05-22T10:05:00Z");
        record.setSequence("1");
        record.setStatus("OPEN");
        record.setMotionFrame(frame);
        record.setCreatedAt("2026-05-22T10:06:00Z");
        record.setUpdatedAt("2026-05-22T10:07:00Z");

        return record;
    }
}
