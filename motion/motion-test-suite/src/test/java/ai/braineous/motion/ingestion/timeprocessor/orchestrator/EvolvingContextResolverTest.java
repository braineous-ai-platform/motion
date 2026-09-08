package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.rag.prompt.observe.Console;
import io.braineous.motion.core.model.EvolvingContext;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EvolvingContextResolverTest {

    @Test
    public void test_1() {
        Console.log("placement", "place normal frame instant");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame frame = frame(
                "frame-1",
                "2026-09-08T13:42:00Z",
                "2026-09-08T13:47:00Z");

        EvolvingContext context = resolver.place(routingKey, frame, null);

        Console.log("contextId", context.getContextId());
        Console.log("contextWindow", context.getContextWindow().toString());

        assertNotNull(context);
        assertNotNull(context.getContextWindow());
        assertEquals("2026-09-08T00:00:00Z", context.getContextWindow().getContextStart());
        assertEquals("2026-09-09T00:00:00Z", context.getContextWindow().getContextEnd());
        assertEquals(1, context.getMotionFrames().size());
        assertEquals(frame, context.getMotionFrames().get(0));
    }

    @Test
    public void test_2() {
        Console.log("placement", "place two frame windows in one context");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame laterFrame = frame(
                "frame-2",
                "2026-09-08T18:00:00Z",
                "2026-09-08T18:05:00Z");
        MotionFrame earlierFrame = frame(
                "frame-1",
                "2026-09-08T02:00:00Z",
                "2026-09-08T02:05:00Z");

        EvolvingContext context = resolver.place(routingKey, laterFrame, null);
        String initialContextId = context.getContextId();
        EvolvingContext evolved = resolver.place(routingKey, earlierFrame, context);

        Console.log("contextId", evolved.getContextId());
        Console.log("membership", evolved.getMotionFrames().toString());

        assertEquals(context, evolved);
        assertEquals(initialContextId, evolved.getContextId());
        assertEquals(2, evolved.getMotionFrames().size());
        assertEquals("frame-1", evolved.getMotionFrames().get(0).getFrameId());
        assertEquals("frame-2", evolved.getMotionFrames().get(1).getFrameId());
    }

    @Test
    public void test_3() {
        Console.log("placement", "place frames across context boundary");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame firstFrame = frame(
                "frame-1",
                "2026-09-08T23:59:59.999Z",
                "2026-09-09T00:00:00Z");
        MotionFrame secondFrame = frame(
                "frame-2",
                "2026-09-09T00:00:00Z",
                "2026-09-09T00:05:00Z");

        EvolvingContext firstContext = resolver.place(routingKey, firstFrame, null);
        EvolvingContext secondContext = resolver.place(routingKey, secondFrame, null);

        Console.log("firstContext", firstContext.getContextId());
        Console.log("secondContext", secondContext.getContextId());

        assertNotEquals(firstContext.getContextId(), secondContext.getContextId());
        assertEquals("2026-09-08T00:00:00Z", firstContext.getContextWindow().getContextStart());
        assertEquals("2026-09-09T00:00:00Z", secondContext.getContextWindow().getContextStart());
    }

    @Test
    public void test_4() {
        Console.log("boundary", "prove half-open context semantics");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame atStart = frame(
                "frame-start",
                "2026-09-08T00:00:00Z",
                "2026-09-08T00:01:00Z");
        MotionFrame atEnd = frame(
                "frame-end",
                "2026-09-09T00:00:00Z",
                "2026-09-09T00:01:00Z");

        EvolvingContext startContext = resolver.place(routingKey, atStart, null);
        EvolvingContext endContext = resolver.place(routingKey, atEnd, null);

        assertEquals("2026-09-08T00:00:00Z", startContext.getContextWindow().getContextStart());
        assertEquals("2026-09-09T00:00:00Z", startContext.getContextWindow().getContextEnd());
        assertEquals("2026-09-09T00:00:00Z", endContext.getContextWindow().getContextStart());
        assertNotEquals(startContext.getContextId(), endContext.getContextId());

        Console.log("boundary", "start included; end placed in next context");
    }

    @Test
    public void test_5() {
        Console.log("membership", "place the same frame twice");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame frame = frame(
                "frame-1",
                "2026-09-08T12:00:00Z",
                "2026-09-08T12:05:00Z");

        EvolvingContext context = resolver.place(routingKey, frame, null);
        resolver.place(routingKey, frame, context);

        assertEquals(1, context.getMotionFrames().size());
        assertEquals("frame-1", context.getMotionFrames().get(0).getFrameId());

        Console.log("membership", "duplicate frame identity excluded");
    }

    @Test
    public void test_6() {
        Console.log("validation", "reject malformed placement inputs");

        final EvolvingContextResolver resolver = new EvolvingContextResolver();
        final MotionFrameRoutingKey routingKey = routingKey();
        final MotionFrame missingWindow = new MotionFrame();
        missingWindow.setFrameId("frame-missing-window");
        final MotionFrame invalidStart = frame(
                "frame-invalid-start",
                "not-an-instant",
                "2026-09-08T12:05:00Z");

        assertThrows(
                IllegalArgumentException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        resolver.place(routingKey, null, null);
                    }
                });

        assertThrows(
                IllegalArgumentException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        resolver.place(routingKey, missingWindow, null);
                    }
                });

        assertThrows(
                IllegalArgumentException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        resolver.place(routingKey, invalidStart, null);
                    }
                });

        Console.log("validation", "all malformed inputs rejected");
    }

    @Test
    public void test_7() {
        Console.log("boundary", "reject cross-boundary context mutation");

        final EvolvingContextResolver resolver = new EvolvingContextResolver();
        final MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame firstFrame = frame(
                "frame-1",
                "2026-09-08T23:00:00Z",
                "2026-09-08T23:05:00Z");
        final MotionFrame nextFrame = frame(
                "frame-2",
                "2026-09-09T00:00:00Z",
                "2026-09-09T00:05:00Z");
        final EvolvingContext context = resolver.place(routingKey, firstFrame, null);

        assertThrows(
                IllegalArgumentException.class,
                new Executable() {
                    @Override
                    public void execute() {
                        resolver.place(routingKey, nextFrame, context);
                    }
                });

        assertEquals(1, context.getMotionFrames().size());
        assertEquals("frame-1", context.getMotionFrames().get(0).getFrameId());
        assertEquals("2026-09-08T00:00:00Z", context.getContextWindow().getContextStart());

        Console.log("boundary", "original context remains unchanged");
    }

    @Test
    public void test_8() {
        Console.log("membership", "retain same frameId across temporal boundaries");

        EvolvingContextResolver resolver = new EvolvingContextResolver();
        MotionFrameRoutingKey routingKey = routingKey();
        MotionFrame laterFrame = frame(
                "shared-frame-id",
                "2026-09-08T16:00:00Z",
                "2026-09-08T16:05:00Z");
        MotionFrame earlierFrame = frame(
                "shared-frame-id",
                "2026-09-08T08:00:00Z",
                "2026-09-08T08:05:00Z");

        EvolvingContext context = resolver.place(routingKey, laterFrame, null);
        resolver.place(routingKey, earlierFrame, context);

        assertEquals(2, context.getMotionFrames().size());
        assertEquals(
                "2026-09-08T08:00:00Z",
                context.getMotionFrames().get(0).getTimeWindow().getWindowStart());
        assertEquals(
                "2026-09-08T16:00:00Z",
                context.getMotionFrames().get(1).getTimeWindow().getWindowStart());
        assertEquals(
                context.getMotionFrames().get(0).getFrameId(),
                context.getMotionFrames().get(1).getFrameId());

        Console.log("membership", "both temporal frames retained in deterministic order");
    }

    private MotionFrameRoutingKey routingKey() {
        MotionFrameRoutingKey routingKey = new MotionFrameRoutingKey();
        routingKey.setRoutingKey("ORDER-1001:ORDER:ORDER_OPERATION_FRAME");
        routingKey.setSubjectId("ORDER-1001");
        routingKey.setSubjectType("ORDER");
        routingKey.setFrameType("ORDER_OPERATION_FRAME");
        return routingKey;
    }

    private MotionFrame frame(
            String frameId,
            String windowStart,
            String windowEnd) {

        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart(windowStart);
        timeWindow.setWindowEnd(windowEnd);

        MotionFrame frame = new MotionFrame();
        frame.setFrameId(frameId);
        frame.setFrameType("ORDER_OPERATION_FRAME");
        frame.setTimeWindow(timeWindow);
        frame.setSequence(frameId);
        frame.setStatus("OPEN");
        return frame;
    }
}
