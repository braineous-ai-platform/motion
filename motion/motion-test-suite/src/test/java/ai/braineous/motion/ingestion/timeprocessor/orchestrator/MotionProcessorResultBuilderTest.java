package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameAppendResult;
import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MotionProcessorResultBuilderTest {

    @Test
    void test_1_build_successAppendResult_returnsProcessorResult() {

        MotionProcessorResultBuilder builder =
                new MotionProcessorResultBuilder();

        MotionFrameAppendResult appendResult =
                newAppendResult();

        MotionProcessorResult result =
                builder.build(appendResult);

        Console.log("appendResult", appendResult.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "frame-1:append:processor",
                result.getResultId());

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "EVENT_APPENDED",
                result.getCode());

        assertEquals(
                "MotionEvent appended to MotionFrame",
                result.getReason());

        assertNotNull(
                result.getAppendResultJson());

        assertNotNull(
                result.getMotionFrameJson());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_2_build_failureAppendResult_returnsFailureProcessorResult() {

        MotionProcessorResultBuilder builder =
                new MotionProcessorResultBuilder();

        MotionFrameAppendResult appendResult =
                new MotionFrameAppendResult();

        appendResult.setResultId("failure-1");
        appendResult.setStatus("FAILURE");
        appendResult.setCode("FRAME_NOT_FOUND");
        appendResult.setReason("MotionFrame cannot be null");
        appendResult.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionProcessorResult result =
                builder.build(appendResult);

        Console.log("appendResult", appendResult.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "failure-1:processor",
                result.getResultId());

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "FRAME_NOT_FOUND",
                result.getCode());

        assertEquals(
                "MotionFrame cannot be null",
                result.getReason());

        assertNotNull(
                result.getAppendResultJson());

        assertNull(
                result.getMotionFrameJson());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_3_build_nullAppendResult_returnsFailure() {

        MotionProcessorResultBuilder builder =
                new MotionProcessorResultBuilder();

        MotionProcessorResult result =
                builder.build(null);

        Console.log("appendResult", String.valueOf((Object) null));
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "APPEND_RESULT_NOT_FOUND",
                result.getCode());

        assertEquals(
                "MotionFrameAppendResult cannot be null",
                result.getReason());

        assertNull(
                result.getAppendResultJson());

        assertNull(
                result.getMotionFrameJson());

        assertEquals(
                "{\"runtime\":\"motion\"}",
                result.getMetadataJson());
    }

    @Test
    void test_4_build_blankAppendResultId_usesUnknownProcessorResultId() {

        MotionProcessorResultBuilder builder =
                new MotionProcessorResultBuilder();

        MotionFrameAppendResult appendResult =
                newAppendResult();

        appendResult.setResultId("   ");

        MotionProcessorResult result =
                builder.build(appendResult);

        Console.log("appendResult", appendResult.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "unknown:processor",
                result.getResultId());

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "EVENT_APPENDED",
                result.getCode());

        assertNotNull(
                result.getAppendResultJson());

        assertNotNull(
                result.getMotionFrameJson());
    }

    @Test
    void test_5_build_successAppendResultWithoutFrame_skipsMotionFrameJson() {

        MotionProcessorResultBuilder builder =
                new MotionProcessorResultBuilder();

        MotionFrameAppendResult appendResult =
                newAppendResult();

        appendResult.setMotionFrame(null);

        MotionProcessorResult result =
                builder.build(appendResult);

        Console.log("appendResult", appendResult.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "frame-1:append:processor",
                result.getResultId());

        assertEquals(
                "SUCCESS",
                result.getStatus());

        assertEquals(
                "EVENT_APPENDED",
                result.getCode());

        assertNotNull(
                result.getAppendResultJson());

        assertNull(
                result.getMotionFrameJson());
    }

    private MotionFrameAppendResult newAppendResult() {

        MotionFrame frame =
                new MotionFrame();

        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");
        frame.setMotionEvents(
                new ArrayList<MotionEvent>());

        MotionEvent event =
                new MotionEvent();

        event.setEventId("event-1");
        event.setEventType("ORDER_UPDATED");
        event.setOccurredAt("2026-06-06T12:00:00Z");
        event.setSubjectId("ORDER-1001");
        event.setSubjectType("ORDER");
        event.setOperation("UPDATE");
        event.setPayloadJson("{\"status\":\"UPDATED\"}");
        event.setMetadataJson("{\"runtime\":\"motion\"}");

        frame.getMotionEvents().add(event);

        MotionFrameAppendResult appendResult =
                new MotionFrameAppendResult();

        appendResult.setResultId("frame-1:append");
        appendResult.setStatus("SUCCESS");
        appendResult.setCode("EVENT_APPENDED");
        appendResult.setReason("MotionEvent appended to MotionFrame");
        appendResult.setFrameCreated("false");
        appendResult.setFrameReused("true");
        appendResult.setMotionFrame(frame);
        appendResult.setMetadataJson("{\"runtime\":\"motion\"}");

        return appendResult;
    }
}