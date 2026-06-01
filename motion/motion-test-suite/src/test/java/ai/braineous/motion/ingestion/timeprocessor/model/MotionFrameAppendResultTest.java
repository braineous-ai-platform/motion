package ai.braineous.motion.ingestion.timeprocessor.model;

import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.MotionFrame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MotionFrameAppendResultTest {

    @Test
    public void test_1() {

        MotionFrame motionFrame = new MotionFrame();
        motionFrame.setFrameId("frame-1001");
        motionFrame.setFrameType("CUSTOMER_ACTIVITY");
        motionFrame.setWindowStart("2026-01-01T10:00:00Z");
        motionFrame.setWindowEnd("2026-01-01T10:10:00Z");
        motionFrame.setSequence("1");
        motionFrame.setStatus("ACTIVE");
        motionFrame.setMetadataJson("{\"source\":\"timeprocessor-ut\"}");

        MotionFrameAppendResult appendResult = new MotionFrameAppendResult();

        appendResult.setResultId("append-result-1001");
        appendResult.setStatus("SUCCESS");
        appendResult.setCode("FRAME_APPENDED");
        appendResult.setReason("MotionEvent appended to resolved active MotionFrame.");
        appendResult.setFrameCreated("false");
        appendResult.setFrameReused("true");
        appendResult.setMotionFrame(motionFrame);
        appendResult.setMetadataJson("{\"test\":\"MotionFrameAppendResultTest\"}");

        Assertions.assertEquals("append-result-1001", appendResult.getResultId());
        Assertions.assertEquals("SUCCESS", appendResult.getStatus());
        Assertions.assertEquals("FRAME_APPENDED", appendResult.getCode());
        Assertions.assertEquals("MotionEvent appended to resolved active MotionFrame.", appendResult.getReason());
        Assertions.assertEquals("false", appendResult.getFrameCreated());
        Assertions.assertEquals("true", appendResult.getFrameReused());
        Assertions.assertNotNull(appendResult.getMotionFrame());
        Assertions.assertEquals("frame-1001", appendResult.getMotionFrame().getFrameId());
        Assertions.assertEquals("CUSTOMER_ACTIVITY", appendResult.getMotionFrame().getFrameType());
        Assertions.assertEquals("{\"test\":\"MotionFrameAppendResultTest\"}", appendResult.getMetadataJson());

        Console.log("appendResult", appendResult);

        Assertions.assertNotNull(appendResult.toString());
        Assertions.assertTrue(appendResult.toString().contains("append-result-1001"));
        Assertions.assertTrue(appendResult.toString().contains("SUCCESS"));
        Assertions.assertTrue(appendResult.toString().contains("FRAME_APPENDED"));
        Assertions.assertTrue(appendResult.toString().contains("frameCreated='false'"));
        Assertions.assertTrue(appendResult.toString().contains("frameReused='true'"));
        Assertions.assertTrue(appendResult.toString().contains("frame-1001"));
    }
}