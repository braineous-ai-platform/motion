package io.braineous.motion.core.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EvolvingContextTest {

    @Test
    public void test_1() {
        EvolvingContext context = new EvolvingContext();

        context.setContextId("context-1");
        context.setContextType("ORDER_EVOLVING_CONTEXT");
        context.setSubjectId("ORDER-1001");
        context.setSubjectType("ORDER");
        context.setStatus("OPEN");
        context.setMetadataJson("{\"runtime\":\"motion\"}");

        MotionFrame frame = new MotionFrame();
        frame.setFrameId("frame-1");
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setWindowStart("2026-05-22T10:00:00Z");
        frame.setWindowEnd("2026-05-22T10:05:00Z");
        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        context.addMotionFrame(frame);

        assertEquals("context-1", context.getContextId());
        assertEquals("ORDER_EVOLVING_CONTEXT", context.getContextType());
        assertEquals("ORDER-1001", context.getSubjectId());
        assertEquals("ORDER", context.getSubjectType());
        assertEquals("OPEN", context.getStatus());
        assertEquals(1, context.getMotionFrames().size());
        assertEquals("{\"runtime\":\"motion\"}", context.getMetadataJson());

        Console.log("context", context.toString());
    }

    @Test
    public void test_2() {
        EvolvingContext context = new EvolvingContext();

        assertNotNull(context.getMotionFrames());
        assertEquals(0, context.getMotionFrames().size());

        Console.log("context", context.toString());
    }
}