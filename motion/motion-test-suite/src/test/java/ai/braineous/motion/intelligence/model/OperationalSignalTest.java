package ai.braineous.motion.intelligence.model;

import ai.braineous.motion.ingestion.intelligence.model.OperationalSignal;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OperationalSignalTest {

    @Test
    public void test_1() {

        OperationalSignal signal = new OperationalSignal();

        signal.setSignalId("signal-1");
        signal.setSignalType("OPERATIONAL_ACTIVITY");
        signal.setSignalLevel("INFO");

        signal.setStatus("DETECTED");
        signal.setCode("SIGNAL_DETECTED");
        signal.setReason("Operational motion produced a detectable signal");
        signal.setMessage("Customer activity increased during the active motion window");

        signal.setContextId("context-1");
        signal.setContextType("CUSTOMER_CONTEXT");
        signal.setSubjectId("customer-1001");
        signal.setSubjectType("CUSTOMER");

        signal.setWindowStart("2026-06-13T10:00:00Z");
        signal.setWindowEnd("2026-06-13T10:10:00Z");
        signal.setDetectedAt("2026-06-13T10:10:05Z");

        signal.setEvidenceJson("{\"frameId\":\"frame-1\",\"eventCount\":\"3\"}");
        signal.setMetadataJson("{\"runtime\":\"motion\",\"layer\":\"intelligence\"}");

        Assertions.assertEquals("signal-1", signal.getSignalId());
        Assertions.assertEquals("OPERATIONAL_ACTIVITY", signal.getSignalType());
        Assertions.assertEquals("INFO", signal.getSignalLevel());

        Assertions.assertEquals("DETECTED", signal.getStatus());
        Assertions.assertEquals("SIGNAL_DETECTED", signal.getCode());
        Assertions.assertEquals("Operational motion produced a detectable signal", signal.getReason());
        Assertions.assertEquals("Customer activity increased during the active motion window", signal.getMessage());

        Assertions.assertEquals("context-1", signal.getContextId());
        Assertions.assertEquals("CUSTOMER_CONTEXT", signal.getContextType());
        Assertions.assertEquals("customer-1001", signal.getSubjectId());
        Assertions.assertEquals("CUSTOMER", signal.getSubjectType());

        Assertions.assertEquals("2026-06-13T10:00:00Z", signal.getWindowStart());
        Assertions.assertEquals("2026-06-13T10:10:00Z", signal.getWindowEnd());
        Assertions.assertEquals("2026-06-13T10:10:05Z", signal.getDetectedAt());

        Assertions.assertEquals("{\"frameId\":\"frame-1\",\"eventCount\":\"3\"}", signal.getEvidenceJson());
        Assertions.assertEquals("{\"runtime\":\"motion\",\"layer\":\"intelligence\"}", signal.getMetadataJson());

        Console.log("signal", signal);

        Assertions.assertNotNull(signal.toString());
        Assertions.assertTrue(signal.toString().contains("signal-1"));
        Assertions.assertTrue(signal.toString().contains("OPERATIONAL_ACTIVITY"));
        Assertions.assertTrue(signal.toString().contains("SIGNAL_DETECTED"));
        Assertions.assertTrue(signal.toString().contains("customer-1001"));
        Assertions.assertTrue(signal.toString().contains("CUSTOMER"));
        Assertions.assertTrue(signal.toString().contains("2026-06-13T10:00:00Z"));
        Assertions.assertTrue(signal.toString().contains("motion"));
    }
}