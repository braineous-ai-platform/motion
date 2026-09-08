package io.braineous.motion.core.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MotionEventTest {

    @Test
    public void test_1() {
        MotionEvent event = new MotionEvent();

        event.setEventId("event-1");
        event.setEventType("ORDER_STATUS_CHANGED");
        event.setOriginTime("2026-05-22T10:15:30Z");
        event.setReceivedAt("2026-05-22T10:15:35Z");
        event.setSubjectId("order-1001");
        event.setSubjectType("ORDER");
        event.setOperation("UPDATED");
        event.setPayloadJson("{\"status\":\"SHIPPED\"}");
        event.setMetadataJson("{\"transport\":\"kafka\"}");

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("PI_BACKTEST");
        replaySignal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
        replaySignal.setMessage("Replay requested for predictive intelligence backtesting");
        event.setReplaySignal(replaySignal);

        Console.log("motionEvent", event.toString());

        assertEquals("event-1", event.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", event.getEventType());
        assertEquals("2026-05-22T10:15:30Z", event.getOriginTime());
        assertEquals("2026-05-22T10:15:35Z", event.getReceivedAt());
        assertEquals("order-1001", event.getSubjectId());
        assertEquals("ORDER", event.getSubjectType());
        assertEquals("UPDATED", event.getOperation());
        assertEquals("{\"status\":\"SHIPPED\"}", event.getPayloadJson());
        assertEquals("{\"transport\":\"kafka\"}", event.getMetadataJson());
        assertNotNull(event.getReplaySignal());
        assertSame(replaySignal, event.getReplaySignal());
        assertEquals("PI_BACKTEST", event.getReplaySignal().getReplayLevel());
        assertEquals("TEMPORAL_REPLAY_REQUESTED", event.getReplaySignal().getReasonCode());
        assertEquals("Replay requested for predictive intelligence backtesting", event.getReplaySignal().getMessage());
    }

    @Test
    public void test_2() {
        MotionEvent event = new MotionEvent();

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        event.setReplaySignal(replaySignal);

        assertNotNull(event.toString());
        assertTrue(event.toString().contains("replaySignal="));

        Console.log("motionEvent", event.toString());
    }

    @Test
    public void test_3() {
        MotionEvent event = new MotionEvent();
        event.setEventId("event-json-1");
        event.setOriginTime("2026-05-22T10:15:30Z");
        event.setReceivedAt("2026-05-22T10:15:35Z");
        event.setPayloadJson("{\"status\":\"SHIPPED\"}");

        Console.log("motionEvent", "serialize both time axes");
        String json = event.toJson();
        MotionEvent restored = MotionEvent.fromJson(json, MotionEvent.class);
        Console.log("motionEventJson", json);

        assertNotNull(restored);
        assertEquals("event-json-1", restored.getEventId());
        assertEquals("2026-05-22T10:15:30Z", restored.getOriginTime());
        assertEquals("2026-05-22T10:15:35Z", restored.getReceivedAt());
        assertEquals("{\"status\":\"SHIPPED\"}", restored.getPayloadJson());
    }

    @Test
    public void test_4() {
        MotionEvent event = new MotionEvent();

        Console.log("motionEvent", "serialize null time axes");
        String json = event.toJson();
        MotionEvent restored = MotionEvent.fromJson(json, MotionEvent.class);
        Console.log("motionEventJson", json);

        assertNotNull(restored);
        org.junit.jupiter.api.Assertions.assertNull(restored.getOriginTime());
        org.junit.jupiter.api.Assertions.assertNull(restored.getReceivedAt());
    }
}
