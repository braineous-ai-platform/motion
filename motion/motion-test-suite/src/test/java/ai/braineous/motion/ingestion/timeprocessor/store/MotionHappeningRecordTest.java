package ai.braineous.motion.ingestion.timeprocessor.store;

import io.braineous.motion.core.model.MotionFrame;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MotionHappeningRecordTest {

    @Test
    void test_1_constructor_initialState() {

        MotionHappeningRecord record = new MotionHappeningRecord();

        Console.log("record", record.toString());

        assertNotNull(record);

        assertNull(record.getRecordId());
        assertNull(record.getRoutingKey());

        assertNull(record.getSubjectId());
        assertNull(record.getSubjectType());

        assertNull(record.getFrameId());
        assertNull(record.getFrameType());

        assertNull(record.getWindowStart());
        assertNull(record.getWindowEnd());

        assertNull(record.getSequence());
        assertNull(record.getStatus());

        assertNull(record.getMotionFrame());

        assertNull(record.getCreatedAt());
        assertNull(record.getUpdatedAt());
    }

    @Test
    void test_2_settersAndGetters_roundTrip() {

        MotionHappeningRecord record = new MotionHappeningRecord();

        record.setRecordId("record-1");
        record.setRoutingKey("ORDER-1001:ORDER:ORDER_OPERATION_FRAME");

        record.setSubjectId("ORDER-1001");
        record.setSubjectType("ORDER");

        record.setFrameId("frame-1");
        record.setFrameType("ORDER_OPERATION_FRAME");

        record.setWindowStart("2026-06-06T10:00:00Z");
        record.setWindowEnd("2026-06-06T10:05:00Z");

        record.setSequence("1");
        record.setStatus("OPEN");

        record.setCreatedAt("2026-06-06T10:00:01Z");
        record.setUpdatedAt("2026-06-06T10:01:01Z");

        Console.log("record", record.toString());

        assertEquals("record-1", record.getRecordId());
        assertEquals("ORDER-1001:ORDER:ORDER_OPERATION_FRAME", record.getRoutingKey());

        assertEquals("ORDER-1001", record.getSubjectId());
        assertEquals("ORDER", record.getSubjectType());

        assertEquals("frame-1", record.getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", record.getFrameType());

        assertEquals("2026-06-06T10:00:00Z", record.getWindowStart());
        assertEquals("2026-06-06T10:05:00Z", record.getWindowEnd());

        assertEquals("1", record.getSequence());
        assertEquals("OPEN", record.getStatus());

        assertEquals("2026-06-06T10:00:01Z", record.getCreatedAt());
        assertEquals("2026-06-06T10:01:01Z", record.getUpdatedAt());
    }

    @Test
    void test_3_motionFrame_roundTrip() {

        MotionFrame frame = new MotionFrame();
        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-06-06T10:00:00Z");
        frame.setWindowEnd("2026-06-06T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");

        MotionHappeningRecord record = new MotionHappeningRecord();
        record.setMotionFrame(frame);

        Console.log("frame", frame.toString());
        Console.log("record", record.toString());

        assertNotNull(record.getMotionFrame());
        assertEquals("frame-1", record.getMotionFrame().getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", record.getMotionFrame().getFrameType());
        assertEquals("2026-06-06T10:00:00Z", record.getMotionFrame().getWindowStart());
        assertEquals("2026-06-06T10:05:00Z", record.getMotionFrame().getWindowEnd());
        assertEquals("1", record.getMotionFrame().getSequence());
        assertEquals("OPEN", record.getMotionFrame().getStatus());
    }

    @Test
    void test_4_toString_containsFields() {

        MotionHappeningRecord record = new MotionHappeningRecord();

        record.setRecordId("record-1");
        record.setRoutingKey("routing-key-1");
        record.setSubjectId("ORDER-1001");
        record.setSubjectType("ORDER");
        record.setFrameId("frame-1");
        record.setFrameType("ORDER_OPERATION_FRAME");
        record.setStatus("OPEN");

        String value = record.toString();

        Console.log("toString", value);

        assertNotNull(value);

        assertTrue(value.contains("record-1"));
        assertTrue(value.contains("routing-key-1"));
        assertTrue(value.contains("ORDER-1001"));
        assertTrue(value.contains("ORDER"));
        assertTrue(value.contains("frame-1"));
        assertTrue(value.contains("ORDER_OPERATION_FRAME"));
        assertTrue(value.contains("OPEN"));
    }

    @Test
    void test_5_json_roundTrip() {

        MotionFrame frame = new MotionFrame();
        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");

        MotionHappeningRecord record = new MotionHappeningRecord();

        record.setRecordId("record-1");
        record.setRoutingKey("routing-key-1");

        record.setSubjectId("ORDER-1001");
        record.setSubjectType("ORDER");

        record.setFrameId("frame-1");
        record.setFrameType("ORDER_OPERATION_FRAME");

        record.setSequence("1");
        record.setStatus("OPEN");

        record.setMotionFrame(frame);

        Console.log("record", record.toString());

        String json = record.toJson();

        Console.log("json", json);

        MotionHappeningRecord restored =
                MotionHappeningRecord.fromJson(
                        json,
                        MotionHappeningRecord.class);

        Console.log("restored", restored.toString());

        assertNotNull(restored);

        assertEquals("record-1", restored.getRecordId());
        assertEquals("routing-key-1", restored.getRoutingKey());

        assertEquals("ORDER-1001", restored.getSubjectId());
        assertEquals("ORDER", restored.getSubjectType());

        assertEquals("frame-1", restored.getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", restored.getFrameType());

        assertEquals("1", restored.getSequence());
        assertEquals("OPEN", restored.getStatus());

        assertNotNull(restored.getMotionFrame());
        assertEquals("frame-1", restored.getMotionFrame().getFrameId());
    }
}