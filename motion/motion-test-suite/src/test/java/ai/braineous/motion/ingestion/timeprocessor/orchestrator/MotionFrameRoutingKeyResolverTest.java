package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MotionFrameRoutingKeyResolverTest {

    @Test
    void test_1_resolve_validMotionEvent_returnsRoutingKey() {

        MotionEvent event = newEvent("ORDER-1001", "ORDER");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", routingKey.toString());

        assertNotNull(routingKey);

        assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                routingKey.getRoutingKey());

        assertEquals(
                "ORDER-1001",
                routingKey.getSubjectId());

        assertEquals(
                "ORDER",
                routingKey.getSubjectType());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                routingKey.getFrameType());
    }

    @Test
    void test_2_resolve_nullMotionEvent_returnsNull() {

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(null);

        Console.log("routingKey", String.valueOf(routingKey));

        assertNull(routingKey);
    }

    @Test
    void test_3_resolve_nullSubjectId_returnsNull() {

        MotionEvent event = newEvent(null, "ORDER");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", String.valueOf(routingKey));

        assertNull(routingKey);
    }

    @Test
    void test_4_resolve_blankSubjectId_returnsNull() {

        MotionEvent event = newEvent("   ", "ORDER");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", String.valueOf(routingKey));

        assertNull(routingKey);
    }

    @Test
    void test_5_resolve_nullSubjectType_returnsNull() {

        MotionEvent event = newEvent("ORDER-1001", null);

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", String.valueOf(routingKey));

        assertNull(routingKey);
    }

    @Test
    void test_6_resolve_blankSubjectType_returnsNull() {

        MotionEvent event = newEvent("ORDER-1001", "   ");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", String.valueOf(routingKey));

        assertNull(routingKey);
    }

    @Test
    void test_7_resolve_trimmedSubjectValues_returnsTrimmedRoutingKey() {

        MotionEvent event = newEvent("  ORDER-1001  ", "  ORDER  ");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", routingKey.toString());

        assertNotNull(routingKey);

        assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                routingKey.getRoutingKey());

        assertEquals(
                "ORDER-1001",
                routingKey.getSubjectId());

        assertEquals(
                "ORDER",
                routingKey.getSubjectType());

        assertEquals(
                "ORDER_OPERATION_FRAME",
                routingKey.getFrameType());
    }

    @Test
    void test_8_resolve_differentSubjectType_returnsDifferentFrameType() {

        MotionEvent event = newEvent("PAYMENT-1001", "PAYMENT");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFrameRoutingKey routingKey =
                resolver.resolve(event);

        Console.log("event", event.toString());
        Console.log("routingKey", routingKey.toString());

        assertNotNull(routingKey);

        assertEquals(
                "PAYMENT-1001:PAYMENT:PAYMENT_OPERATION_FRAME",
                routingKey.getRoutingKey());

        assertEquals(
                "PAYMENT_OPERATION_FRAME",
                routingKey.getFrameType());
    }

    private MotionEvent newEvent(
            String subjectId,
            String subjectType) {

        MotionEvent event = new MotionEvent();
        event.setEventId("event-1");
        event.setEventType("ORDER_UPDATED");
        event.setOccurredAt("2026-06-06T12:00:00Z");
        event.setSubjectId(subjectId);
        event.setSubjectType(subjectType);
        event.setOperation("UPDATE");
        event.setPayloadJson("{\"status\":\"UPDATED\"}");
        event.setMetadataJson("{\"runtime\":\"motion\"}");

        return event;
    }
}