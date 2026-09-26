package ai.braineous.motion.perception.support;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.motion.ingestion.eventprocessor.orchestrator.MotionIngestionOrchestrator;

public class PerceptionPipelineTestHarness {

    private final MotionIngestionOrchestrator motionIngestionOrchestrator;

    public PerceptionPipelineTestHarness(
            MotionIngestionOrchestrator motionIngestionOrchestrator) {
        this.motionIngestionOrchestrator = motionIngestionOrchestrator;
    }

    public void send() {
        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("flight-event-001");
        rawEvent.setSource("fno");
        rawEvent.setSourceType("FLIGHT");
        rawEvent.setEventType("FLIGHT_DELAYED");
        rawEvent.setReceivedAt("2026-06-06T12:00:00Z");
        rawEvent.setPayloadJson(
                "{\"flightId\":\"F100\",\"origin\":\"AUS\",\"destination\":\"DFW\","
                        + "\"status\":\"DELAYED\",\"delayMinutes\":45}");
        rawEvent.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("observation-envelope-001");
        motionEnvelope.setTenantId("fno");
        motionEnvelope.setCorrelationId("F100");
        motionEnvelope.setTraceId("observation-trace-001");
        motionEnvelope.setReceivedAt("2026-06-06T12:00:00Z");
        motionEnvelope.setRawEvent(rawEvent);
        motionEnvelope.setMetadataJson("{\"runtime\":\"motion\"}");

        motionIngestionOrchestrator.ingest(motionEnvelope);
    }
}
