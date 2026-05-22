package io.braineous.motion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EvolvingContextTest {

    @Test
    public void test_1() {
        EvolvingContext context = new EvolvingContext();

        context.setContextId("context-1");
        context.setContextType("ORDER_EVOLUTION_CONTEXT");
        context.setSubjectId("order-1001");
        context.setSubjectType("ORDER");
        context.setStatus("ACTIVE");
        context.setFramesJson("[{\"frameId\":\"frame-1\"}]");
        context.setMetadataJson("{\"runtime\":\"motion\"}");

        assertEquals("context-1", context.getContextId());
        assertEquals("ORDER_EVOLUTION_CONTEXT", context.getContextType());
        assertEquals("order-1001", context.getSubjectId());
        assertEquals("ORDER", context.getSubjectType());
        assertEquals("ACTIVE", context.getStatus());
        assertEquals("[{\"frameId\":\"frame-1\"}]", context.getFramesJson());
        assertEquals("{\"runtime\":\"motion\"}", context.getMetadataJson());
    }

    @Test
    public void test_2() {
        EvolvingContext context = new EvolvingContext();

        assertNotNull(context.toString());
    }
}
