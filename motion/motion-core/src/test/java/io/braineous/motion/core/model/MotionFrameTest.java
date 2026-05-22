package io.braineous.motion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionFrameTest {

    @Test
    public void test_1() {
        MotionFrame frame = new MotionFrame();

        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-05-22T10:00:00Z");
        frame.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setEventsJson("[{\"eventId\":\"event-1\"}]");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        assertEquals("frame-1", frame.getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", frame.getFrameType());
        assertEquals("2026-05-22T10:00:00Z", frame.getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", frame.getWindowEnd());
        assertEquals("1", frame.getSequence());
        assertEquals("OPEN", frame.getStatus());
        assertEquals("[{\"eventId\":\"event-1\"}]", frame.getEventsJson());
        assertEquals("{\"runtime\":\"motion\"}", frame.getMetadataJson());
    }

    @Test
    public void test_2() {
        MotionFrame frame = new MotionFrame();

        assertNotNull(frame.toString());
    }
}