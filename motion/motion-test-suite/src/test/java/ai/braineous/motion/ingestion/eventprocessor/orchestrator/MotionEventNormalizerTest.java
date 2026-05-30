package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionEventNormalizerTest {

    @Test
    public void test_1() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-1");
        rawEvent.setSource("payment-system");
        rawEvent.setSourceType("PAYMENT");
        rawEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        rawEvent.setReceivedAt("2026-01-01T10:15:30Z");
        rawEvent.setPayloadJson("{\"paymentId\":\"payment-1001\"}");
        rawEvent.setMetadataJson("{\"source\":\"payment-system\"}");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-1");
        motionEnvelope.setTenantId("tenant-1");
        motionEnvelope.setCorrelationId("payment-1001");
        motionEnvelope.setTraceId("trace-1");
        motionEnvelope.setReceivedAt("2026-01-01T10:15:30Z");
        motionEnvelope.setRawEvent(rawEvent);
        motionEnvelope.setMetadataJson("{\"tenant\":\"tenant-1\"}");

        MotionEventNormalizer normalizer =
                new MotionEventNormalizer();

        MotionEvent motionEvent =
                normalizer.normalize(motionEnvelope);

        Console.log(
                "motionEnvelope",
                motionEnvelope.toJson());

        Console.log(
                "motionEvent",
                motionEvent.toJson());

        assertNotNull(motionEvent);

        assertEquals(
                "envelope-1",
                motionEvent.getEventId());

        assertEquals(
                "PAYMENT_CAPTURE_REQUESTED",
                motionEvent.getEventType());

        assertEquals(
                "2026-01-01T10:15:30Z",
                motionEvent.getOccurredAt());

        assertEquals(
                "payment-1001",
                motionEvent.getSubjectId());

        assertEquals(
                "PAYMENT",
                motionEvent.getSubjectType());

        assertEquals(
                "PAYMENT_CAPTURE_REQUESTED",
                motionEvent.getOperation());

        assertEquals(
                "{\"paymentId\":\"payment-1001\"}",
                motionEvent.getPayloadJson());

        assertEquals(
                "{\"tenant\":\"tenant-1\"}",
                motionEvent.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionEventNormalizer normalizer =
                new MotionEventNormalizer();

        MotionEvent motionEvent =
                normalizer.normalize(null);

        Console.log(
                "motionEvent",
                motionEvent.toJson());

        assertNotNull(motionEvent);
        assertNotNull(motionEvent.toJson());
    }

    @Test
    public void test_3() {

        MotionEnvelope motionEnvelope =
                new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-2");
        motionEnvelope.setCorrelationId("correlation-2");
        motionEnvelope.setReceivedAt("2026-05-01T12:00:00Z");
        motionEnvelope.setMetadataJson("{\"tenant\":\"tenant-2\"}");

        MotionEventNormalizer normalizer =
                new MotionEventNormalizer();

        MotionEvent motionEvent =
                normalizer.normalize(motionEnvelope);

        String json =
                motionEvent.toJson();

        Console.log(
                "motionEventJson",
                json);

        MotionEvent restored =
                MotionEvent.fromJson(
                        json,
                        MotionEvent.class);

        Console.log(
                "restoredMotionEvent",
                restored.toJson());

        assertEquals(
                motionEvent.getEventId(),
                restored.getEventId());

        assertEquals(
                motionEvent.getEventType(),
                restored.getEventType());

        assertEquals(
                motionEvent.getOccurredAt(),
                restored.getOccurredAt());

        assertEquals(
                motionEvent.getSubjectId(),
                restored.getSubjectId());

        assertEquals(
                motionEvent.getSubjectType(),
                restored.getSubjectType());

        assertEquals(
                motionEvent.getOperation(),
                restored.getOperation());

        assertEquals(
                motionEvent.getPayloadJson(),
                restored.getPayloadJson());

        assertEquals(
                motionEvent.getMetadataJson(),
                restored.getMetadataJson());
    }
}
