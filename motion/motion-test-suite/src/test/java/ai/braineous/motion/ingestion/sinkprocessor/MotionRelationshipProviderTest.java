package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionTimeWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MotionRelationshipProviderTest {

    @Test
    public void test_1() {
        MotionFrame motionFrame = newFrame();

        motionFrame.addMotionEvent(
                newEvent("event-1", "ORDER_CREATED", "CREATED"));
        motionFrame.addMotionEvent(
                newEvent("event-2", "ORDER_UPDATED", "UPDATED"));
        motionFrame.addMotionEvent(
                newEvent("event-3", "ORDER_SHIPPED", "SHIPPED"));

        MotionFactExtractor extractor = new MotionFactExtractor();
        List<Fact> facts = extractor.extract(motionFrame.toJson());

        Console.log("MotionRelationshipProviderTest", "INPUT FACTS");

        for (Fact fact : facts) {
            Console.log(
                    "MotionRelationshipProviderTest",
                    "FACT ID = " + fact.getId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "FACT MODE = " + fact.getMode());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "FACT JSON = " + fact.getText());
        }

        MotionRelationshipProvider provider =
                new MotionRelationshipProvider();

        List<Relationship> relationships =
                provider.provideRelationships(facts);

        Console.log(
                "MotionRelationshipProviderTest",
                "RELATIONSHIPS = " + relationships.size());

        for (Relationship relationship : relationships) {
            Fact from = relationship.getFrom();
            Fact to = relationship.getTo();
            Edge edge = (Edge) relationship.getEdge();

            Console.log(
                    "MotionRelationshipProviderTest",
                    "FROM ID = " + from.getId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "FROM JSON = " + from.getText());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "TO ID = " + to.getId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "TO JSON = " + to.getText());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "EDGE ID = " + edge.getId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "EDGE FROM = " + edge.getFromFactId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "EDGE TO = " + edge.getToFactId());
            Console.log(
                    "MotionRelationshipProviderTest",
                    "EDGE MODE = " + edge.getMode());
        }

        Assertions.assertEquals(4, facts.size());
        Assertions.assertEquals(3, relationships.size());

        String[] expectedToIds = new String[]{
                "MotionEvent:event-1",
                "MotionEvent:event-2",
                "MotionEvent:event-3"
        };

        int index = 0;
        while (index < relationships.size()) {
            Relationship relationship = relationships.get(index);
            Fact from = relationship.getFrom();
            Fact to = relationship.getTo();
            Edge edge = (Edge) relationship.getEdge();

            Assertions.assertEquals("MotionFrame:frame-1", from.getId());
            Assertions.assertEquals(expectedToIds[index], to.getId());
            Assertions.assertEquals("atomic", from.getMode());
            Assertions.assertEquals("atomic", to.getMode());
            Assertions.assertEquals("relational", edge.getMode());
            Assertions.assertEquals(from.getId(), edge.getFromFactId());
            Assertions.assertEquals(to.getId(), edge.getToFactId());

            index = index + 1;
        }
    }

    private MotionFrame newFrame() {
        MotionFrame motionFrame = new MotionFrame();

        motionFrame.setFrameId("frame-1");
        motionFrame.setFrameType("ORDER_OPERATION_FRAME");

        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-09-12T10:00:00Z");
        timeWindow.setWindowEnd("2026-09-12T10:05:00Z");
        motionFrame.setTimeWindow(timeWindow);

        motionFrame.setSequence("1");
        motionFrame.setStatus("OPEN");
        motionFrame.setMetadataJson("{\"runtime\":\"motion\"}");

        return motionFrame;
    }

    private MotionEvent newEvent(
            String eventId,
            String eventType,
            String operation) {
        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId(eventId);
        motionEvent.setEventType(eventType);
        motionEvent.setOriginTime("2026-09-12T10:01:00Z");
        motionEvent.setReceivedAt("2026-09-12T10:01:01Z");
        motionEvent.setSubjectId("ORDER-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation(operation);
        motionEvent.setPayloadJson("{\"status\":\"" + operation + "\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionReplaySignal replaySignal = new MotionReplaySignal();
        replaySignal.setReplayLevel("FAILURE_RECOVERY");
        replaySignal.setReasonCode("REPLAY_NOT_REQUIRED");
        replaySignal.setMessage(
                "Replay is not required for accepted Motion event");
        replaySignal.setMotionEventId(eventId);
        motionEvent.setReplaySignal(replaySignal);

        return motionEvent;
    }
}
