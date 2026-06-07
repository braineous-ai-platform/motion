package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningRecord;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningStore;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TimeProcessorOrchestratorIT {

    @Inject
    TimeProcessorOrchestrator orchestrator;

    @Inject
    MotionHappeningStore store;

    @Test
    void test_1_process_validEvent_returnsSuccessAndPersistsFrame() {

        store.clear();

        MotionEvent event =
                newEvent(
                        "event-1",
                        "ORDER-1001");

        MotionProcessorResult result =
                orchestrator.process(event);

        List<MotionHappeningRecord> records =
                store.findBySubject(
                        "ORDER-1001",
                        "ORDER");

        Console.log("event", event.toString());
        Console.log("result", result.toString());
        Console.log("records", String.valueOf(records.size()));

        assertNotNull(result);

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

        assertEquals(
                1,
                records.size());

        MotionHappeningRecord record =
                records.get(0);

        Console.log("record", record.toString());

        assertEquals(
                "ORDER-1001",
                record.getSubjectId());

        assertEquals(
                "ORDER",
                record.getSubjectType());

        assertNotNull(
                record.getMotionFrame());
    }

    @Test
    void test_2_process_nullEvent_returnsFailure() {

        store.clear();

        MotionProcessorResult result =
                orchestrator.process(null);

        Console.log("event", String.valueOf((Object) null));
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "ROUTING_KEY_NOT_FOUND",
                result.getCode());

        assertEquals(
                "MotionFrameRoutingKey cannot be resolved",
                result.getReason());
    }

    @Test
    void test_3_process_blankSubjectId_returnsFailure() {

        store.clear();

        MotionEvent event =
                newEvent(
                        "event-1",
                        "   ");

        MotionProcessorResult result =
                orchestrator.process(event);

        Console.log("event", event.toString());
        Console.log("result", result.toString());

        assertNotNull(result);

        assertEquals(
                "FAILURE",
                result.getStatus());

        assertEquals(
                "ROUTING_KEY_NOT_FOUND",
                result.getCode());
    }

    @Test
    void test_4_process_sameRoutingKey_reusesFrameAndAppendsEvents() {

        store.clear();

        MotionEvent event1 =
                newEvent(
                        "event-1",
                        "ORDER-1001");

        MotionEvent event2 =
                newEvent(
                        "event-2",
                        "ORDER-1001");

        MotionProcessorResult result1 =
                orchestrator.process(event1);

        MotionProcessorResult result2 =
                orchestrator.process(event2);

        List<MotionHappeningRecord> records =
                store.findBySubject(
                        "ORDER-1001",
                        "ORDER");

        Console.log("event1", event1.toString());
        Console.log("event2", event2.toString());

        Console.log("result1", result1.toString());
        Console.log("result2", result2.toString());

        Console.log("records", String.valueOf(records.size()));

        assertNotNull(result2);

        assertEquals(
                "SUCCESS",
                result2.getStatus());

        assertTrue(
                records.size() >= 1);

        MotionHappeningRecord latest =
                records.get(records.size() - 1);

        Console.log("record", latest.toString());

        assertNotNull(
                latest.getMotionFrame());

        assertNotNull(
                latest.getMotionFrame().getMotionEvents());

        assertTrue(
                latest.getMotionFrame()
                        .getMotionEvents()
                        .size() >= 2);
    }

    private MotionEvent newEvent(
            String eventId,
            String subjectId) {

        MotionEvent event =
                new MotionEvent();

        event.setEventId(eventId);
        event.setEventType("ORDER_UPDATED");
        event.setOccurredAt("2026-06-06T12:00:00Z");

        event.setSubjectId(subjectId);
        event.setSubjectType("ORDER");

        event.setOperation("UPDATE");

        event.setPayloadJson(
                "{\"status\":\"UPDATED\"}");

        event.setMetadataJson(
                "{\"runtime\":\"motion\"}");

        return event;
    }
}