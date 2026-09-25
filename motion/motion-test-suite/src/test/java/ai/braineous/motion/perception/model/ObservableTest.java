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

        Assertions.assertNull(observable.getFacts());
    }

    @Test
    public void test_2() {
        Observable observable = new Observable();
        Fact fact = new Fact("MotionEvent:event-1", "event payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(fact);

        observable.setFacts(facts);

        Assertions.assertEquals(1, observable.getFacts().size());
        Assertions.assertSame(fact, observable.getFacts().get(0));
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

        observable.setFacts(facts);

        Assertions.assertEquals(3, observable.getFacts().size());
        Assertions.assertSame(first, observable.getFacts().get(0));
        Assertions.assertSame(second, observable.getFacts().get(1));
        Assertions.assertSame(third, observable.getFacts().get(2));
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

        observable.setFacts(facts);

        Fact carried = observable.getFacts().get(0);
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

        observable.setFacts(facts);

        Assertions.assertEquals(2, observable.getFacts().size());
        Assertions.assertSame(duplicate, observable.getFacts().get(0));
        Assertions.assertSame(duplicate, observable.getFacts().get(1));
    }

    @Test
    public void test_6() {
        Observable observable = new Observable();
        List<Fact> facts = new ArrayList<Fact>();

        observable.setFacts(facts);

        Assertions.assertNotNull(observable.getFacts());
        Assertions.assertTrue(observable.getFacts().isEmpty());
    }

    @Test
    public void test_7() {
        Observable observable = new Observable();
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(new Fact("MotionFrame:frame-1", "frame payload"));

        observable.setFacts(facts);

        Assertions.assertSame(facts, observable.getFacts());
    }

    @Test
    public void test_8() {
        Observable observable = new Observable();
        Fact fact = new Fact(
                "MotionEvent:event-1",
                "representative event payload");
        List<Fact> facts = new ArrayList<Fact>();
        facts.add(fact);

        observable.setFacts(facts);

        String representation = observable.toString();
        Assertions.assertTrue(representation.contains("facts="));
        Assertions.assertTrue(representation.contains("MotionEvent:event-1"));
        Assertions.assertTrue(representation.contains("representative event payload"));
    }
}
