package ai.braineous.motion.ingestion.eventprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RawEventTest {

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

        Console.log("rawEvent", rawEvent.toString());
        Console.log("rawEventJson", rawEvent.toJson());

        assertEquals("raw-event-1", rawEvent.getRawEventId());
        assertEquals("order-service", rawEvent.getSource());
        assertEquals("api", rawEvent.getSourceType());
        assertEquals("ORDER_CREATED", rawEvent.getEventType());
        assertEquals("2026-05-28T10:00:00Z", rawEvent.getReceivedAt());
        assertEquals("{\"orderId\":\"ORD-1001\"}", rawEvent.getPayloadJson());
        assertEquals("{\"correlationId\":\"corr-1001\"}", rawEvent.getMetadataJson());
    }

    @Test
    public void test_2() {

        RawEvent rawEvent = new RawEvent();

        Console.log("rawEvent", rawEvent.toString());
        Console.log("rawEventJson", rawEvent.toJson());

        assertNotNull(rawEvent.toString());
        assertNotNull(rawEvent.toJson());
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

        String json = rawEvent.toJson();

        Console.log("rawEventJson", json);

        RawEvent restored = RawEvent.fromJson(json, RawEvent.class);

        Console.log("restoredRawEvent", restored.toString());

        assertEquals("raw-event-2", restored.getRawEventId());
        assertEquals("device-gateway", restored.getSource());
        assertEquals("connector", restored.getSourceType());
        assertEquals("DEVICE_SIGNAL", restored.getEventType());
        assertEquals("2026-05-28T10:05:00Z", restored.getReceivedAt());
        assertEquals("{\"deviceId\":\"DEV-9001\",\"signal\":\"ACTIVE\"}", restored.getPayloadJson());
        assertEquals("{\"tenantId\":\"tenant-1\"}", restored.getMetadataJson());
    }
}