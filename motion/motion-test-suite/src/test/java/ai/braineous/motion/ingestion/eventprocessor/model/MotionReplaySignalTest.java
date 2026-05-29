package ai.braineous.motion.ingestion.eventprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionReplaySignalTest {

    @Test
    public void test_1() {

        MotionReplaySignal signal = new MotionReplaySignal();

        signal.setReplaySignalId("replay-signal-1");
        signal.setReplayLevel("PI_BACKTEST");
        signal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
        signal.setMessage("Replay requested for predictive intelligence backtesting");

        signal.setRawEventId("raw-event-1");
        signal.setEnvelopeId("envelope-1");
        signal.setMotionEventId("motion-event-1");
        signal.setCorrelationId("corr-1001");
        signal.setTraceId("trace-1001");

        signal.setTenantId("tenant-1");
        signal.setSource("payment-system");
        signal.setEventType("PAYMENT_CAPTURE_REQUESTED");

        signal.setReplayFromTime("2025-01-01T00:00:00Z");
        signal.setReplayToTime("2025-06-30T23:59:59Z");
        signal.setAsOfTime("2025-01-01T00:00:00Z");
        signal.setTimelineId("timeline-1");

        signal.setTargetTopic("motion.replay.pi.backtest");
        signal.setPartitionKey("tenant-1");

        signal.setRawEventJson("{\"rawEventId\":\"raw-event-1\"}");
        signal.setEnvelopeJson("{\"envelopeId\":\"envelope-1\"}");
        signal.setMotionEventJson("{\"eventId\":\"motion-event-1\"}");
        signal.setCgoStateRefJson("{\"stateRef\":\"cgo-state-as-of-2025-01-01\"}");
        signal.setActualOutcomeJson("{\"outcome\":\"APPROVED\"}");
        signal.setMetadataJson("{\"pipeline\":\"motion-ingestion\"}");

        Console.log("motionReplaySignal", signal.toString());
        Console.log("motionReplaySignalJson", signal.toJson());

        assertEquals("replay-signal-1", signal.getReplaySignalId());
        assertEquals("PI_BACKTEST", signal.getReplayLevel());
        assertEquals("TEMPORAL_REPLAY_REQUESTED", signal.getReasonCode());
        assertEquals("Replay requested for predictive intelligence backtesting", signal.getMessage());

        assertEquals("raw-event-1", signal.getRawEventId());
        assertEquals("envelope-1", signal.getEnvelopeId());
        assertEquals("motion-event-1", signal.getMotionEventId());
        assertEquals("corr-1001", signal.getCorrelationId());
        assertEquals("trace-1001", signal.getTraceId());

        assertEquals("tenant-1", signal.getTenantId());
        assertEquals("payment-system", signal.getSource());
        assertEquals("PAYMENT_CAPTURE_REQUESTED", signal.getEventType());

        assertEquals("2025-01-01T00:00:00Z", signal.getReplayFromTime());
        assertEquals("2025-06-30T23:59:59Z", signal.getReplayToTime());
        assertEquals("2025-01-01T00:00:00Z", signal.getAsOfTime());
        assertEquals("timeline-1", signal.getTimelineId());

        assertEquals("motion.replay.pi.backtest", signal.getTargetTopic());
        assertEquals("tenant-1", signal.getPartitionKey());

        assertEquals("{\"rawEventId\":\"raw-event-1\"}", signal.getRawEventJson());
        assertEquals("{\"envelopeId\":\"envelope-1\"}", signal.getEnvelopeJson());
        assertEquals("{\"eventId\":\"motion-event-1\"}", signal.getMotionEventJson());
        assertEquals("{\"stateRef\":\"cgo-state-as-of-2025-01-01\"}", signal.getCgoStateRefJson());
        assertEquals("{\"outcome\":\"APPROVED\"}", signal.getActualOutcomeJson());
        assertEquals("{\"pipeline\":\"motion-ingestion\"}", signal.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionReplaySignal signal = new MotionReplaySignal();

        Console.log("motionReplaySignal", signal.toString());
        Console.log("motionReplaySignalJson", signal.toJson());

        assertNotNull(signal.toString());
        assertNotNull(signal.toJson());
    }

    @Test
    public void test_3() {

        MotionReplaySignal signal = new MotionReplaySignal();

        signal.setReplaySignalId("replay-signal-2");
        signal.setReplayLevel("FAILURE_RECOVERY");
        signal.setReasonCode("TRANSIENT_PROCESSING_FAILURE");
        signal.setMessage("Replay is allowed from the failed processing boundary");

        signal.setRawEventId("raw-event-2");
        signal.setEnvelopeId("envelope-2");
        signal.setMotionEventId(null);
        signal.setCorrelationId("corr-2001");
        signal.setTraceId("trace-2001");

        signal.setTenantId("tenant-2");
        signal.setSource("order-system");
        signal.setEventType("ORDER_CREATED");

        signal.setReplayFromTime("2026-05-01T10:15:30Z");
        signal.setReplayToTime("2026-05-01T10:15:30Z");
        signal.setAsOfTime("2026-05-01T10:15:30Z");
        signal.setTimelineId("timeline-2");

        signal.setTargetTopic("motion.replay.failure.recovery");
        signal.setPartitionKey("raw-event-2");

        signal.setRawEventJson("{\"rawEventId\":\"raw-event-2\"}");
        signal.setEnvelopeJson("{\"envelopeId\":\"envelope-2\"}");
        signal.setMotionEventJson(null);
        signal.setCgoStateRefJson(null);
        signal.setActualOutcomeJson(null);
        signal.setMetadataJson("{\"stage\":\"validation\"}");

        String json = signal.toJson();

        Console.log("motionReplaySignalJson", json);

        MotionReplaySignal restored = MotionReplaySignal.fromJson(json, MotionReplaySignal.class);

        Console.log("restoredMotionReplaySignal", restored.toString());

        assertEquals("replay-signal-2", restored.getReplaySignalId());
        assertEquals("FAILURE_RECOVERY", restored.getReplayLevel());
        assertEquals("TRANSIENT_PROCESSING_FAILURE", restored.getReasonCode());
        assertEquals("Replay is allowed from the failed processing boundary", restored.getMessage());

        assertEquals("raw-event-2", restored.getRawEventId());
        assertEquals("envelope-2", restored.getEnvelopeId());
        assertEquals(null, restored.getMotionEventId());
        assertEquals("corr-2001", restored.getCorrelationId());
        assertEquals("trace-2001", restored.getTraceId());

        assertEquals("tenant-2", restored.getTenantId());
        assertEquals("order-system", restored.getSource());
        assertEquals("ORDER_CREATED", restored.getEventType());

        assertEquals("2026-05-01T10:15:30Z", restored.getReplayFromTime());
        assertEquals("2026-05-01T10:15:30Z", restored.getReplayToTime());
        assertEquals("2026-05-01T10:15:30Z", restored.getAsOfTime());
        assertEquals("timeline-2", restored.getTimelineId());

        assertEquals("motion.replay.failure.recovery", restored.getTargetTopic());
        assertEquals("raw-event-2", restored.getPartitionKey());

        assertEquals("{\"rawEventId\":\"raw-event-2\"}", restored.getRawEventJson());
        assertEquals("{\"envelopeId\":\"envelope-2\"}", restored.getEnvelopeJson());
        assertEquals(null, restored.getMotionEventJson());
        assertEquals(null, restored.getCgoStateRefJson());
        assertEquals(null, restored.getActualOutcomeJson());
        assertEquals("{\"stage\":\"validation\"}", restored.getMetadataJson());
    }
}