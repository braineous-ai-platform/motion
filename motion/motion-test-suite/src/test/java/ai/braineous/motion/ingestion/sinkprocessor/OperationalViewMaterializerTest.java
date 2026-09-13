package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationalViewMaterializerTest {

    @Test
    public void test_1() {

        OperationalViewMaterializer materializer =
                new OperationalViewMaterializer();

        logNullCase("case-1-null-motion-frame");
        Assertions.assertNull(materializer.materialize(null));

        MotionFrame zeroEventFrame =
                newFrame(
                        "frame-zero",
                        "ORDER_FRAME",
                        "2026-09-13T00:00:00Z",
                        "2026-09-13T00:05:00Z",
                        "1",
                        "OPEN",
                        "{\"case\":\"zero\"}");

        materializeAndAssert(
                "case-2-zero-events",
                materializer,
                zeroEventFrame,
                new ArrayList<String>(),
                true);

        MotionFrame oneEventFrame =
                newFrame(
                        "frame-one",
                        "ORDER_FRAME",
                        "2026-09-13T00:05:00Z",
                        "2026-09-13T00:10:00Z",
                        "2",
                        "OPEN",
                        "{\"case\":\"one\"}");
        oneEventFrame.addMotionEvent(newEvent("event-01"));

        List<String> oneEventIds = new ArrayList<String>();
        oneEventIds.add("event-01");

        materializeAndAssert(
                "case-3-one-event",
                materializer,
                oneEventFrame,
                oneEventIds,
                false);

        MotionFrame manyEventFrame =
                newFrame(
                        "frame-many",
                        "ORDER_FRAME",
                        "2026-09-13T00:10:00Z",
                        "2026-09-13T00:15:00Z",
                        "3",
                        "OPEN",
                        "{\"case\":\"many\"}");
        List<String> manyEventIds = new ArrayList<String>();

        for (int index = 1; index <= 10; index++) {
            String eventId = String.format("event-%02d", Integer.valueOf(index));
            manyEventFrame.addMotionEvent(newEvent(eventId));
            manyEventIds.add(eventId);
        }

        materializeAndAssert(
                "case-4-many-events",
                materializer,
                manyEventFrame,
                manyEventIds,
                true);

        MotionFrame duplicateEventFrame =
                newFrame(
                        "frame-duplicates",
                        "ORDER_FRAME",
                        "2026-09-13T00:15:00Z",
                        "2026-09-13T00:20:00Z",
                        "4",
                        "OPEN",
                        "{\"case\":\"duplicates\"}");
        duplicateEventFrame.addMotionEvent(newEvent("event-A"));
        duplicateEventFrame.addMotionEvent(newEvent("event-B"));
        duplicateEventFrame.addMotionEvent(newEvent("event-A"));
        duplicateEventFrame.addMotionEvent(newEvent("event-C"));
        duplicateEventFrame.addMotionEvent(newEvent("event-A"));

        List<String> duplicateEventIds = new ArrayList<String>();
        duplicateEventIds.add("event-A");
        duplicateEventIds.add("event-B");
        duplicateEventIds.add("event-A");
        duplicateEventIds.add("event-C");
        duplicateEventIds.add("event-A");

        materializeAndAssert(
                "case-5-duplicate-event-ids",
                materializer,
                duplicateEventFrame,
                duplicateEventIds,
                false);

        MotionFrame nullEventFrame =
                newFrame(
                        "frame-null-event",
                        "ORDER_FRAME",
                        "2026-09-13T00:20:00Z",
                        "2026-09-13T00:25:00Z",
                        "5",
                        "OPEN",
                        "{\"case\":\"null-event\"}");
        nullEventFrame.getMotionEvents().add(newEvent("event-before-null"));
        nullEventFrame.getMotionEvents().add(null);
        nullEventFrame.getMotionEvents().add(newEvent("event-after-null"));

        List<String> nullEventIds = new ArrayList<String>();
        nullEventIds.add("event-before-null");
        nullEventIds.add("event-after-null");

        materializeAndAssert(
                "case-6-null-event",
                materializer,
                nullEventFrame,
                nullEventIds,
                true);

        MotionFrame nullEventIdFrame =
                newFrame(
                        "frame-null-event-id",
                        "ORDER_FRAME",
                        "2026-09-13T00:25:00Z",
                        "2026-09-13T00:30:00Z",
                        "6",
                        "OPEN",
                        "{\"case\":\"null-event-id\"}");
        nullEventIdFrame.addMotionEvent(newEvent("event-before-null-id"));
        nullEventIdFrame.addMotionEvent(newEvent(null));
        nullEventIdFrame.addMotionEvent(newEvent("event-after-null-id"));

        List<String> nullEventIdIds = new ArrayList<String>();
        nullEventIdIds.add("event-before-null-id");
        nullEventIdIds.add("event-after-null-id");

        materializeAndAssert(
                "case-7-null-event-id",
                materializer,
                nullEventIdFrame,
                nullEventIdIds,
                false);

        MotionFrame blankEventIdFrame =
                newFrame(
                        "frame-blank-event-id",
                        "ORDER_FRAME",
                        "2026-09-13T00:30:00Z",
                        "2026-09-13T00:35:00Z",
                        "7",
                        "OPEN",
                        "{\"case\":\"blank-event-id\"}");
        blankEventIdFrame.addMotionEvent(newEvent("event-before-blank"));
        blankEventIdFrame.addMotionEvent(newEvent(""));
        blankEventIdFrame.addMotionEvent(newEvent("event-after-blank"));

        List<String> blankEventIds = new ArrayList<String>();
        blankEventIds.add("event-before-blank");
        blankEventIds.add("");
        blankEventIds.add("event-after-blank");

        materializeAndAssert(
                "case-8-blank-event-id",
                materializer,
                blankEventIdFrame,
                blankEventIds,
                false);

        MotionFrame nullEventsFrame =
                newFrame(
                        "frame-null-events",
                        "ORDER_FRAME",
                        "2026-09-13T00:35:00Z",
                        "2026-09-13T00:40:00Z",
                        "8",
                        "OPEN",
                        "{\"case\":\"null-events\"}");
        nullEventsFrame.setMotionEvents(null);

        materializeAndAssert(
                "case-9-null-motion-events-list",
                materializer,
                nullEventsFrame,
                new ArrayList<String>(),
                false);

        MotionFrame nullTimeWindowFrame =
                newFrame(
                        "frame-null-window",
                        "ORDER_FRAME",
                        null,
                        null,
                        "9",
                        "OPEN",
                        "{\"case\":\"null-window\"}");
        nullTimeWindowFrame.setTimeWindow(null);

        materializeAndAssert(
                "case-10-null-time-window",
                materializer,
                nullTimeWindowFrame,
                new ArrayList<String>(),
                false);

        MotionFrame nullFrameIdFrame =
                newFrame(
                        null,
                        "ORDER_FRAME",
                        "2026-09-13T00:45:00Z",
                        "2026-09-13T00:50:00Z",
                        "10",
                        "OPEN",
                        "{\"case\":\"null-frame-id\"}");

        OperationalView nullFrameIdView =
                materializeAndAssert(
                        "case-11-null-frame-id",
                        materializer,
                        nullFrameIdFrame,
                        new ArrayList<String>(),
                        false);
        Assertions.assertNull(nullFrameIdView.getViewId());
        Assertions.assertNotNull(nullFrameIdView.getSourceFrameIds());
        Assertions.assertEquals(0, nullFrameIdView.getSourceFrameIds().size());

        MotionFrame nullStateFrame =
                newFrame(
                        "frame-null-state",
                        null,
                        "2026-09-13T00:50:00Z",
                        "2026-09-13T00:55:00Z",
                        null,
                        null,
                        null);

        OperationalView nullStateView =
                materializeAndAssert(
                        "case-12-null-state-values",
                        materializer,
                        nullStateFrame,
                        new ArrayList<String>(),
                        false);
        Assertions.assertNull(nullStateView.getState().get("frameType"));
        Assertions.assertNull(nullStateView.getState().get("sequence"));
        Assertions.assertNull(nullStateView.getState().get("status"));
        Assertions.assertNull(nullStateView.getState().get("metadataJson"));

        MotionFrame unusualFrame =
                newFrame(
                        "   ",
                        "FRAME::UNUSUAL/TYPE",
                        "2026-09-13T00:55:00Z",
                        "2026-09-13T01:00:00Z",
                        " sequence with spaces ",
                        "",
                        "");

        materializeAndAssert(
                "case-13-unusual-legal-strings",
                materializer,
                unusualFrame,
                new ArrayList<String>(),
                false);

        for (int frameIndex = 1; frameIndex <= 20; frameIndex++) {
            String frameId = String.format(
                    "sequence-frame-%02d",
                    Integer.valueOf(frameIndex));
            Instant windowStart =
                    Instant.parse("2026-09-14T00:00:00Z")
                            .plusSeconds(frameIndex * 300L);
            Instant windowEnd = windowStart.plusSeconds(300L);
            String sequence = String.valueOf(frameIndex);

            MotionFrame sequenceFrame =
                    newFrame(
                            frameId,
                            "SEQUENCE_FRAME_" + frameIndex,
                            windowStart.toString(),
                            windowEnd.toString(),
                            sequence,
                            "OPEN_" + frameIndex,
                            "{\"frameIndex\":" + frameIndex + "}");

            List<String> sequenceEventIds = new ArrayList<String>();
            int eventCount = frameIndex % 4;

            for (int eventIndex = 1;
                 eventIndex <= eventCount;
                 eventIndex++) {
                String eventId =
                        frameId + ":event-" + eventIndex;
                sequenceFrame.addMotionEvent(newEvent(eventId));
                sequenceEventIds.add(eventId);
            }

            materializeAndAssert(
                    "case-14-sequential-frame-" + frameIndex,
                    materializer,
                    sequenceFrame,
                    sequenceEventIds,
                    false);
        }

        MotionFrame repeatedFrame =
                newFrame(
                        "frame-repeated",
                        "REPEATED_FRAME",
                        "2026-09-15T00:00:00Z",
                        "2026-09-15T00:05:00Z",
                        "15",
                        "OPEN",
                        "{\"case\":\"repeated\"}");
        repeatedFrame.addMotionEvent(newEvent("repeated-event-1"));
        repeatedFrame.addMotionEvent(newEvent("repeated-event-2"));

        List<String> repeatedEventIds = new ArrayList<String>();
        repeatedEventIds.add("repeated-event-1");
        repeatedEventIds.add("repeated-event-2");

        OperationalView repeatedView1 =
                materializeAndAssert(
                        "case-15-same-frame-first-call",
                        materializer,
                        repeatedFrame,
                        repeatedEventIds,
                        true);
        OperationalView repeatedView2 =
                materializeAndAssert(
                        "case-15-same-frame-second-call",
                        materializer,
                        repeatedFrame,
                        repeatedEventIds,
                        true);

        Assertions.assertNotSame(repeatedView1, repeatedView2);
        Assertions.assertNotSame(repeatedView1.getState(), repeatedView2.getState());
        Assertions.assertNotSame(
                repeatedView1.getSourceFrameIds(),
                repeatedView2.getSourceFrameIds());
        Assertions.assertNotSame(
                repeatedView1.getSourceEventIds(),
                repeatedView2.getSourceEventIds());
        assertSemanticEquality(repeatedView1, repeatedView2);

        MotionFrame mutationFrame =
                newFrame(
                        "frame-mutation",
                        "MUTATION_FRAME",
                        "2026-09-15T01:00:00Z",
                        "2026-09-15T01:05:00Z",
                        "16",
                        "OPEN",
                        "{\"case\":\"mutation\"}");
        mutationFrame.addMotionEvent(newEvent("mutation-event-1"));
        mutationFrame.addMotionEvent(newEvent("mutation-event-2"));

        List<String> mutationEventIds = new ArrayList<String>();
        mutationEventIds.add("mutation-event-1");
        mutationEventIds.add("mutation-event-2");

        OperationalView mutationView1 =
                materializeAndAssert(
                        "case-16-mutation-isolation-first-call",
                        materializer,
                        mutationFrame,
                        mutationEventIds,
                        false);

        mutationView1.getSourceFrameIds().add("injected-frame");
        mutationView1.getSourceEventIds().add("injected-event");
        mutationView1.getState().put("injected-state", "injected-value");

        OperationalView mutationView2 =
                materializeAndAssert(
                        "case-16-mutation-isolation-second-call",
                        materializer,
                        mutationFrame,
                        mutationEventIds,
                        false);

        Assertions.assertFalse(
                mutationView2.getSourceFrameIds().contains("injected-frame"));
        Assertions.assertFalse(
                mutationView2.getSourceEventIds().contains("injected-event"));
        Assertions.assertFalse(
                mutationView2.getState().containsKey("injected-state"));
    }

    private OperationalView materializeAndAssert(
            String caseName,
            OperationalViewMaterializer materializer,
            MotionFrame motionFrame,
            List<String> expectedEventIds,
            boolean assertInputUnmodified) {

        Console.log(
                "OperationalViewMaterializerTest.case",
                caseName);
        Console.log(
                "OperationalViewMaterializerTest.input",
                motionFrame.toJson());

        String beforeJson = motionFrame.toJson();
        Instant before = Instant.now();

        OperationalView operationalView =
                materializer.materialize(motionFrame);

        Instant after = Instant.now();

        Console.log(
                "OperationalViewMaterializerTest.output",
                operationalView.toJson());

        Assertions.assertNotNull(operationalView);
        Assertions.assertEquals(
                motionFrame.getFrameId(),
                operationalView.getViewId());
        Assertions.assertNull(operationalView.getRoutingKey());
        Assertions.assertNull(operationalView.getContextId());

        if (motionFrame.getTimeWindow() == null) {
            Assertions.assertNull(operationalView.getWindowStart());
            Assertions.assertNull(operationalView.getWindowEnd());
        } else {
            Assertions.assertEquals(
                    motionFrame.getTimeWindow().getWindowStart(),
                    operationalView.getWindowStart());
            Assertions.assertEquals(
                    motionFrame.getTimeWindow().getWindowEnd(),
                    operationalView.getWindowEnd());
        }

        assertState(motionFrame, operationalView);
        assertSourceFrameIds(motionFrame, operationalView);
        Assertions.assertNotNull(operationalView.getSourceEventIds());
        Assertions.assertEquals(
                expectedEventIds,
                operationalView.getSourceEventIds());
        assertMaterializedAt(before, after, operationalView);

        if (assertInputUnmodified) {
            String afterJson = motionFrame.toJson();
            Assertions.assertEquals(beforeJson, afterJson);
        }

        return operationalView;
    }

    private void assertState(
            MotionFrame motionFrame,
            OperationalView operationalView) {

        Map<String, Object> state = operationalView.getState();

        Assertions.assertNotNull(state);
        Assertions.assertTrue(state instanceof LinkedHashMap);
        Assertions.assertEquals(4, state.size());
        Assertions.assertTrue(state.containsKey("frameType"));
        Assertions.assertTrue(state.containsKey("sequence"));
        Assertions.assertTrue(state.containsKey("status"));
        Assertions.assertTrue(state.containsKey("metadataJson"));

        Iterator<String> keys = state.keySet().iterator();
        Assertions.assertEquals("frameType", keys.next());
        Assertions.assertEquals("sequence", keys.next());
        Assertions.assertEquals("status", keys.next());
        Assertions.assertEquals("metadataJson", keys.next());
        Assertions.assertFalse(keys.hasNext());

        Assertions.assertEquals(
                motionFrame.getFrameType(),
                state.get("frameType"));
        Assertions.assertEquals(
                motionFrame.getSequence(),
                state.get("sequence"));
        Assertions.assertEquals(
                motionFrame.getStatus(),
                state.get("status"));
        Assertions.assertEquals(
                motionFrame.getMetadataJson(),
                state.get("metadataJson"));
    }

    private void assertSourceFrameIds(
            MotionFrame motionFrame,
            OperationalView operationalView) {

        Assertions.assertNotNull(operationalView.getSourceFrameIds());

        if (motionFrame.getFrameId() == null) {
            Assertions.assertEquals(
                    0,
                    operationalView.getSourceFrameIds().size());
        } else {
            Assertions.assertEquals(
                    1,
                    operationalView.getSourceFrameIds().size());
            Assertions.assertEquals(
                    motionFrame.getFrameId(),
                    operationalView.getSourceFrameIds().get(0));
        }
    }

    private void assertMaterializedAt(
            Instant before,
            Instant after,
            OperationalView operationalView) {

        Assertions.assertNotNull(operationalView.getMaterializedAt());

        Instant materializedAt =
                Instant.parse(operationalView.getMaterializedAt());

        Assertions.assertFalse(materializedAt.isBefore(before));
        Assertions.assertFalse(materializedAt.isAfter(after));
    }

    private void assertSemanticEquality(
            OperationalView first,
            OperationalView second) {

        Assertions.assertEquals(first.getViewId(), second.getViewId());
        Assertions.assertEquals(first.getRoutingKey(), second.getRoutingKey());
        Assertions.assertEquals(first.getContextId(), second.getContextId());
        Assertions.assertEquals(first.getWindowStart(), second.getWindowStart());
        Assertions.assertEquals(first.getWindowEnd(), second.getWindowEnd());
        Assertions.assertEquals(first.getState(), second.getState());
        Assertions.assertEquals(
                first.getSourceFrameIds(),
                second.getSourceFrameIds());
        Assertions.assertEquals(
                first.getSourceEventIds(),
                second.getSourceEventIds());
        Instant.parse(first.getMaterializedAt());
        Instant.parse(second.getMaterializedAt());
    }

    private MotionFrame newFrame(
            String frameId,
            String frameType,
            String windowStart,
            String windowEnd,
            String sequence,
            String status,
            String metadataJson) {

        MotionFrame motionFrame = new MotionFrame();

        motionFrame.setFrameId(frameId);
        motionFrame.setFrameType(frameType);

        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart(windowStart);
        timeWindow.setWindowEnd(windowEnd);
        motionFrame.setTimeWindow(timeWindow);

        motionFrame.setSequence(sequence);
        motionFrame.setStatus(status);
        motionFrame.setMetadataJson(metadataJson);

        return motionFrame;
    }

    private MotionEvent newEvent(String eventId) {

        MotionEvent motionEvent = new MotionEvent();

        motionEvent.setEventId(eventId);
        motionEvent.setEventType("ORDER_UPDATED");
        motionEvent.setOriginTime("2026-09-13T00:01:00Z");
        motionEvent.setReceivedAt("2026-09-13T00:01:01Z");
        motionEvent.setSubjectId("ORDER-1001");
        motionEvent.setSubjectType("ORDER");
        motionEvent.setOperation("UPDATED");
        motionEvent.setPayloadJson("{\"status\":\"UPDATED\"}");
        motionEvent.setMetadataJson("{\"runtime\":\"motion\"}");

        return motionEvent;
    }

    private void logNullCase(String caseName) {

        Console.log(
                "OperationalViewMaterializerTest.case",
                caseName);
        Console.log(
                "OperationalViewMaterializerTest.input",
                "null");
        Console.log(
                "OperationalViewMaterializerTest.output",
                "null");
    }
}
