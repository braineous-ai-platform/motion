package ai.braineous.motion.ingestion.timeprocessor.infra.kafka;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.motion.ingestion.timeprocessor.orchestrator.TimeProcessorOrchestrator;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonSyntaxException;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeProcessorConsumerTest {

    @Test
    public void test_1() {
        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId("motion-event-1");
        motionEvent.setEventType("ORDER_STATUS_CHANGED");
        motionEvent.setOriginTime("2026-09-07T16:30:00Z");
        motionEvent.setSubjectId("order-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation("UPDATED");
        motionEvent.setPayloadJson("{\"status\":\"SHIPPED\"}");
        motionEvent.setMetadataJson("{\"source\":\"time-processor-consumer-test\"}");

        Console.log("event created", motionEvent.toString());

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("PI_BACKTEST");
        replaySignal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
        replaySignal.setMessage("Replay requested for Time Processor testing");
        replaySignal.setMotionEventId("motion-event-1");
        motionEvent.setReplaySignal(replaySignal);

        Console.log("replay attached", replaySignal.toString());

        String payload = motionEvent.toJson();

        Console.log("payload serialized", payload);

        RecordingTimeProcessorOrchestrator orchestrator =
                new RecordingTimeProcessorOrchestrator();
        TimeProcessorConsumer consumer = new TimeProcessorConsumer();
        consumer.timeProcessorOrchestrator = orchestrator;

        consumer.consume(payload);

        Console.log("consumer invoked", payload);

        MotionEvent capturedMotionEvent = orchestrator.getCapturedMotionEvent();

        Console.log("orchestrator invocation captured", capturedMotionEvent.toString());

        assertEquals(1, orchestrator.getInvocationCount());
        assertNotNull(capturedMotionEvent);
        assertEquals("motion-event-1", capturedMotionEvent.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", capturedMotionEvent.getEventType());
        assertEquals("2026-09-07T16:30:00Z", capturedMotionEvent.getOriginTime());
        assertEquals("order-1001", capturedMotionEvent.getSubjectId());
        assertEquals("ORDER", capturedMotionEvent.getSubjectType());
        assertEquals("UPDATED", capturedMotionEvent.getOperation());
        assertEquals("{\"status\":\"SHIPPED\"}", capturedMotionEvent.getPayloadJson());
        assertEquals(
                "{\"source\":\"time-processor-consumer-test\"}",
                capturedMotionEvent.getMetadataJson());
        assertNotNull(capturedMotionEvent.getReplaySignal());

        MotionReplaySignal capturedReplaySignal = capturedMotionEvent.getReplaySignal();

        Console.log("replay inspected", capturedReplaySignal.toString());

        assertEquals("PI_BACKTEST", capturedReplaySignal.getReplayLevel());
        assertEquals("TEMPORAL_REPLAY_REQUESTED", capturedReplaySignal.getReasonCode());
        assertEquals(
                "Replay requested for Time Processor testing",
                capturedReplaySignal.getMessage());
        assertEquals("motion-event-1", capturedReplaySignal.getMotionEventId());

        Console.log("assertions complete", "TimeProcessorConsumer happy path verified");
    }

    @Test
    public void test_2() {
        RecordingTimeProcessorOrchestrator orchestrator =
                new RecordingTimeProcessorOrchestrator();
        TimeProcessorConsumer consumer = new TimeProcessorConsumer();
        consumer.timeProcessorOrchestrator = orchestrator;

        Console.log("invalid payload", "not-motion-event-json");

        JsonSyntaxException exception = assertThrows(
                JsonSyntaxException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        consumer.consume("not-motion-event-json");
                    }
                });

        Console.log("exception propagated", exception.getMessage());

        assertNotNull(exception);
        assertEquals(0, orchestrator.getInvocationCount());

        Console.log("assertions complete", "invalid payload propagation verified");
    }

    private static class RecordingTimeProcessorOrchestrator
            extends TimeProcessorOrchestrator {

        private MotionEvent capturedMotionEvent;
        private int invocationCount;

        @Override
        public MotionProcessorResult process(MotionEvent motionEvent) {
            capturedMotionEvent = motionEvent;
            invocationCount++;
            return null;
        }

        public MotionEvent getCapturedMotionEvent() {
            return capturedMotionEvent;
        }

        public int getInvocationCount() {
            return invocationCount;
        }
    }
}
