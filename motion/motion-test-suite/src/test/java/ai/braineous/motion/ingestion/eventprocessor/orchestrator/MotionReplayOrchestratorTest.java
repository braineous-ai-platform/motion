package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionReplaySignal;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionReplayOrchestratorTest {

    @Test
    public void test_1() {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId("motion-event-1");
        motionEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        motionEvent.setOccurredAt("2026-01-01T10:15:30Z");
        motionEvent.setSubjectId("payment-1001");
        motionEvent.setSubjectType("PAYMENT");
        motionEvent.setOperation("CAPTURE");
        motionEvent.setPayloadJson("{\"paymentId\":\"payment-1001\"}");
        motionEvent.setMetadataJson("{\"source\":\"payment-system\"}");

        MotionReplayOrchestrator orchestrator =
                new MotionReplayOrchestrator();

        MotionReplaySignal replaySignal =
                orchestrator.evaluate(motionEvent);

        Console.log("motionEvent", motionEvent.toJson());
        Console.log("motionReplaySignal", replaySignal.toJson());

        assertNotNull(replaySignal);

        assertEquals("FAILURE_RECOVERY", replaySignal.getReplayLevel());
        assertEquals("REPLAY_NOT_REQUIRED", replaySignal.getReasonCode());
        assertEquals(
                "Replay is not required for accepted Motion event",
                replaySignal.getMessage());

        assertEquals(
                "motion-event-1",
                replaySignal.getMotionEventId());

        assertEquals(
                "PAYMENT_CAPTURE_REQUESTED",
                replaySignal.getEventType());

        assertEquals(
                "2026-01-01T10:15:30Z",
                replaySignal.getReplayFromTime());

        assertEquals(
                "2026-01-01T10:15:30Z",
                replaySignal.getReplayToTime());

        assertEquals(
                "2026-01-01T10:15:30Z",
                replaySignal.getAsOfTime());

        assertEquals(
                motionEvent.toJson(),
                replaySignal.getMotionEventJson());

        assertEquals(
                "{\"source\":\"payment-system\"}",
                replaySignal.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionReplayOrchestrator orchestrator =
                new MotionReplayOrchestrator();

        MotionReplaySignal replaySignal =
                orchestrator.evaluate(null);

        Console.log(
                "motionReplaySignal",
                replaySignal.toJson());

        assertNotNull(replaySignal);

        assertEquals(
                "FAILURE_RECOVERY",
                replaySignal.getReplayLevel());

        assertEquals(
                "REPLAY_NOT_REQUIRED",
                replaySignal.getReasonCode());

        assertEquals(
                "Replay is not required for accepted Motion event",
                replaySignal.getMessage());
    }

    @Test
    public void test_3() {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId("motion-event-2");
        motionEvent.setEventType("ORDER_CREATED");
        motionEvent.setOccurredAt("2026-05-01T12:00:00Z");
        motionEvent.setMetadataJson("{\"tenant\":\"tenant-1\"}");

        MotionReplayOrchestrator orchestrator =
                new MotionReplayOrchestrator();

        MotionReplaySignal replaySignal =
                orchestrator.evaluate(motionEvent);

        String json = replaySignal.toJson();

        Console.log(
                "motionReplaySignalJson",
                json);

        MotionReplaySignal restored =
                MotionReplaySignal.fromJson(
                        json,
                        MotionReplaySignal.class);

        Console.log(
                "restoredMotionReplaySignal",
                restored.toJson());

        assertEquals(
                replaySignal.getReplayLevel(),
                restored.getReplayLevel());

        assertEquals(
                replaySignal.getReasonCode(),
                restored.getReasonCode());

        assertEquals(
                replaySignal.getMessage(),
                restored.getMessage());

        assertEquals(
                replaySignal.getMotionEventId(),
                restored.getMotionEventId());

        assertEquals(
                replaySignal.getEventType(),
                restored.getEventType());

        assertEquals(
                replaySignal.getReplayFromTime(),
                restored.getReplayFromTime());

        assertEquals(
                replaySignal.getReplayToTime(),
                restored.getReplayToTime());

        assertEquals(
                replaySignal.getAsOfTime(),
                restored.getAsOfTime());

        assertEquals(
                replaySignal.getMotionEventJson(),
                restored.getMotionEventJson());

        assertEquals(
                replaySignal.getMetadataJson(),
                restored.getMetadataJson());
    }
}