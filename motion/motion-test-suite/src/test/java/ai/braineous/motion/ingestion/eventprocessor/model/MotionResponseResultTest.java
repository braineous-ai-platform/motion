package ai.braineous.motion.ingestion.eventprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionResponseResultTest {

    @Test
    public void test_1() {

        MotionResponseResult result = new MotionResponseResult();

        result.setResultId("result-1");
        result.setStatus("ACCEPTED");
        result.setReasonCode("INGESTION_ACCEPTED");
        result.setMessage("Event accepted into Motion ingestion pipeline");

        result.setRawEventId("raw-event-1");
        result.setEnvelopeId("envelope-1");
        result.setMotionEventId("motion-event-1");
        result.setCorrelationId("corr-1001");
        result.setTraceId("trace-1001");

        result.setTargetTopic("orders.motion.normalized");
        result.setPartitionKey("ORDER-1001");

        result.setRawEventJson("{\"rawEventId\":\"raw-event-1\"}");
        result.setEnvelopeJson("{\"envelopeId\":\"envelope-1\"}");
        result.setMotionEventJson("{\"eventId\":\"motion-event-1\"}");
        result.setReplaySignalJson("{\"replayable\":\"false\"}");

        result.setMetadataJson("{\"pipeline\":\"motion-ingestion\"}");

        Console.log("responseResult", result.toString());
        Console.log("responseResultJson", result.toJson());

        assertEquals("result-1", result.getResultId());
        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("INGESTION_ACCEPTED", result.getReasonCode());
        assertEquals("Event accepted into Motion ingestion pipeline", result.getMessage());

        assertEquals("raw-event-1", result.getRawEventId());
        assertEquals("envelope-1", result.getEnvelopeId());
        assertEquals("motion-event-1", result.getMotionEventId());
        assertEquals("corr-1001", result.getCorrelationId());
        assertEquals("trace-1001", result.getTraceId());

        assertEquals("orders.motion.normalized", result.getTargetTopic());
        assertEquals("ORDER-1001", result.getPartitionKey());

        assertEquals("{\"rawEventId\":\"raw-event-1\"}", result.getRawEventJson());
        assertEquals("{\"envelopeId\":\"envelope-1\"}", result.getEnvelopeJson());
        assertEquals("{\"eventId\":\"motion-event-1\"}", result.getMotionEventJson());
        assertEquals("{\"replayable\":\"false\"}", result.getReplaySignalJson());

        assertEquals("{\"pipeline\":\"motion-ingestion\"}", result.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionResponseResult result = new MotionResponseResult();

        Console.log("responseResult", result.toString());
        Console.log("responseResultJson", result.toJson());

        assertNotNull(result.toString());
        assertNotNull(result.toJson());
    }

    @Test
    public void test_3() {

        MotionResponseResult result = new MotionResponseResult();

        result.setResultId("result-2");
        result.setStatus("QUARANTINED");
        result.setReasonCode("INVALID_PAYLOAD");
        result.setMessage("Event payload could not be normalized");

        result.setRawEventId("raw-event-2");
        result.setEnvelopeId("envelope-2");
        result.setMotionEventId(null);
        result.setCorrelationId("corr-2001");
        result.setTraceId("trace-2001");

        result.setTargetTopic("orders.motion.quarantine");
        result.setPartitionKey("raw-event-2");

        result.setRawEventJson("{\"rawEventId\":\"raw-event-2\"}");
        result.setEnvelopeJson("{\"envelopeId\":\"envelope-2\"}");
        result.setMotionEventJson(null);
        result.setReplaySignalJson("{\"replayable\":\"false\",\"reasonCode\":\"POISON_INPUT\"}");

        result.setMetadataJson("{\"stage\":\"validation\"}");

        String json = result.toJson();

        Console.log("responseResultJson", json);

        MotionResponseResult restored =
                MotionResponseResult.fromJson(json, MotionResponseResult.class);

        Console.log("restoredResponseResult", restored.toString());

        assertEquals("result-2", restored.getResultId());
        assertEquals("QUARANTINED", restored.getStatus());
        assertEquals("INVALID_PAYLOAD", restored.getReasonCode());
        assertEquals("Event payload could not be normalized", restored.getMessage());

        assertEquals("raw-event-2", restored.getRawEventId());
        assertEquals("envelope-2", restored.getEnvelopeId());
        assertEquals(null, restored.getMotionEventId());
        assertEquals("corr-2001", restored.getCorrelationId());
        assertEquals("trace-2001", restored.getTraceId());

        assertEquals("orders.motion.quarantine", restored.getTargetTopic());
        assertEquals("raw-event-2", restored.getPartitionKey());

        assertEquals("{\"rawEventId\":\"raw-event-2\"}", restored.getRawEventJson());
        assertEquals("{\"envelopeId\":\"envelope-2\"}", restored.getEnvelopeJson());
        assertEquals(null, restored.getMotionEventJson());
        assertEquals("{\"replayable\":\"false\",\"reasonCode\":\"POISON_INPUT\"}", restored.getReplaySignalJson());

        assertEquals("{\"stage\":\"validation\"}", restored.getMetadataJson());
    }
}