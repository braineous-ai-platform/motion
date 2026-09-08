package ai.braineous.motion.ingestion.timeprocessor.infra.flink;

import ai.braineous.motion.ingestion.timeprocessor.orchestrator.MotionFrameRoutingKeyResolver;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionReplaySignal;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.CloseableIterator;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionFramePartitionKeySelectorIT {

    @Test
    public void test_1() throws Exception {
        Console.log("Flink runtime", "create bounded real topology");

        MotionEvent eventA1 = newEvent(
                "event-a1",
                "ORDER-1001",
                "ORDER",
                "CREATED");

        MotionEvent eventA2 = newEvent(
                "event-a2",
                "ORDER-1001",
                "ORDER",
                "UPDATED");

        MotionEvent eventB1 = newEvent(
                "event-b1",
                "PAYMENT-2001",
                "PAYMENT",
                "AUTHORIZED");

        Map<String, MotionEvent> expectedEvents =
                new HashMap<String, MotionEvent>();

        expectedEvents.put(eventA1.getEventId(), eventA1);
        expectedEvents.put(eventA2.getEventId(), eventA2);
        expectedEvents.put(eventB1.getEventId(), eventB1);

        StreamExecutionEnvironment environment =
                StreamExecutionEnvironment.getExecutionEnvironment();

        environment.setParallelism(2);

        MotionFramePartitionKeySelector selector =
                new MotionFramePartitionKeySelector(
                        new MotionFrameRoutingKeyResolver());

        DataStream<PartitionObservation> observations =
                environment
                        .fromData(eventA1, eventA2, eventB1)
                        .keyBy(selector)
                        .process(new KeyObservationFunction());

        Console.log("Flink topology", "source -> keyBy -> keyed observation");

        List<PartitionObservation> results =
                new ArrayList<PartitionObservation>();

        CloseableIterator<PartitionObservation> iterator =
                observations.executeAndCollect();

        try {
            while (iterator.hasNext()) {
                results.add(iterator.next());
            }
        } finally {
            iterator.close();
        }

        Console.log("Flink completion", "observations=" + results.size());

        Map<String, PartitionObservation> observationsByEventId =
                new HashMap<String, PartitionObservation>();

        for (PartitionObservation observation : results) {
            observationsByEventId.put(
                    observation.getMotionEvent().getEventId(),
                    observation);

            Console.log(
                    "keyed observation",
                    observation.getMotionEvent().getEventId()
                            + " -> " + observation.getRoutingKey()
                            + " @ subtask " + observation.getSubtaskIndex());
        }

        assertEquals(3, results.size());
        assertEquals(3, observationsByEventId.size());

        PartitionObservation observationA1 =
                observationsByEventId.get("event-a1");
        PartitionObservation observationA2 =
                observationsByEventId.get("event-a2");
        PartitionObservation observationB1 =
                observationsByEventId.get("event-b1");

        assertNotNull(observationA1);
        assertNotNull(observationA2);
        assertNotNull(observationB1);

        assertEquals(observationA1.getRoutingKey(), observationA2.getRoutingKey());
        assertEquals(observationA1.getSubtaskIndex(), observationA2.getSubtaskIndex());
        assertNotEquals(observationA1.getRoutingKey(), observationB1.getRoutingKey());

        assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                observationA1.getRoutingKey());

        assertEquals(
                "PAYMENT-2001:PAYMENT:PAYMENT_OPERATION_FRAME",
                observationB1.getRoutingKey());

        for (Map.Entry<String, MotionEvent> expectedEntry : expectedEvents.entrySet()) {
            PartitionObservation actualObservation =
                    observationsByEventId.get(expectedEntry.getKey());

            assertNotNull(actualObservation);
            assertEquals(
                    expectedEntry.getValue().toJson(),
                    actualObservation.getMotionEvent().toJson());
        }

        Console.log("serialization evidence", "selector and resolver executed in Flink job");
        Console.log("assertions complete", "all canonical events preserved");
    }

    private MotionEvent newEvent(
            String eventId,
            String subjectId,
            String subjectType,
            String operation) {

        MotionEvent motionEvent = new MotionEvent();
        motionEvent.setEventId(eventId);
        motionEvent.setEventType("SUBJECT_CHANGED");
        motionEvent.setOriginTime("2026-09-07T20:00:00Z");
        motionEvent.setSubjectId(subjectId);
        motionEvent.setSubjectType(subjectType);
        motionEvent.setOperation(operation);
        motionEvent.setPayloadJson("{\"eventId\":\"" + eventId + "\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"flink-it\"}");

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("NONE");
        replaySignal.setReasonCode("LIVE_EVENT");
        replaySignal.setMessage("Canonical live event");
        replaySignal.setMotionEventId(eventId);
        motionEvent.setReplaySignal(replaySignal);

        return motionEvent;
    }

    private static class KeyObservationFunction
            extends KeyedProcessFunction<String, MotionEvent, PartitionObservation> {

        private static final long serialVersionUID = 1L;

        @Override
        public void processElement(
                MotionEvent motionEvent,
                Context context,
                Collector<PartitionObservation> collector) {

            PartitionObservation observation = new PartitionObservation();
            observation.setRoutingKey(context.getCurrentKey());
            observation.setSubtaskIndex(
                    getRuntimeContext().getTaskInfo().getIndexOfThisSubtask());
            observation.setMotionEvent(motionEvent);
            collector.collect(observation);
        }
    }

    public static class PartitionObservation {

        private String routingKey;
        private int subtaskIndex;
        private MotionEvent motionEvent;

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public int getSubtaskIndex() {
            return subtaskIndex;
        }

        public void setSubtaskIndex(int subtaskIndex) {
            this.subtaskIndex = subtaskIndex;
        }

        public MotionEvent getMotionEvent() {
            return motionEvent;
        }

        public void setMotionEvent(MotionEvent motionEvent) {
            this.motionEvent = motionEvent;
        }
    }
}
