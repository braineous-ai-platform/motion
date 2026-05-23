package io.braineous.motion.core.model.history;

import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionFrame;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class MotionFrameRecordTest {

    @Test
    public void test_1() {
        MotionFrame frame = new MotionFrame();
        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-05-22T10:00:00Z");
        frame.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionFrameRecord record = new MotionFrameRecord();
        record.setRecordId("record-1");
        record.setContextId("context-1");
        record.setContextType("ORDER_EVOLVING_CONTEXT");
        record.setSubjectId("ORDER-1001");
        record.setSubjectType("ORDER");
        record.setFrameId("frame-1");
        record.setFrameType("ORDER_OPERATION_FRAME");
        record.setWindowStart("2026-05-22T10:00:00Z");
        record.setWindowEnd("2026-05-22T10:05:00Z");
        record.setSequence("1");
        record.setMotionFrame(frame);
        record.setCreatedAt(Instant.parse("2026-05-22T10:06:00Z"));
        record.setUpdatedAt(Instant.parse("2026-05-22T10:07:00Z"));

        assertEquals("record-1", record.getRecordId());
        assertEquals("context-1", record.getContextId());
        assertEquals("ORDER_EVOLVING_CONTEXT", record.getContextType());
        assertEquals("ORDER-1001", record.getSubjectId());
        assertEquals("ORDER", record.getSubjectType());
        assertEquals("frame-1", record.getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", record.getFrameType());
        assertEquals("2026-05-22T10:00:00Z", record.getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", record.getWindowEnd());
        assertEquals("1", record.getSequence());
        assertNotNull(record.getMotionFrame());
        assertEquals("frame-1", record.getMotionFrame().getFrameId());
        assertEquals(Instant.parse("2026-05-22T10:06:00Z"), record.getCreatedAt());
        assertEquals(Instant.parse("2026-05-22T10:07:00Z"), record.getUpdatedAt());

        Console.log("record", record.toString());
    }

    @Test
    public void test_2() {
        MotionFrameRecord record = new MotionFrameRecord();

        assertNull(record.getRecordId());
        assertNull(record.getContextId());
        assertNull(record.getContextType());
        assertNull(record.getSubjectId());
        assertNull(record.getSubjectType());
        assertNull(record.getFrameId());
        assertNull(record.getFrameType());
        assertNull(record.getWindowStart());
        assertNull(record.getWindowEnd());
        assertNull(record.getSequence());
        assertNull(record.getMotionFrame());
        assertNull(record.getCreatedAt());
        assertNull(record.getUpdatedAt());

        Console.log("record", record.toString());
    }
}