package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MotionValidationOrchestratorTest {

    @Test
    public void test_1() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-1");
        rawEvent.setSource("payment-system");
        rawEvent.setSourceType("PAYMENT");
        rawEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        rawEvent.setReceivedAt("2026-01-01T10:15:30Z");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-1");
        motionEnvelope.setCorrelationId("payment-1001");
        motionEnvelope.setReceivedAt("2026-01-01T10:15:30Z");
        motionEnvelope.setRawEvent(rawEvent);

        MotionValidationOrchestrator orchestrator =
                new MotionValidationOrchestrator();

        boolean valid =
                orchestrator.validate(motionEnvelope);

        Console.log(
                "motionEnvelope",
                motionEnvelope.toJson());

        Console.log(
                "validationResult",
                String.valueOf(valid));

        assertTrue(valid);
    }

    @Test
    public void test_2() {

        MotionValidationOrchestrator orchestrator =
                new MotionValidationOrchestrator();

        boolean valid =
                orchestrator.validate(null);

        Console.log(
                "validationResult",
                String.valueOf(valid));

        assertFalse(valid);
    }

    @Test
    public void test_3() {

        RawEvent rawEvent = new RawEvent();

        rawEvent.setRawEventId("raw-event-2");
        rawEvent.setSource("payment-system");
        rawEvent.setSourceType("PAYMENT");
        rawEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");

        MotionEnvelope motionEnvelope = new MotionEnvelope();

        motionEnvelope.setEnvelopeId("envelope-2");
        motionEnvelope.setCorrelationId("payment-2001");

        motionEnvelope.setRawEvent(rawEvent);

        MotionValidationOrchestrator orchestrator =
                new MotionValidationOrchestrator();

        boolean valid =
                orchestrator.validate(motionEnvelope);

        Console.log(
                "motionEnvelope",
                motionEnvelope.toJson());

        Console.log(
                "validationResult",
                String.valueOf(valid));

        assertFalse(valid);
    }
}
