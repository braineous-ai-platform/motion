package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionReplaySignal;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionResponseResult;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionResponseResultBuilderTest {

    @Test
    public void test_1() {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId("motion-event-1");
        motionEvent.setEventType("PAYMENT_CAPTURE_REQUESTED");
        motionEvent.setOccurredAt("2026-01-01T10:15:30Z");
        motionEvent.setMetadataJson("{\"tenant\":\"tenant-1\"}");

        MotionReplaySignal replaySignal =
                new MotionReplaySignal();

        replaySignal.setReplayLevel("FAILURE_RECOVERY");
        replaySignal.setReasonCode("REPLAY_NOT_REQUIRED");

        MotionResponseResultBuilder builder =
                new MotionResponseResultBuilder();

        MotionResponseResult result =
                builder.build(
                        motionEvent,
                        replaySignal,
                        motionEvent);

        Console.log("responseResult", result.toJson());

        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("MOTION_EVENT_ACCEPTED", result.getReasonCode());
        assertEquals("Motion event accepted into ingestion pipeline", result.getMessage());

        assertEquals("motion-event-1", result.getResultId());
        assertEquals("motion-event-1", result.getMotionEventId());

        assertNotNull(result.getMotionEventJson());
        assertNotNull(result.getReplaySignalJson());

        assertEquals("{\"tenant\":\"tenant-1\"}",
                result.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionResponseResultBuilder builder =
                new MotionResponseResultBuilder();

        MotionResponseResult result =
                builder.build(
                        null,
                        null,
                        null);

        Console.log("responseResult", result.toJson());

        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("MOTION_EVENT_ACCEPTED", result.getReasonCode());
        assertEquals("Motion event accepted into ingestion pipeline", result.getMessage());
    }

    @Test
    public void test_3() {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId("motion-event-3");
        motionEvent.setEventType("ORDER_CREATED");
        motionEvent.setOccurredAt("2026-05-01T12:00:00Z");
        motionEvent.setMetadataJson("{\"tenant\":\"tenant-3\"}");

        MotionReplaySignal replaySignal =
                new MotionReplaySignal();

        replaySignal.setReplayLevel("FAILURE_RECOVERY");
        replaySignal.setReasonCode("REPLAY_NOT_REQUIRED");

        MotionResponseResultBuilder builder =
                new MotionResponseResultBuilder();

        MotionResponseResult result =
                builder.build(
                        motionEvent,
                        replaySignal,
                        motionEvent);

        String json =
                result.toJson();

        Console.log("responseResultJson", json);

        MotionResponseResult restored =
                MotionResponseResult.fromJson(
                        json,
                        MotionResponseResult.class);

        Console.log("restoredResponseResult",
                restored.toJson());

        assertEquals(result.getResultId(),
                restored.getResultId());

        assertEquals(result.getStatus(),
                restored.getStatus());

        assertEquals(result.getReasonCode(),
                restored.getReasonCode());

        assertEquals(result.getMotionEventId(),
                restored.getMotionEventId());

        assertEquals(result.getMetadataJson(),
                restored.getMetadataJson());

        assertEquals(result.getReplaySignalJson(),
                restored.getReplaySignalJson());
    }
}