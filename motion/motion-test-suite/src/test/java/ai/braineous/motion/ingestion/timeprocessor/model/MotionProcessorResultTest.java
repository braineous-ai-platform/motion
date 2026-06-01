package ai.braineous.motion.ingestion.timeprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotionProcessorResultTest {

    @Test
    public void test_1() {

        MotionProcessorResult result = new MotionProcessorResult();

        result.setResultId("processor-result-1001");
        result.setStatus("SUCCESS");
        result.setCode("MOTION_FRAME_PROCESSED");
        result.setReason("MotionEvent successfully processed into MotionFrame.");

        result.setAppendResultJson(
                "{\"status\":\"SUCCESS\",\"code\":\"FRAME_APPENDED\"}");

        result.setMotionFrameJson(
                "{\"frameId\":\"frame-1001\",\"frameType\":\"CUSTOMER_ACTIVITY\"}");

        result.setMetadataJson(
                "{\"source\":\"MotionProcessorResultTest\"}");

        Assertions.assertEquals(
                "processor-result-1001",
                result.getResultId());

        Assertions.assertEquals(
                "SUCCESS",
                result.getStatus());

        Assertions.assertEquals(
                "MOTION_FRAME_PROCESSED",
                result.getCode());

        Assertions.assertEquals(
                "MotionEvent successfully processed into MotionFrame.",
                result.getReason());

        Assertions.assertEquals(
                "{\"status\":\"SUCCESS\",\"code\":\"FRAME_APPENDED\"}",
                result.getAppendResultJson());

        Assertions.assertEquals(
                "{\"frameId\":\"frame-1001\",\"frameType\":\"CUSTOMER_ACTIVITY\"}",
                result.getMotionFrameJson());

        Assertions.assertEquals(
                "{\"source\":\"MotionProcessorResultTest\"}",
                result.getMetadataJson());

        Console.log("processorResult", result);

        Assertions.assertNotNull(result.toString());

        Assertions.assertTrue(
                result.toString().contains("processor-result-1001"));

        Assertions.assertTrue(
                result.toString().contains("SUCCESS"));

        Assertions.assertTrue(
                result.toString().contains("MOTION_FRAME_PROCESSED"));

        Assertions.assertTrue(
                result.toString().contains("FRAME_APPENDED"));

        Assertions.assertTrue(
                result.toString().contains("frame-1001"));
    }
}