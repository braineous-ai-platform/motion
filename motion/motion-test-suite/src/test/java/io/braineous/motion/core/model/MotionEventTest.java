package io.braineous.motion.core.model;

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
        event.setOccurredAt("2026-05-22T10:15:30Z");
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

        assertEquals("event-1", event.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", event.getEventType());
        assertEquals("2026-05-22T10:15:30Z", event.getOccurredAt());
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
    }
}
