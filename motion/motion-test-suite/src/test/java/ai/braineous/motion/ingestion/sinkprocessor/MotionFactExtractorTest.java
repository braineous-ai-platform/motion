package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionTimeWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MotionFactExtractorTest {

    @Test
    public void test_1() {
        MotionFrame motionFrame = newFrame("frame-1");

        MotionEvent event1 = newEvent("event-1", "ORDER_CREATED", "CREATED");
        MotionEvent event2 = newEvent("event-2", "ORDER_UPDATED", "UPDATED");
        MotionEvent event3 = newEvent("event-3", "ORDER_SHIPPED", "SHIPPED");

        motionFrame.addMotionEvent(event1);
        motionFrame.addMotionEvent(event2);
        motionFrame.addMotionEvent(event3);

        Console.log(
                "MotionFactExtractorTest",
                "INPUT GRAPH\n"
                        + "MotionFrame frame-1\n"
                        + "    MotionEvent event-1\n"
                        + "    MotionEvent event-2\n"
                        + "    MotionEvent event-3");

        MotionFactExtractor extractor = new MotionFactExtractor();
        List<Fact> facts = extractor.extract(motionFrame.toJson());

        Console.log(
                "MotionFactExtractorTest",
                "EXTRACTED FACTS = " + facts.size());

        for (Fact fact : facts) {
            Console.log(
                    "MotionFactExtractorTest",
                    "FACT ID = " + fact.getId());
            Console.log(
                    "MotionFactExtractorTest",
                    "FACT JSON = " + fact.getText());
        }

        Assertions.assertEquals(4, facts.size());

        Assertions.assertEquals("MotionFrame:frame-1", facts.get(0).getId());
        Assertions.assertEquals("MotionEvent:event-1", facts.get(1).getId());
        Assertions.assertEquals("MotionEvent:event-2", facts.get(2).getId());
        Assertions.assertEquals("MotionEvent:event-3", facts.get(3).getId());

        for (Fact fact : facts) {
            Assertions.assertEquals("atomic", fact.getMode());

            JsonObject factJson =
                    JsonParser.parseString(fact.getText()).getAsJsonObject();

            Assertions.assertEquals(fact.getId(), factJson.get("id").getAsString());
            Assertions.assertEquals("atomic", factJson.get("mode").getAsString());
        }

        JsonObject frameFactJson =
                JsonParser.parseString(facts.get(0).getText()).getAsJsonObject();

        Assertions.assertEquals("MotionFrame", frameFactJson.get("kind").getAsString());
        Assertions.assertEquals("frame-1", frameFactJson.get("frameId").getAsString());
        Assertions.assertEquals("ORDER_OPERATION_FRAME", frameFactJson.get("frameType").getAsString());
        Assertions.assertTrue(frameFactJson.has("timeWindow"));
        Assertions.assertEquals("1", frameFactJson.get("sequence").getAsString());
        Assertions.assertEquals("OPEN", frameFactJson.get("status").getAsString());
        Assertions.assertEquals("{\"runtime\":\"motion\"}", frameFactJson.get("metadataJson").getAsString());
        Assertions.assertFalse(frameFactJson.has("motionEvents"));

        JsonObject eventFactJson =
                JsonParser.parseString(facts.get(1).getText()).getAsJsonObject();

        Assertions.assertEquals("MotionEvent", eventFactJson.get("kind").getAsString());
        Assertions.assertEquals("event-1", eventFactJson.get("eventId").getAsString());
        Assertions.assertEquals("ORDER_CREATED", eventFactJson.get("eventType").getAsString());
        Assertions.assertEquals("2026-09-12T10:01:00Z", eventFactJson.get("originTime").getAsString());
        Assertions.assertEquals("2026-09-12T10:01:01Z", eventFactJson.get("receivedAt").getAsString());
        Assertions.assertEquals("ORDER-1001", eventFactJson.get("subjectId").getAsString());
        Assertions.assertEquals("ORDER", eventFactJson.get("subjectType").getAsString());
        Assertions.assertEquals("CREATED", eventFactJson.get("operation").getAsString());
        Assertions.assertEquals("{\"status\":\"CREATED\"}", eventFactJson.get("payloadJson").getAsString());
        Assertions.assertEquals("{\"runtime\":\"motion\"}", eventFactJson.get("metadataJson").getAsString());
        Assertions.assertTrue(eventFactJson.has("replaySignal"));

        Assertions.assertTrue(facts.get(1).getText().contains("\"eventId\":\"event-1\""));
        Assertions.assertTrue(facts.get(1).getText().contains("\"eventType\":\"ORDER_CREATED\""));
        Assertions.assertTrue(facts.get(1).getText().contains("\"operation\":\"CREATED\""));
        Assertions.assertTrue(facts.get(1).getText().contains("\"payloadJson\":\"{\\\"status\\\":\\\"CREATED\\\"}\""));
        Assertions.assertTrue(facts.get(1).getText().contains("\"replaySignal\":"));

        Assertions.assertTrue(facts.get(2).getText().contains("\"eventId\":\"event-2\""));
        Assertions.assertTrue(facts.get(2).getText().contains("\"eventType\":\"ORDER_UPDATED\""));
        Assertions.assertTrue(facts.get(2).getText().contains("\"operation\":\"UPDATED\""));

        Assertions.assertTrue(facts.get(3).getText().contains("\"eventId\":\"event-3\""));
        Assertions.assertTrue(facts.get(3).getText().contains("\"eventType\":\"ORDER_SHIPPED\""));
        Assertions.assertTrue(facts.get(3).getText().contains("\"operation\":\"SHIPPED\""));
    }

    @Test
    public void test_2() {
        MotionFactExtractor extractor = new MotionFactExtractor();

        List<Fact> nullFacts = extractor.extract(null);
        List<Fact> blankFacts = extractor.extract("   ");

        Assertions.assertNotNull(nullFacts);
        Assertions.assertNotNull(blankFacts);
        Assertions.assertEquals(0, nullFacts.size());
        Assertions.assertEquals(0, blankFacts.size());
    }

    @Test
    public void test_3() {
        MotionFactExtractor extractor = new MotionFactExtractor();

        List<Fact> facts = extractor.extract("{malformed-json");

        Assertions.assertNotNull(facts);
        Assertions.assertEquals(0, facts.size());
    }

    @Test
    public void test_4() {
        MotionFactExtractor extractor = new MotionFactExtractor();

        MotionFrame nullIdFrame = newFrame(null);
        nullIdFrame.addMotionEvent(newEvent("event-null-frame", "ORDER_UPDATED", "UPDATED"));

        List<Fact> nullIdFacts = extractor.extract(nullIdFrame.toJson());

        Assertions.assertEquals(1, nullIdFacts.size());
        Assertions.assertEquals("MotionEvent:event-null-frame", nullIdFacts.get(0).getId());

        MotionFrame blankIdFrame = newFrame("   ");
        blankIdFrame.addMotionEvent(newEvent("event-blank-frame", "ORDER_UPDATED", "UPDATED"));

        List<Fact> blankIdFacts = extractor.extract(blankIdFrame.toJson());

        Assertions.assertEquals(1, blankIdFacts.size());
        Assertions.assertEquals("MotionEvent:event-blank-frame", blankIdFacts.get(0).getId());
    }

    @Test
    public void test_5() {
        MotionFrame motionFrame = newFrame("frame-invalid-events");

        motionFrame.getMotionEvents().add(null);
        motionFrame.addMotionEvent(newEvent(null, "ORDER_UPDATED", "UPDATED"));
        motionFrame.addMotionEvent(newEvent("   ", "ORDER_UPDATED", "UPDATED"));
        motionFrame.addMotionEvent(newEvent("event-valid", "ORDER_UPDATED", "UPDATED"));

        MotionFactExtractor extractor = new MotionFactExtractor();
        List<Fact> facts = extractor.extract(motionFrame.toJson());

        Assertions.assertEquals(2, facts.size());
        Assertions.assertEquals("MotionFrame:frame-invalid-events", facts.get(0).getId());
        Assertions.assertEquals("MotionEvent:event-valid", facts.get(1).getId());
    }

    @Test
    public void test_6() {
        MotionFrame motionFrame = newFrame("frame-duplicates");

        MotionEvent duplicateEvent = newEvent("event-duplicate", "ORDER_UPDATED", "UPDATED");
        motionFrame.addMotionEvent(duplicateEvent);
        motionFrame.addMotionEvent(duplicateEvent);

        MotionFactExtractor extractor = new MotionFactExtractor();
        List<Fact> facts = extractor.extract(motionFrame.toJson());

        Assertions.assertEquals(3, facts.size());
        Assertions.assertEquals("MotionFrame:frame-duplicates", facts.get(0).getId());
        Assertions.assertEquals("MotionEvent:event-duplicate", facts.get(1).getId());
        Assertions.assertEquals("MotionEvent:event-duplicate", facts.get(2).getId());
    }

    private MotionFrame newFrame(String frameId) {
        MotionFrame motionFrame = new MotionFrame();

        motionFrame.setFrameId(frameId);
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
        replaySignal.setMessage("Replay is not required for accepted Motion event");
        replaySignal.setMotionEventId(eventId);
        motionEvent.setReplaySignal(replaySignal);

        return motionEvent;
    }
}
