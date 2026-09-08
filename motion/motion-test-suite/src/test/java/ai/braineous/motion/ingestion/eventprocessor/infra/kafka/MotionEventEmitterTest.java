package ai.braineous.motion.ingestion.eventprocessor.infra.kafka;

import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MotionEventEmitterTest {

    @Test
    public void test_1() throws InterruptedException {
        Console.log("create MotionEvent", "creating representative canonical MotionEvent");

        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId("motion-event-1");
        motionEvent.setEventType("ORDER_STATUS_CHANGED");
        motionEvent.setOriginTime("2026-05-22T10:15:30Z");
        motionEvent.setSubjectId("order-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation("UPDATED");
        motionEvent.setPayloadJson("{\"status\":\"SHIPPED\"}");
        motionEvent.setMetadataJson("{\"transport\":\"kafka\"}");

        Console.log("attach MotionReplaySignal", "attaching representative replay signal");

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("PI_BACKTEST");
        replaySignal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
        replaySignal.setMessage("Replay requested for predictive intelligence backtesting");
        replaySignal.setMotionEventId("motion-event-1");
        motionEvent.setReplaySignal(replaySignal);

        Console.log("create/inject emitter", "creating isolated emitter mock");

        RecordingEmitter emitter = new RecordingEmitter();
        MotionEventEmitter motionEventEmitter = new MotionEventEmitter();
        motionEventEmitter.emitter = emitter;

        Console.log("invoke MotionEventEmitter", "emitting canonical MotionEvent");

        AtomicReference<Throwable> emissionFailure = new AtomicReference<Throwable>();

        Thread emissionThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            motionEventEmitter.emit(motionEvent);
                        } catch (Throwable failure) {
                            emissionFailure.set(failure);
                        }
                    }
                }
        );

        emissionThread.start();

        assertTrue(emitter.awaitSend());
        assertTrue(emissionThread.isAlive());

        Console.log("send completion", "acknowledging successful send");

        emitter.completeSend();
        emissionThread.join(5000L);

        assertFalse(emissionThread.isAlive());
        assertNull(emissionFailure.get());

        String emittedJson = emitter.getEmittedJson();

        Console.log("capture emitted JSON", emittedJson);

        assertEquals(1, emitter.getEmissionCount());
        assertNotNull(emittedJson);
        assertFalse(emittedJson.isEmpty());
        assertEquals(motionEvent.toJson(), emittedJson);

        MotionEvent restoredMotionEvent = MotionEvent.fromJson(
                emittedJson,
                MotionEvent.class);

        Console.log("deserialize emitted MotionEvent", restoredMotionEvent.toString());

        assertEquals("motion-event-1", restoredMotionEvent.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", restoredMotionEvent.getEventType());
        assertEquals("2026-05-22T10:15:30Z", restoredMotionEvent.getOriginTime());
        assertEquals("order-1001", restoredMotionEvent.getSubjectId());
        assertEquals("ORDER", restoredMotionEvent.getSubjectType());
        assertEquals("UPDATED", restoredMotionEvent.getOperation());
        assertEquals("{\"status\":\"SHIPPED\"}", restoredMotionEvent.getPayloadJson());
        assertEquals("{\"transport\":\"kafka\"}", restoredMotionEvent.getMetadataJson());
        assertNotNull(restoredMotionEvent.getReplaySignal());

        MotionReplaySignal restoredReplaySignal = restoredMotionEvent.getReplaySignal();

        Console.log("inspect replay signal", restoredReplaySignal.toString());

        assertEquals("PI_BACKTEST", restoredReplaySignal.getReplayLevel());
        assertEquals("TEMPORAL_REPLAY_REQUESTED", restoredReplaySignal.getReasonCode());
        assertEquals(
                "Replay requested for predictive intelligence backtesting",
                restoredReplaySignal.getMessage());
        assertEquals("motion-event-1", restoredReplaySignal.getMotionEventId());

        Console.log("assert completed", "MotionEventEmitter unit test completed");
    }

    @Test
    public void test_2() {
        Console.log("create MotionEvent", "creating event for failed send completion");

        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId("motion-event-failure-1");

        RuntimeException sendFailure =
                new RuntimeException("Kafka acknowledgement failed");

        RecordingEmitter emitter = new RecordingEmitter();
        emitter.failSend(sendFailure);

        MotionEventEmitter motionEventEmitter = new MotionEventEmitter();
        motionEventEmitter.emitter = emitter;

        Console.log("invoke MotionEventEmitter", "emitting with exceptional send completion");

        RuntimeException actualFailure = assertThrows(
                RuntimeException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        motionEventEmitter.emit(motionEvent);
                    }
                }
        );

        Console.log("send failure", actualFailure.getMessage());

        assertSame(sendFailure, actualFailure);
        assertEquals(1, emitter.getEmissionCount());

        Console.log("assert completed", "failed send propagated without retry");
    }

    private static class RecordingEmitter implements Emitter<String> {

        private String emittedJson;
        private int emissionCount;
        private CompletableFuture<Void> sendCompletion = new CompletableFuture<Void>();
        private CountDownLatch sendLatch = new CountDownLatch(1);

        @Override
        public CompletionStage<Void> send(String payload) {
            emittedJson = payload;
            emissionCount++;
            sendLatch.countDown();
            return sendCompletion;
        }

        @Override
        public <M extends Message<? extends String>> void send(M message) {
            throw new UnsupportedOperationException("Message emission is not used by this test");
        }

        @Override
        public void complete() {
            throw new UnsupportedOperationException("Completion is not used by this test");
        }

        @Override
        public void error(Exception failure) {
            throw new UnsupportedOperationException("Error signaling is not used by this test");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean hasRequests() {
            return true;
        }

        public String getEmittedJson() {
            return emittedJson;
        }

        public int getEmissionCount() {
            return emissionCount;
        }

        public boolean awaitSend() throws InterruptedException {
            return sendLatch.await(5L, TimeUnit.SECONDS);
        }

        public void completeSend() {
            sendCompletion.complete(null);
        }

        public void failSend(RuntimeException failure) {
            sendCompletion.completeExceptionally(failure);
        }
    }
}
