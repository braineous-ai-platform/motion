package ai.braineous.motion.ingestion.eventprocessor.infra.kafka;

import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class MotionEventEmitterIT {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String MOTION_EVENT_TOPIC = "motion_event";

    @Inject
    MotionEventEmitter motionEventEmitter;

    @Test
    public void test_1() {
        Console.log("integration runtime booted", "Motion Quarkus integration runtime is ready");

        String uniqueId = UUID.randomUUID().toString();
        String motionEventId = "motion-event-it-" + uniqueId;
        String consumerGroupId = "motion-event-emitter-it-" + uniqueId;

        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProperties);

        try {
            consumer.subscribe(Collections.singletonList(MOTION_EVENT_TOPIC));
            consumer.poll(Duration.ofSeconds(5));

            Console.log("Kafka consumer created", consumerGroupId);

            MotionEvent motionEvent = new MotionEvent();
            motionEvent.setEventId(motionEventId);
            motionEvent.setEventType("ORDER_STATUS_CHANGED");
            motionEvent.setOccurredAt("2026-09-07T16:30:00Z");
            motionEvent.setSubjectId("order-it-1001");
            motionEvent.setSubjectType("ORDER");
            motionEvent.setOperation("UPDATED");
            motionEvent.setPayloadJson("{\"status\":\"SHIPPED\"}");
            motionEvent.setMetadataJson("{\"source\":\"motion-emitter-it\"}");

            Console.log("MotionEvent created", motionEventId);

            MotionReplaySignal replaySignal = new MotionReplaySignal();
            replaySignal.setReplayLevel("PI_BACKTEST");
            replaySignal.setReasonCode("TEMPORAL_REPLAY_REQUESTED");
            replaySignal.setMessage("Replay requested by MotionEventEmitter integration test");
            replaySignal.setMotionEventId(motionEventId);
            motionEvent.setReplaySignal(replaySignal);

            Console.log("replay signal attached", replaySignal.toString());

            motionEventEmitter.emit(motionEvent);

            Console.log("emitter invoked", motionEventId);
            Console.log("Kafka polling started", MOTION_EVENT_TOPIC);

            String matchingPayload = null;
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (matchingPayload == null && System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> record : records) {
                    String value = record.value();

                    if (value != null && !value.isEmpty()) {
                        MotionEvent candidate = MotionEvent.fromJson(value, MotionEvent.class);

                        if (motionEventId.equals(candidate.getEventId())) {
                            matchingPayload = value;
                            Console.log("matching record received", value);
                            break;
                        }
                    }
                }
            }

            assertNotNull(matchingPayload);
            assertFalse(matchingPayload.isEmpty());
            assertEquals(motionEvent.toJson(), matchingPayload);

            MotionEvent restoredMotionEvent = MotionEvent.fromJson(
                    matchingPayload,
                    MotionEvent.class);

            Console.log("payload deserialized", restoredMotionEvent.toString());

            assertEquals(motionEventId, restoredMotionEvent.getEventId());
            assertEquals("ORDER_STATUS_CHANGED", restoredMotionEvent.getEventType());
            assertEquals("2026-09-07T16:30:00Z", restoredMotionEvent.getOccurredAt());
            assertEquals("order-it-1001", restoredMotionEvent.getSubjectId());
            assertEquals("ORDER", restoredMotionEvent.getSubjectType());
            assertEquals("UPDATED", restoredMotionEvent.getOperation());
            assertEquals("{\"status\":\"SHIPPED\"}", restoredMotionEvent.getPayloadJson());
            assertEquals("{\"source\":\"motion-emitter-it\"}", restoredMotionEvent.getMetadataJson());
            assertNotNull(restoredMotionEvent.getReplaySignal());

            MotionReplaySignal restoredReplaySignal = restoredMotionEvent.getReplaySignal();

            Console.log("replay signal inspected", restoredReplaySignal.toString());

            assertEquals("PI_BACKTEST", restoredReplaySignal.getReplayLevel());
            assertEquals("TEMPORAL_REPLAY_REQUESTED", restoredReplaySignal.getReasonCode());
            assertEquals(
                    "Replay requested by MotionEventEmitter integration test",
                    restoredReplaySignal.getMessage());
            assertEquals(motionEventId, restoredReplaySignal.getMotionEventId());

            Console.log("assertions complete", motionEventId);
        } finally {
            consumer.close(Duration.ofSeconds(5));
        }
    }
}
