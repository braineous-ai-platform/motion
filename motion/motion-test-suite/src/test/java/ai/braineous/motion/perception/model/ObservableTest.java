package ai.braineous.motion.perception.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObservableTest {

    @Test
    public void test_1() {
        Observable observable = new Observable();

        Assertions.assertNull(observable.getObservableFacts());
        Assertions.assertNull(observable.getObservableFactAnchor());
    }

    @Test
    public void test_2() {
        Observable observable = new Observable();
        Fact fact = new Fact("MotionEvent:event-1", "event payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(fact);

        observable.setObservableFacts(facts);

        Assertions.assertEquals(1, observable.getObservableFacts().size());
        Assertions.assertSame(fact, observable.getObservableFacts().get(0));
    }

    @Test
    public void test_3() {
        Observable observable = new Observable();
        Fact first = new Fact("MotionEvent:event-1", "first payload");
        Fact second = new Fact("MotionEvent:event-2", "second payload");
        Fact third = new Fact("MotionEvent:event-3", "third payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(first);
        facts.add(second);
        facts.add(third);

        observable.setObservableFacts(facts);

        Assertions.assertEquals(3, observable.getObservableFacts().size());
        Assertions.assertSame(first, observable.getObservableFacts().get(0));
        Assertions.assertSame(second, observable.getObservableFacts().get(1));
        Assertions.assertSame(third, observable.getObservableFacts().get(2));
    }

    @Test
    public void test_4() {
        Observable observable = new Observable();
        Set<String> attributes = new HashSet<String>();
        attributes.add("subject:ORDER-1001");
        attributes.add("operation:CREATED");
        Fact fact = new Fact(
                "MotionEvent:event-1",
                "{\"eventType\":\"ORDER_CREATED\"}",
                attributes,
                "atomic");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(fact);

        observable.setObservableFacts(facts);

        Fact carried = observable.getObservableFacts().get(0);
        Assertions.assertEquals("MotionEvent:event-1", carried.getId());
        Assertions.assertEquals(
                "{\"eventType\":\"ORDER_CREATED\"}",
                carried.getText());
        Assertions.assertEquals("atomic", carried.getMode());
        Assertions.assertEquals(attributes, carried.getAttributes());
    }

    @Test
    public void test_5() {
        Observable observable = new Observable();
        Fact duplicate = new Fact("MotionEvent:event-1", "event payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(duplicate);
        facts.add(duplicate);

        observable.setObservableFacts(facts);

        Assertions.assertEquals(2, observable.getObservableFacts().size());
        Assertions.assertSame(duplicate, observable.getObservableFacts().get(0));
        Assertions.assertSame(duplicate, observable.getObservableFacts().get(1));
    }

    @Test
    public void test_6() {
        Observable observable = new Observable();
        List<Fact> facts = new ArrayList<Fact>();

        observable.setObservableFacts(facts);

        Assertions.assertNotNull(observable.getObservableFacts());
        Assertions.assertTrue(observable.getObservableFacts().isEmpty());
    }

    @Test
    public void test_7() {
        Observable observable = new Observable();
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(new Fact("MotionFrame:frame-1", "frame payload"));

        observable.setObservableFacts(facts);

        Assertions.assertSame(facts, observable.getObservableFacts());
    }

    @Test
    public void test_8() {
        Observable observable = new Observable();
        Fact fact = new Fact(
                "MotionEvent:event-1",
                "representative event payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(fact);

        observable.setObservableFacts(facts);

        String representation = observable.toString();
        Assertions.assertTrue(representation.contains("observableFactAnchor="));
        Assertions.assertTrue(representation.contains("observableFacts="));
        Assertions.assertTrue(representation.contains("MotionEvent:event-1"));
        Assertions.assertTrue(representation.contains("representative event payload"));
    }

    @Test
    public void test_9() {
        Observable observable = new Observable();
        Fact anchor = new Fact("MotionEvent:event-1", "anchor payload");

        observable.setObservableFactAnchor(anchor);

        Assertions.assertSame(anchor, observable.getObservableFactAnchor());
        Assertions.assertNull(observable.getObservableFacts());
    }
}
