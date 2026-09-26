package ai.braineous.motion.ingestion.sinkprocessor.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationalViewTest {

    @Test
    public void test_1() {

        String windowStart = "2026-09-12T10:00:00Z";
        String windowEnd = "2026-09-12T11:00:00Z";
        String materializedAt = "2026-09-12T11:00:01Z";

        Map<String, Object> state = new HashMap<String, Object>();
        state.put("status", "ACTIVE");
        state.put("eventCount", Integer.valueOf(2));

        List<String> sourceFrameIds = new ArrayList<String>();
        sourceFrameIds.add("frame-1");
        sourceFrameIds.add("frame-2");

        List<String> sourceEventIds = new ArrayList<String>();
        sourceEventIds.add("event-1");
        sourceEventIds.add("event-2");

        OperationalView operationalView = new OperationalView();

        operationalView.setViewId("operational-view-1");
        operationalView.setRoutingKey("ORDER-1001:ORDER:ORDER_OPERATION_FRAME");
        operationalView.setContextId("context-1");
        operationalView.setWindowStart(windowStart);
        operationalView.setWindowEnd(windowEnd);
        operationalView.setState(state);
        operationalView.setSourceFrameIds(sourceFrameIds);
        operationalView.setSourceEventIds(sourceEventIds);
        operationalView.setMaterializedAt(materializedAt);

        Assertions.assertEquals("operational-view-1", operationalView.getViewId());
        Assertions.assertEquals(
                "ORDER-1001:ORDER:ORDER_OPERATION_FRAME",
                operationalView.getRoutingKey());
        Assertions.assertEquals("context-1", operationalView.getContextId());
        Assertions.assertEquals(windowStart, operationalView.getWindowStart());
        Assertions.assertEquals(windowEnd, operationalView.getWindowEnd());
        Assertions.assertEquals(state, operationalView.getState());
        Assertions.assertEquals(sourceFrameIds, operationalView.getSourceFrameIds());
        Assertions.assertEquals(sourceEventIds, operationalView.getSourceEventIds());
        Assertions.assertEquals(materializedAt, operationalView.getMaterializedAt());

        Console.log("operationalView", operationalView);
    }

    @Test
    public void test_2() {
        OperationalView operationalView = new OperationalView();

        Assertions.assertNull(operationalView.getObservedView());
    }

    @Test
    public void test_3() {
        OperationalView operationalView = new OperationalView();
        Fact fact = new Fact("MotionEvent:event-1", "event payload");
        List<Fact> observedView = new ArrayList<Fact>();
        observedView.add(fact);

        operationalView.setObservedView(observedView);

        Assertions.assertEquals(1, operationalView.getObservedView().size());
        Assertions.assertSame(fact, operationalView.getObservedView().get(0));
    }

    @Test
    public void test_4() {
        OperationalView operationalView = new OperationalView();
        Fact first = new Fact("MotionEvent:event-1", "first payload");
        Fact second = new Fact("MotionEvent:event-2", "second payload");
        Fact third = new Fact("MotionEvent:event-3", "third payload");
        List<Fact> observedView = new ArrayList<Fact>();
        observedView.add(first);
        observedView.add(second);
        observedView.add(third);

        operationalView.setObservedView(observedView);

        Assertions.assertEquals(3, operationalView.getObservedView().size());
        Assertions.assertSame(first, operationalView.getObservedView().get(0));
        Assertions.assertSame(second, operationalView.getObservedView().get(1));
        Assertions.assertSame(third, operationalView.getObservedView().get(2));
    }

    @Test
    public void test_5() {
        OperationalView operationalView = new OperationalView();
        List<Fact> observedView = new ArrayList<Fact>();

        operationalView.setObservedView(observedView);

        Assertions.assertNotNull(operationalView.getObservedView());
        Assertions.assertTrue(operationalView.getObservedView().isEmpty());
    }

    @Test
    public void test_6() {
        OperationalView operationalView = new OperationalView();
        List<Fact> observedView = new ArrayList<Fact>();
        observedView.add(new Fact("MotionFrame:frame-1", "frame payload"));

        operationalView.setObservedView(observedView);

        Assertions.assertSame(observedView, operationalView.getObservedView());
    }

    @Test
    public void test_7() {
        OperationalView operationalView = new OperationalView();
        Fact fact = new Fact(
                "MotionEvent:event-1",
                "representative event payload");
        List<Fact> observedView = new ArrayList<Fact>();
        observedView.add(fact);

        operationalView.setObservedView(observedView);

        String representation = operationalView.toString();
        Assertions.assertTrue(representation.contains("observedView="));
        Assertions.assertTrue(representation.contains("MotionEvent:event-1"));
        Assertions.assertTrue(representation.contains("representative event payload"));
    }
}
