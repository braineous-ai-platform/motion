package ai.braineous.motion.ingestion.timeprocessor.infra.kafka;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.motion.ingestion.timeprocessor.orchestrator.TimeProcessorOrchestrator;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestProfile(TimeProcessorConsumerIT.Profile.class)
public class TimeProcessorConsumerIT {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String MOTION_EVENT_TOPIC = "motion_event";

    @Inject
    RecordingTimeProcessorOrchestrator orchestrator;

    @Test
    public void test_1() throws Exception {
        Console.log("Quarkus/Kafka runtime ready", "real incoming connector is active");

        String motionEventId = "time-processor-consumer-it-" + UUID.randomUUID();

        orchestrator.reset(motionEventId);

        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId(motionEventId);
        motionEvent.setEventType("ORDER_STATUS_CHANGED");
        motionEvent.setOccurredAt("2026-09-07T18:00:00Z");
        motionEvent.setSubjectId("order-consumer-it-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation("UPDATED");
        motionEvent.setPayloadJson("{\"status\":\"DELIVERED\"}");
        motionEvent.setMetadataJson("{\"source\":\"time-processor-consumer-it\"}");

        Console.log("event created", motionEvent.toString());

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("PI_BACKTEST");
        replaySignal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
        replaySignal.setMessage("Replay received through the real Kafka consumer");
        replaySignal.setMotionEventId(motionEventId);
        motionEvent.setReplaySignal(replaySignal);

        Console.log("replay signal attached", replaySignal.toString());

        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");

        KafkaProducer<String, String> producer =
                new KafkaProducer<String, String>(producerProperties);

        try {
            Console.log("Kafka producer created", BOOTSTRAP_SERVERS);

            ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>(
                            MOTION_EVENT_TOPIC,
                            motionEventId,
                            motionEvent.toJson());

            producer.send(record).get(10, TimeUnit.SECONDS);
            producer.flush();

            Console.log("record sent", motionEventId);
        } finally {
            producer.close(Duration.ofSeconds(5));
        }

        Console.log("waiting for consumer", motionEventId);

        long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();

        while (!orchestrator.hasReceived(motionEventId)
                && System.nanoTime() < deadline) {
            Thread.sleep(50L);
        }

        MotionEvent capturedMotionEvent = orchestrator.getCapturedMotionEvent();

        Console.log(
                "orchestrator invocation observed",
                String.valueOf(orchestrator.getInvocationCount()));
        Console.log("captured event inspected", String.valueOf(capturedMotionEvent));

        assertEquals(1, orchestrator.getInvocationCount());
        assertNotNull(capturedMotionEvent);
        assertEquals(motionEventId, capturedMotionEvent.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", capturedMotionEvent.getEventType());
        assertEquals("2026-09-07T18:00:00Z", capturedMotionEvent.getOccurredAt());
        assertEquals("order-consumer-it-1001", capturedMotionEvent.getSubjectId());
        assertEquals("ORDER", capturedMotionEvent.getSubjectType());
        assertEquals("UPDATED", capturedMotionEvent.getOperation());
        assertEquals("{\"status\":\"DELIVERED\"}", capturedMotionEvent.getPayloadJson());
        assertEquals(
                "{\"source\":\"time-processor-consumer-it\"}",
                capturedMotionEvent.getMetadataJson());
        assertNotNull(capturedMotionEvent.getReplaySignal());

        MotionReplaySignal capturedReplaySignal = capturedMotionEvent.getReplaySignal();

        Console.log("replay inspected", capturedReplaySignal.toString());

        assertEquals("PI_BACKTEST", capturedReplaySignal.getReplayLevel());
        assertEquals("TEMPORAL_REPLAY_REQUESTED", capturedReplaySignal.getReasonCode());
        assertEquals(
                "Replay received through the real Kafka consumer",
                capturedReplaySignal.getMessage());
        assertEquals(motionEventId, capturedReplaySignal.getMotionEventId());

        Console.log("assertions complete", motionEventId);
    }

    public static class Profile implements QuarkusTestProfile {

        @Override
        public Set<Class<?>> getEnabledAlternatives() {
            return Set.<Class<?>>of(RecordingTimeProcessorOrchestrator.class);
        }
    }
}

@Alternative
@ApplicationScoped
class RecordingTimeProcessorOrchestrator extends TimeProcessorOrchestrator {

    private final AtomicInteger invocationCount = new AtomicInteger();
    private volatile MotionEvent capturedMotionEvent;
    private volatile String expectedMotionEventId;

    @Override
    public MotionProcessorResult process(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return null;
        }

        if (expectedMotionEventId == null) {
            return null;
        }

        if (!expectedMotionEventId.equals(motionEvent.getEventId())) {
            return null;
        }

        capturedMotionEvent = motionEvent;
        invocationCount.incrementAndGet();
        return null;
    }

    public void reset(String expectedMotionEventId) {
        this.expectedMotionEventId = expectedMotionEventId;
        capturedMotionEvent = null;
        invocationCount.set(0);
    }

    public boolean hasReceived(String motionEventId) {
        MotionEvent current = capturedMotionEvent;

        if (current == null) {
            return false;
        }

        return motionEventId.equals(current.getEventId());
    }

    public MotionEvent getCapturedMotionEvent() {
        return capturedMotionEvent;
    }

    public int getInvocationCount() {
        return invocationCount.get();
    }
}
