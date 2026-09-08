package ai.braineous.motion.ingestion.timeprocessor.infra.flink;

import ai.braineous.motion.ingestion.timeprocessor.orchestrator.MotionFrameRoutingKeyResolver;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MotionFramePartitionKeySelectorTest {

    @Test
    public void test_1() throws Exception {
        Console.log("create MotionEvent", "ORDER-1001 / ORDER");

        MotionEvent motionEvent = newEvent(
                "event-1",
                "ORDER-1001",
                "ORDER");

        MotionFrameRoutingKeyResolver resolver =
                new MotionFrameRoutingKeyResolver();

        MotionFramePartitionKeySelector selector =
                new MotionFramePartitionKeySelector(resolver);

        String partitionKey = selector.getKey(motionEvent);
        String canonicalKey = resolver.resolve(motionEvent).getRoutingKey();

        Console.log("canonical routing key", canonicalKey);
        Console.log("Flink partition key", partitionKey);

        assertEquals("ORDER-1001:ORDER:ORDER_OPERATION_FRAME", partitionKey);
        assertEquals(canonicalKey, partitionKey);

        Console.log("assertions complete", partitionKey);
    }

    @Test
    public void test_2() throws Exception {
        Console.log("create MotionEvents", "distinct Motion identities");

        MotionEvent orderEvent = newEvent(
                "shared-event-id",
                "ORDER-1001",
                "ORDER");

        MotionEvent paymentEvent = newEvent(
                "shared-event-id",
                "PAYMENT-1001",
                "PAYMENT");

        MotionFramePartitionKeySelector selector =
                new MotionFramePartitionKeySelector(
                        new MotionFrameRoutingKeyResolver());

        String orderKey = selector.getKey(orderEvent);
        String paymentKey = selector.getKey(paymentEvent);

        Console.log("order partition key", orderKey);
        Console.log("payment partition key", paymentKey);

        assertEquals("ORDER-1001:ORDER:ORDER_OPERATION_FRAME", orderKey);
        assertEquals("PAYMENT-1001:PAYMENT:PAYMENT_OPERATION_FRAME", paymentKey);
        assertNotEquals(orderKey, paymentKey);

        Console.log("assertions complete", "partition identities differ");
    }

    @Test
    public void test_3() throws Exception {
        Console.log("create invalid MotionEvents", "null and blank routing identity");

        MotionEvent nullSubjectId = newEvent("event-1", null, "ORDER");
        MotionEvent blankSubjectType = newEvent("event-2", "ORDER-1001", "   ");

        MotionFramePartitionKeySelector selector =
                new MotionFramePartitionKeySelector(
                        new MotionFrameRoutingKeyResolver());

        String nullEventKey = selector.getKey(null);
        String nullSubjectIdKey = selector.getKey(nullSubjectId);
        String blankSubjectTypeKey = selector.getKey(blankSubjectType);

        Console.log("null event key", String.valueOf(nullEventKey));
        Console.log("null subject key", String.valueOf(nullSubjectIdKey));
        Console.log("blank subject type key", String.valueOf(blankSubjectTypeKey));

        assertNull(nullEventKey);
        assertNull(nullSubjectIdKey);
        assertNull(blankSubjectTypeKey);

        Console.log("assertions complete", "resolver rejection semantics preserved");
    }

    private MotionEvent newEvent(
            String eventId,
            String subjectId,
            String subjectType) {

        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId(eventId);
        motionEvent.setEventType("SUBJECT_UPDATED");
        motionEvent.setOriginTime("2026-09-07T19:00:00Z");
        motionEvent.setSubjectId(subjectId);
        motionEvent.setSubjectType(subjectType);
        motionEvent.setOperation("UPDATE");
        motionEvent.setPayloadJson("{\"status\":\"UPDATED\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"motion\"}");
        return motionEvent;
    }
}
