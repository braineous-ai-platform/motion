package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.infra.kafka.MotionEventEmitter;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionResponseResult;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        RecordingMotionEventNormalizer eventNormalizer =
                new RecordingMotionEventNormalizer();

        orchestrator.eventNormalizer = eventNormalizer;

        RecordingMotionReplayOrchestrator replayOrchestrator =
                new RecordingMotionReplayOrchestrator();

        orchestrator.replayOrchestrator = replayOrchestrator;

        RecordingMotionEventEmitter motionEventEmitter =
                new RecordingMotionEventEmitter();

        orchestrator.motionEventEmitter = motionEventEmitter;

        orchestrator.responseResultBuilder =
                new MotionResponseResultBuilder();

        MotionResponseResult responseResult =
                orchestrator.ingest(motionEnvelope);

        Console.log("motionEnvelope", motionEnvelope.toJson());
        Console.log("responseResult", responseResult.toJson());
        Console.log("replayEvaluationCount", String.valueOf(replayOrchestrator.getInvocationCount()));
        Console.log("emitterInvocationCount", String.valueOf(motionEventEmitter.getInvocationCount()));

        MotionEvent emittedMotionEvent =
                motionEventEmitter.getMotionEvent();

        Console.log("emittedMotionEvent", emittedMotionEvent.toJson());

        assertEquals(1, replayOrchestrator.getInvocationCount());
        assertEquals(1, motionEventEmitter.getInvocationCount());
        assertSame(eventNormalizer.getMotionEvent(), emittedMotionEvent);
        assertSame(replayOrchestrator.getReplaySignal(), emittedMotionEvent.getReplaySignal());
        assertEquals("envelope-1", emittedMotionEvent.getEventId());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", emittedMotionEvent.getEventType());
        assertEquals("2026-01-01T10:15:30Z", emittedMotionEvent.getOccurredAt());
        assertEquals("payment-1001", emittedMotionEvent.getSubjectId());
        assertEquals("PAYMENT", emittedMotionEvent.getSubjectType());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", emittedMotionEvent.getOperation());
        assertEquals("{\"paymentId\":\"payment-1001\"}", emittedMotionEvent.getPayloadJson());
        assertEquals("{\"tenant\":\"tenant-1\"}", emittedMotionEvent.getMetadataJson());
        assertNotNull(emittedMotionEvent.getReplaySignal());
        assertEquals("FAILURE_RECOVERY", emittedMotionEvent.getReplaySignal().getReplayLevel());
        assertEquals("REPLAY_NOT_REQUIRED", emittedMotionEvent.getReplaySignal().getReasonCode());
        assertEquals("Replay is not required for accepted Motion event", emittedMotionEvent.getReplaySignal().getMessage());
        assertEquals("envelope-1", emittedMotionEvent.getReplaySignal().getMotionEventId());

        assertEquals("ACCEPTED", responseResult.getStatus());
        assertEquals("MOTION_EVENT_ACCEPTED", responseResult.getReasonCode());
        assertEquals("Motion event accepted into ingestion pipeline", responseResult.getMessage());

        assertEquals("envelope-1", responseResult.getResultId());
        assertEquals("envelope-1", responseResult.getMotionEventId());
        assertEquals("{\"tenant\":\"tenant-1\"}", responseResult.getMetadataJson());

        MotionEvent motionEvent =
                MotionEvent.fromJson(responseResult.getMotionEventJson(), MotionEvent.class);

        Console.log("motionEvent", motionEvent.toJson());

        assertEquals("envelope-1", motionEvent.getEventId());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", motionEvent.getEventType());
        assertEquals("2026-01-01T10:15:30Z", motionEvent.getOccurredAt());
        assertEquals("payment-1001", motionEvent.getSubjectId());
        assertEquals("PAYMENT", motionEvent.getSubjectType());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", motionEvent.getOperation());
        assertEquals("{\"paymentId\":\"payment-1001\"}", motionEvent.getPayloadJson());
        assertEquals("{\"tenant\":\"tenant-1\"}", motionEvent.getMetadataJson());
        assertNotNull(motionEvent.getReplaySignal());

        assertEquals(
                "FAILURE_RECOVERY",
                motionEvent.getReplaySignal().getReplayLevel()
        );

        assertEquals(
                "REPLAY_NOT_REQUIRED",
                motionEvent.getReplaySignal().getReasonCode()
        );

        assertEquals(
                "Replay is not required for accepted Motion event",
                motionEvent.getReplaySignal().getMessage()
        );

        assertEquals(
                "envelope-1",
                motionEvent.getReplaySignal().getMotionEventId()
        );
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

        orchestrator.motionEventEmitter =
                new RecordingMotionEventEmitter();

        orchestrator.responseResultBuilder =
                new MotionResponseResultBuilder();

        MotionResponseResult responseResult =
                orchestrator.ingest(null);

        Console.log("responseResult", String.valueOf(responseResult));

        assertNull(responseResult);
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

        orchestrator.motionEventEmitter =
                new RecordingMotionEventEmitter();

        orchestrator.responseResultBuilder =
                new MotionResponseResultBuilder();

        MotionResponseResult responseResult =
                orchestrator.ingest(motionEnvelope);

        Console.log("motionEnvelope", motionEnvelope.toJson());
        Console.log("responseResult", String.valueOf(responseResult));

        assertNull(responseResult);
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

        orchestrator.motionEventEmitter =
                new RecordingMotionEventEmitter();

        orchestrator.responseResultBuilder =
                new MotionResponseResultBuilder();

        MotionResponseResult responseResult =
                orchestrator.ingest(motionEnvelope);

        String json =
                responseResult.toJson();

        Console.log("responseResultJson", json);

        MotionResponseResult restored =
                MotionResponseResult.fromJson(json, MotionResponseResult.class);

        Console.log("restoredResponseResult", restored.toJson());

        assertEquals(responseResult.getResultId(), restored.getResultId());
        assertEquals(responseResult.getStatus(), restored.getStatus());
        assertEquals(responseResult.getReasonCode(), restored.getReasonCode());
        assertEquals(responseResult.getMessage(), restored.getMessage());
        assertEquals(responseResult.getMotionEventId(), restored.getMotionEventId());
        assertEquals(responseResult.getMotionEventJson(), restored.getMotionEventJson());
        assertEquals(responseResult.getReplaySignalJson(), restored.getReplaySignalJson());
        assertEquals(responseResult.getMetadataJson(), restored.getMetadataJson());
    }

    @Test
    public void test_5() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-5");
        rawEvent.setSource("payment-system");
        rawEvent.setSourceType("PAYMENT");
        rawEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        rawEvent.setReceivedAt("2026-06-01T10:15:30Z");
        rawEvent.setPayloadJson("{\"paymentId\":\"payment-5001\"}");
        rawEvent.setMetadataJson("{\"source\":\"payment-system\"}");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-5");
        motionEnvelope.setTenantId("tenant-5");
        motionEnvelope.setCorrelationId("payment-5001");
        motionEnvelope.setTraceId("trace-5");
        motionEnvelope.setReceivedAt("2026-06-01T10:15:30Z");
        motionEnvelope.setRawEvent(rawEvent);
        motionEnvelope.setMetadataJson("{\"tenant\":\"tenant-5\"}");

        RuntimeException emissionFailure =
                new RuntimeException("motion event emission failed");

        RecordingMotionEventEmitter motionEventEmitter =
                new RecordingMotionEventEmitter(emissionFailure);

        RecordingMotionResponseResultBuilder responseResultBuilder =
                new RecordingMotionResponseResultBuilder();

        MotionIngestionOrchestrator orchestrator =
                new MotionIngestionOrchestrator();

        orchestrator.validationOrchestrator =
                new MotionValidationOrchestrator();
        orchestrator.eventNormalizer =
                new MotionEventNormalizer();
        orchestrator.replayOrchestrator =
                new MotionReplayOrchestrator();
        orchestrator.motionEventEmitter = motionEventEmitter;
        orchestrator.responseResultBuilder = responseResultBuilder;

        Console.log("admissionFailure", "invoke MotionIngestionOrchestrator");

        RuntimeException actualFailure = assertThrows(
                RuntimeException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        orchestrator.ingest(motionEnvelope);
                    }
                }
        );

        Console.log("emitterInvocationCount", String.valueOf(motionEventEmitter.getInvocationCount()));
        Console.log("responseBuilderInvocationCount", String.valueOf(responseResultBuilder.getInvocationCount()));
        Console.log("propagatedFailure", actualFailure.getMessage());

        assertSame(emissionFailure, actualFailure);
        assertEquals(1, motionEventEmitter.getInvocationCount());
        assertNotNull(motionEventEmitter.getMotionEvent());
        assertNotNull(motionEventEmitter.getMotionEvent().getReplaySignal());
        assertEquals(0, responseResultBuilder.getInvocationCount());

        Console.log("admissionFailureAssertions", "complete");
    }

    private static class RecordingMotionEventNormalizer extends MotionEventNormalizer {

        private MotionEvent motionEvent;

        @Override
        public MotionEvent normalize(MotionEnvelope motionEnvelope) {
            this.motionEvent = super.normalize(motionEnvelope);
            return this.motionEvent;
        }

        public MotionEvent getMotionEvent() {
            return this.motionEvent;
        }
    }

    private static class RecordingMotionReplayOrchestrator extends MotionReplayOrchestrator {

        private int invocationCount;
        private MotionReplaySignal replaySignal;

        @Override
        public MotionReplaySignal evaluate(MotionEvent motionEvent) {
            this.invocationCount++;
            this.replaySignal = super.evaluate(motionEvent);
            return this.replaySignal;
        }

        public int getInvocationCount() {
            return this.invocationCount;
        }

        public MotionReplaySignal getReplaySignal() {
            return this.replaySignal;
        }
    }

    private static class RecordingMotionEventEmitter extends MotionEventEmitter {

        private int invocationCount;
        private MotionEvent motionEvent;
        private RuntimeException emissionFailure;

        public RecordingMotionEventEmitter() {
        }

        public RecordingMotionEventEmitter(RuntimeException emissionFailure) {
            this.emissionFailure = emissionFailure;
        }

        @Override
        public void emit(MotionEvent motionEvent) {
            this.invocationCount++;
            this.motionEvent = motionEvent;

            if (this.emissionFailure != null) {
                throw this.emissionFailure;
            }
        }

        public int getInvocationCount() {
            return this.invocationCount;
        }

        public MotionEvent getMotionEvent() {
            return this.motionEvent;
        }
    }

    private static class RecordingMotionResponseResultBuilder extends MotionResponseResultBuilder {

        private int invocationCount;

        @Override
        public MotionResponseResult build(MotionEvent motionEvent,
                                          MotionReplaySignal replaySignal,
                                          MotionEvent publishedEvent) {
            this.invocationCount++;
            return super.build(motionEvent, replaySignal, publishedEvent);
        }

        public int getInvocationCount() {
            return this.invocationCount;
        }
    }
}
