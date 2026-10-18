package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemMutation;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MutationPublisherTest {

    @Test
    public void test_1() {
        SystemMutation mutation = systemMutation();
        String expected = mutation.toJson();

        RecordingEmitter emitter = new RecordingEmitter();
        MutationPublisher publisher = new MutationPublisher();
        publisher.emitter = emitter;

        publisher.publish(mutation);

        Assertions.assertEquals(expected, emitter.payload);
        Assertions.assertEquals(expected, mutation.toJson());
        Assertions.assertEquals("mutation-1", mutation.getMutationId());
        Assertions.assertEquals("2026-10-16T18:00:00Z", mutation.getCreatedAt());
        Assertions.assertEquals(
                "select decision from llm",
                mutation.getCognitiveQuery());
        Assertions.assertEquals(
                "{\"gateResult\":{\"ok\":true,\"why\":\"approved\"}}",
                mutation.getCognitiveResultJson());
    }

    @Test
    public void test_2() {
        SystemMutation mutation = systemMutation();

        RecordingEmitter emitter = new RecordingEmitter();
        MutationPublisher publisher = new MutationPublisher();
        publisher.emitter = emitter;

        publisher.publish(mutation);

        Assertions.assertEquals(1, emitter.sendCount);
    }

    private SystemMutation systemMutation() {
        Perception perception = new Perception();
        Observation observation = new Observation();
        perception.setObservation(observation);

        SystemMutation mutation = new SystemMutation();
        mutation.setPerception(perception);
        mutation.setMutationId("mutation-1");
        mutation.setCreatedAt("2026-10-16T18:00:00Z");
        mutation.setCognitiveQuery("select decision from llm");
        mutation.setCognitiveResultJson(
                "{\"gateResult\":{\"ok\":true,\"why\":\"approved\"}}");
        return mutation;
    }

    static class RecordingEmitter implements Emitter<String> {

        String payload;
        int sendCount;

        @Override
        public CompletionStage<Void> send(String payload) {
            this.payload = payload;
            this.sendCount = this.sendCount + 1;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <M extends Message<? extends String>> void send(M message) {
            throw new UnsupportedOperationException("Message emission is not used");
        }

        @Override
        public void complete() {
            throw new UnsupportedOperationException("Completion is not used");
        }

        @Override
        public void error(Exception failure) {
            throw new UnsupportedOperationException("Error signaling is not used");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean hasRequests() {
            return true;
        }
    }
}
