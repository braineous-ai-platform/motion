package ai.braineous.motion.ingestion.eventprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MotionEnvelopeTest {

    @Test
    public void test_1() {

        RawEvent rawEvent = new RawEvent();
        rawEvent.setRawEventId("raw-event-1");
        rawEvent.setSource("order-service");
        rawEvent.setSourceType("api");
        rawEvent.setEventType("ORDER_CREATED");
        rawEvent.setReceivedAt("2026-05-28T10:00:00Z");
        rawEvent.setPayloadJson("{\"orderId\":\"ORD-1001\"}");
        rawEvent.setMetadataJson("{\"correlationId\":\"corr-1001\"}");

        MotionEnvelope envelope = new MotionEnvelope();
        envelope.setEnvelopeId("envelope-1");
        envelope.setTenantId("tenant-1");
        envelope.setCorrelationId("corr-1001");
        envelope.setTraceId("trace-1001");
        envelope.setReceivedAt("2026-05-28T10:00:01Z");
        envelope.setRawEvent(rawEvent);
        envelope.setMetadataJson("{\"pipeline\":\"motion-ingestion\"}");

        Console.log("envelope", envelope.toString());
        Console.log("envelopeJson", envelope.toJson());

        assertEquals("envelope-1", envelope.getEnvelopeId());
        assertEquals("tenant-1", envelope.getTenantId());
        assertEquals("corr-1001", envelope.getCorrelationId());
        assertEquals("trace-1001", envelope.getTraceId());
        assertEquals("2026-05-28T10:00:01Z", envelope.getReceivedAt());

        assertNotNull(envelope.getRawEvent());
        assertEquals("raw-event-1", envelope.getRawEvent().getRawEventId());
        assertEquals("order-service", envelope.getRawEvent().getSource());
        assertEquals("api", envelope.getRawEvent().getSourceType());
        assertEquals("ORDER_CREATED", envelope.getRawEvent().getEventType());
        assertEquals("2026-05-28T10:00:00Z", envelope.getRawEvent().getReceivedAt());
        assertEquals("{\"orderId\":\"ORD-1001\"}", envelope.getRawEvent().getPayloadJson());
        assertEquals("{\"correlationId\":\"corr-1001\"}", envelope.getRawEvent().getMetadataJson());

        assertEquals("{\"pipeline\":\"motion-ingestion\"}", envelope.getMetadataJson());
    }

    @Test
    public void test_2() {

        MotionEnvelope envelope = new MotionEnvelope();

        Console.log("envelope", envelope.toString());
        Console.log("envelopeJson", envelope.toJson());

        assertNotNull(envelope.toString());
        assertNotNull(envelope.toJson());
    }

    @Test
    public void test_3() {

        RawEvent rawEvent = new RawEvent();
        rawEvent.setRawEventId("raw-event-2");
        rawEvent.setSource("device-gateway");
        rawEvent.setSourceType("connector");
        rawEvent.setEventType("DEVICE_SIGNAL");
        rawEvent.setReceivedAt("2026-05-28T10:05:00Z");
        rawEvent.setPayloadJson("{\"deviceId\":\"DEV-9001\",\"signal\":\"ACTIVE\"}");
        rawEvent.setMetadataJson("{\"tenantId\":\"tenant-1\"}");

        MotionEnvelope envelope = new MotionEnvelope();
        envelope.setEnvelopeId("envelope-2");
        envelope.setTenantId("tenant-1");
        envelope.setCorrelationId("corr-2001");
        envelope.setTraceId("trace-2001");
        envelope.setReceivedAt("2026-05-28T10:05:01Z");
        envelope.setRawEvent(rawEvent);
        envelope.setMetadataJson("{\"stage\":\"envelope\"}");

        String json = envelope.toJson();

        Console.log("envelopeJson", json);

        MotionEnvelope restored = MotionEnvelope.fromJson(json, MotionEnvelope.class);

        Console.log("restoredEnvelope", restored.toString());

        assertEquals("envelope-2", restored.getEnvelopeId());
        assertEquals("tenant-1", restored.getTenantId());
        assertEquals("corr-2001", restored.getCorrelationId());
        assertEquals("trace-2001", restored.getTraceId());
        assertEquals("2026-05-28T10:05:01Z", restored.getReceivedAt());

        assertNotNull(restored.getRawEvent());
        assertEquals("raw-event-2", restored.getRawEvent().getRawEventId());
        assertEquals("device-gateway", restored.getRawEvent().getSource());
        assertEquals("connector", restored.getRawEvent().getSourceType());
        assertEquals("DEVICE_SIGNAL", restored.getRawEvent().getEventType());
        assertEquals("2026-05-28T10:05:00Z", restored.getRawEvent().getReceivedAt());
        assertEquals("{\"deviceId\":\"DEV-9001\",\"signal\":\"ACTIVE\"}", restored.getRawEvent().getPayloadJson());
        assertEquals("{\"tenantId\":\"tenant-1\"}", restored.getRawEvent().getMetadataJson());

        assertEquals("{\"stage\":\"envelope\"}", restored.getMetadataJson());
    }
}