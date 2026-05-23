package io.braineous.motion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        assertEquals("event-1", event.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", event.getEventType());
        assertEquals("2026-05-22T10:15:30Z", event.getOccurredAt());
        assertEquals("order-1001", event.getSubjectId());
        assertEquals("ORDER", event.getSubjectType());
        assertEquals("UPDATED", event.getOperation());
        assertEquals("{\"status\":\"SHIPPED\"}", event.getPayloadJson());
        assertEquals("{\"transport\":\"kafka\"}", event.getMetadataJson());
    }

    @Test
    public void test_2() {
        MotionEvent event = new MotionEvent();

        assertNotNull(event.toString());
    }
}
