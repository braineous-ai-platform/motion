package io.braineous.motion.core.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionFrameTest {

    @Test
    public void test_1() {

        MotionFrame frame = new MotionFrame();

        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-05-22T10:00:00Z");
        timeWindow.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setTimeWindow(timeWindow);
        frame.setSequence("1");
        frame.setStatus("OPEN");

        MotionEvent motionEvent = new MotionEvent();
        frame.addMotionEvent(motionEvent);

        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        Console.log("frame", frame.toString());

        assertEquals("frame-1", frame.getFrameId());
        assertEquals("ORDER_OPERATION_FRAME", frame.getFrameType());
        assertNotNull(frame.getTimeWindow());
        assertEquals("2026-05-22T10:00:00Z", frame.getTimeWindow().getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", frame.getTimeWindow().getWindowEnd());
        assertEquals("1", frame.getSequence());
        assertEquals("OPEN", frame.getStatus());

        assertNotNull(frame.getMotionEvents());
        assertEquals(1, frame.getMotionEvents().size());

        assertEquals("{\"runtime\":\"motion\"}", frame.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionFrame frame = new MotionFrame();

        Console.log("frame", frame.toString());

        assertNotNull(frame.toString());
        assertNotNull(frame.getMotionEvents());
        assertEquals(0, frame.getMotionEvents().size());
    }

    @Test
    public void test_3() {
        MotionFrame frame = new MotionFrame();
        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-05-22T10:00:00Z");
        timeWindow.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setTimeWindow(timeWindow);

        MotionEvent event = new MotionEvent();
        event.setEventId("event-1");
        frame.addMotionEvent(event);

        Console.log("frame", "serialize nested MotionTimeWindow");
        String json = frame.toJson();
        MotionFrame restored = MotionFrame.fromJson(json, MotionFrame.class);
        Console.log("frameJson", json);

        assertNotNull(restored);
        assertNotNull(restored.getTimeWindow());
        assertEquals("2026-05-22T10:00:00Z", restored.getTimeWindow().getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", restored.getTimeWindow().getWindowEnd());
        assertNotNull(restored.getMotionEvents());
        assertEquals(1, restored.getMotionEvents().size());
        assertEquals("event-1", restored.getMotionEvents().get(0).getEventId());
    }
}
