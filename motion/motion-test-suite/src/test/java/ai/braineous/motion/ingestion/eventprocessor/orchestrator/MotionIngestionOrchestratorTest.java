package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MotionIngestionOrchestratorTest {

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

        MotionIngestionOrchestrator orchestrator =
                new MotionIngestionOrchestrator();

        orchestrator.validationOrchestrator =
                new MotionValidationOrchestrator();

        orchestrator.eventNormalizer =
                new MotionEventNormalizer();

        orchestrator.replayOrchestrator =
                new MotionReplayOrchestrator();

        orchestrator.eventPublisher =
                new MotionEventPublisher();

        MotionEvent motionEvent =
                orchestrator.ingest(motionEnvelope);

        Console.log("motionEnvelope", motionEnvelope.toJson());
        Console.log("motionEvent", motionEvent.toJson());

        assertEquals("envelope-1", motionEvent.getEventId());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", motionEvent.getEventType());
        assertEquals("2026-01-01T10:15:30Z", motionEvent.getOccurredAt());
        assertEquals("payment-1001", motionEvent.getSubjectId());
        assertEquals("PAYMENT", motionEvent.getSubjectType());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", motionEvent.getOperation());
        assertEquals("{\"paymentId\":\"payment-1001\"}", motionEvent.getPayloadJson());
        assertEquals("{\"tenant\":\"tenant-1\"}", motionEvent.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionIngestionOrchestrator orchestrator =
                new MotionIngestionOrchestrator();

        orchestrator.validationOrchestrator =
                new MotionValidationOrchestrator();

        orchestrator.eventNormalizer =
                new MotionEventNormalizer();

        orchestrator.replayOrchestrator =
                new MotionReplayOrchestrator();

        orchestrator.eventPublisher =
                new MotionEventPublisher();

        MotionEvent motionEvent =
                orchestrator.ingest(null);

        Console.log("motionEvent", String.valueOf(motionEvent));

        assertNull(motionEvent);
    }

    @Test
    public void test_3() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-2");
        rawEvent.setSource("payment-system");
        rawEvent.setSourceType("PAYMENT");
        rawEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        rawEvent.setPayloadJson("{\"paymentId\":\"payment-2001\"}");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-2");
        motionEnvelope.setCorrelationId("payment-2001");
        motionEnvelope.setRawEvent(rawEvent);

        MotionIngestionOrchestrator orchestrator =
                new MotionIngestionOrchestrator();

        orchestrator.validationOrchestrator =
                new MotionValidationOrchestrator();

        orchestrator.eventNormalizer =
                new MotionEventNormalizer();

        orchestrator.replayOrchestrator =
                new MotionReplayOrchestrator();

        orchestrator.eventPublisher =
                new MotionEventPublisher();

        MotionEvent motionEvent =
                orchestrator.ingest(motionEnvelope);

        Console.log("motionEnvelope", motionEnvelope.toJson());
        Console.log("motionEvent", String.valueOf(motionEvent));

        assertNull(motionEvent);
    }

    @Test
    public void test_4() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-3");
        rawEvent.setSource("order-system");
        rawEvent.setSourceType("ORDER");
        rawEvent.setEventType("ORDER_CREATED");
        rawEvent.setReceivedAt("2026-05-01T12:00:00Z");
        rawEvent.setPayloadJson("{\"orderId\":\"order-3001\"}");
        rawEvent.setMetadataJson("{\"source\":\"order-system\"}");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-3");
        motionEnvelope.setTenantId("tenant-3");
        motionEnvelope.setCorrelationId("order-3001");
        motionEnvelope.setTraceId("trace-3");
        motionEnvelope.setReceivedAt("2026-05-01T12:00:00Z");
        motionEnvelope.setRawEvent(rawEvent);
        motionEnvelope.setMetadataJson("{\"tenant\":\"tenant-3\"}");

        MotionIngestionOrchestrator orchestrator =
                new MotionIngestionOrchestrator();

        orchestrator.validationOrchestrator =
                new MotionValidationOrchestrator();

        orchestrator.eventNormalizer =
                new MotionEventNormalizer();

        orchestrator.replayOrchestrator =
                new MotionReplayOrchestrator();

        orchestrator.eventPublisher =
                new MotionEventPublisher();

        MotionEvent motionEvent =
                orchestrator.ingest(motionEnvelope);

        String json =
                motionEvent.toJson();

        Console.log("motionEventJson", json);

        MotionEvent restored =
                MotionEvent.fromJson(json, MotionEvent.class);

        Console.log("restoredMotionEvent", restored.toJson());

        assertEquals(motionEvent.getEventId(), restored.getEventId());
        assertEquals(motionEvent.getEventType(), restored.getEventType());
        assertEquals(motionEvent.getOccurredAt(), restored.getOccurredAt());
        assertEquals(motionEvent.getSubjectId(), restored.getSubjectId());
        assertEquals(motionEvent.getSubjectType(), restored.getSubjectType());
        assertEquals(motionEvent.getOperation(), restored.getOperation());
        assertEquals(motionEvent.getPayloadJson(), restored.getPayloadJson());
        assertEquals(motionEvent.getMetadataJson(), restored.getMetadataJson());
    }
}
