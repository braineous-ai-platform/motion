package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Observable;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemMutation;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionBaseModel;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class MutationPublisherIT {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String MUTATION_EVENT_TOPIC = "mutation_event";

    @Inject
    MutationPublisher mutationPublisher;

    @Test
    public void test_1() {
        String uniqueId = UUID.randomUUID().toString();
        String mutationId = "mutation-publisher-it-" + uniqueId;
        String consumerGroupId = "mutation-publisher-it-" + uniqueId;
        String anchorId = "PaymentRequest:PAY-IT-" + uniqueId;
        String cognitiveResultJson =
                "{\"gateResult\":{\"ok\":true,\"why\":\"approved\",\"mutationIt\":\""
                        + uniqueId
                        + "\"}}";

        Perception perception = perception(anchorId);

        SystemMutation mutation = new SystemMutation();
        mutation.setPerception(perception);
        mutation.setMutationId(mutationId);
        mutation.setCreatedAt("2026-10-18T09:25:00Z");
        mutation.setCognitiveQuery("select decision from llm");
        mutation.setCognitiveResultJson(cognitiveResultJson);

        String expectedPayload = mutation.toJson();

        Console.log("MutationPublisherIT.topic", MUTATION_EVENT_TOPIC);
        Console.log("MutationPublisherIT.group", consumerGroupId);
        Console.log("MutationPublisherIT.mutationId", mutationId);

        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        consumerProperties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        consumerProperties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> consumer =
                new KafkaConsumer<String, String>(consumerProperties);

        try {
            consumer.subscribe(Collections.singletonList(MUTATION_EVENT_TOPIC));
            consumer.poll(Duration.ofSeconds(5));

            mutationPublisher.publish(mutation);

            Console.log("MutationPublisherIT.published", mutationId);

            String matchingPayload = null;
            String recordTopic = null;
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (matchingPayload == null && System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> record : records) {
                    String value = record.value();

                    if (value != null && value.contains(mutationId)) {
                        matchingPayload = value;
                        recordTopic = record.topic();
                        Console.log("MutationPublisherIT.record.topic", recordTopic);
                        Console.log("MutationPublisherIT.record.value", value);
                        break;
                    }
                }
            }

            assertNotNull(matchingPayload);
            assertEquals(MUTATION_EVENT_TOPIC, recordTopic);
            assertEquals(expectedPayload, matchingPayload);

            JsonObject root = JsonParser.parseString(matchingPayload).getAsJsonObject();
            assertEquals(5, root.size());
            assertTrue(root.has("perception"));
            assertTrue(root.has("mutationId"));
            assertTrue(root.has("createdAt"));
            assertTrue(root.has("cognitiveQuery"));
            assertTrue(root.has("cognitiveResultJson"));
            assertFalse(root.has("kafka"));
            assertFalse(root.has("payload"));
            assertFalse(root.has("eventId"));
            assertFalse(root.has("eventType"));
            assertFalse(root.has("payloadJson"));
            assertFalse(root.has("ingestionId"));
            assertEquals(mutationId, root.get("mutationId").getAsString());
            assertTrue(root.get("cognitiveResultJson").isJsonPrimitive());
            assertEquals(
                    cognitiveResultJson,
                    root.get("cognitiveResultJson").getAsString());

            SystemMutation restored =
                    MotionBaseModel.fromJson(matchingPayload, SystemMutation.class);

            assertEquals(mutationId, restored.getMutationId());
            assertEquals(cognitiveResultJson, restored.getCognitiveResultJson());
            assertNotNull(restored.getPerception());
            assertNotNull(restored.getPerception().getObservation());
            assertNotNull(restored.getPerception().getObservation().getObservable());
            assertEquals(
                    anchorId,
                    restored.getPerception()
                            .getObservation()
                            .getObservable()
                            .getObservableFactAnchor()
                            .getId());

            assertEquals(mutationId, mutation.getMutationId());
            assertEquals(cognitiveResultJson, mutation.getCognitiveResultJson());
            assertEquals(anchorId, mutation.getPerception()
                    .getObservation()
                    .getObservable()
                    .getObservableFactAnchor()
                    .getId());
            assertEquals(expectedPayload, mutation.toJson());
        } finally {
            consumer.close(Duration.ofSeconds(5));
        }
    }

    private Perception perception(String anchorId) {
        Fact anchor = new Fact(anchorId, "{\"id\":\"" + anchorId + "\"}");

        Observable observable = new Observable();
        observable.setObservableFactAnchor(anchor);

        Observation observation = new Observation();
        observation.setObservable(observable);

        Perception perception = new Perception();
        perception.setObservation(observation);
        return perception;
    }
}
