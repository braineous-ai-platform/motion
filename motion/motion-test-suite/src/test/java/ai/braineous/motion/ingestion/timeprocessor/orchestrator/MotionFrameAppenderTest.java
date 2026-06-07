package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameAppendResult;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MotionFrameAppenderTest {

    @Test
    void test_1_append_validFrameAndEvent_appendsEvent() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionFrame frame =
                newFrame();

        MotionEvent event =
                newEvent("event-1");

        MotionFrameAppendResult result =
                appender.append(frame, event);

        Console.log("frame", frame.toString());
        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "EVENT_APPENDED",
                result.getCode());

        assertEquals(
                "frame-1:append",
                result.getResultId());

        assertNotNull(
                result.getMotionFrame());

        assertNotNull(
                result.getMotionFrame().getMotionEvents());

        assertEquals(
                1,
                result.getMotionFrame().getMotionEvents().size());

        assertEquals(
                "event-1",
                result.getMotionFrame()
                        .getMotionEvents()
                        .get(0)
                        .getEventId());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_2_append_frameWithExistingEvents_preservesAndAppends() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionFrame frame =
                newFrame();

        frame.setMotionEvents(
                new ArrayList<MotionEvent>());

        frame.getMotionEvents().add(
                newEvent("event-existing"));

        MotionEvent event =
                newEvent("event-2");

        MotionFrameAppendResult result =
                appender.append(frame, event);

        Console.log("frame", frame.toString());
        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "EVENT_APPENDED",
                result.getCode());

        assertNotNull(
                result.getMotionFrame().getMotionEvents());

        assertEquals(
                2,
                result.getMotionFrame()
                        .getMotionEvents()
                        .size());

        assertEquals(
                "event-existing",
                result.getMotionFrame()
                        .getMotionEvents()
                        .get(0)
                        .getEventId());

        assertEquals(
                "event-2",
                result.getMotionFrame()
                        .getMotionEvents()
                        .get(1)
                        .getEventId());
    }

    @Test
    void test_3_append_nullFrame_returnsFailure() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionEvent event =
                newEvent("event-1");

        MotionFrameAppendResult result =
                appender.append(
                        null,
                        event);

        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "FRAME_NOT_FOUND",
                result.getCode());

        assertEquals(
                "MotionFrame cannot be null",
                result.getReason());

        assertNull(
                result.getMotionFrame());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_4_append_nullEvent_returnsFailure() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionFrame frame =
                newFrame();

        MotionFrameAppendResult result =
                appender.append(
                        frame,
                        null);

        Console.log("frame", frame.toString());
        Console.log("event", String.valueOf((Object) null));
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "EVENT_NOT_FOUND",
                result.getCode());

        assertEquals(
                "MotionEvent cannot be null",
                result.getReason());

        assertNull(
                result.getMotionFrame());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_5_append_frameWithNullEventList_initializesListAndAppends() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionFrame frame =
                newFrame();

        frame.setMotionEvents(null);

        MotionEvent event =
                newEvent("event-5");

        MotionFrameAppendResult result =
                appender.append(
                        frame,
                        event);

        Console.log("frame", frame.toString());
        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertNotNull(
                result.getMotionFrame()
                        .getMotionEvents());

        assertEquals(
                1,
                result.getMotionFrame()
                        .getMotionEvents()
                        .size());

        assertEquals(
                "event-5",
                result.getMotionFrame()
                        .getMotionEvents()
                        .get(0)
                        .getEventId());
    }

    @Test
    void test_6_append_blankFrameId_usesUnknownResultId() {

        MotionFrameAppender appender =
                new MotionFrameAppender();

        MotionFrame frame =
                newFrame();

        frame.setFrameId("   ");

        MotionEvent event =
                newEvent("event-6");

        MotionFrameAppendResult result =
                appender.append(
                        frame,
                        event);

        Console.log("frame", frame.toString());
        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "unknown:append",
                result.getResultId());

        assertEquals(
                1,
                result.getMotionFrame()
                        .getMotionEvents()
                        .size());
    }

    private MotionFrame newFrame() {

        MotionFrame frame =
                new MotionFrame();

        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        return frame;
    }

    private MotionEvent newEvent(String eventId) {

        MotionEvent event =
                new MotionEvent();

        event.setEventId(eventId);
        event.setEventType("ORDER_UPDATED");
        event.setOccurredAt("2026-06-06T12:00:00Z");
        event.setSubjectId("ORDER-1001");
        event.setSubjectType("ORDER");
        event.setOperation("UPDATE");
        event.setPayloadJson("{\"status\":\"UPDATED\"}");
        event.setMetadataJson("{\"runtime\":\"motion\"}");

        return event;
    }
}