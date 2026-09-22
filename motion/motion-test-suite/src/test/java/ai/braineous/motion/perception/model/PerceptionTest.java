package ai.braineous.motion.perception.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerceptionTest {

    @Test
    public void test_1() {
        Perception perception = new Perception();

        Console.log("PerceptionTest", "new perception");

        assertNull(perception.getObservation());
    }

    @Test
    public void test_2() {
        Perception perception = new Perception();
        Observation observation = new Observation();

        perception.setObservation(observation);

        Console.log("PerceptionTest", "observation assigned");

        assertSame(observation, perception.getObservation());
    }

    @Test
    public void test_3() {
        Perception perception = new Perception();
        Observation observation = new Observation();

        perception.setObservation(observation);

        Console.log("PerceptionTest", perception.toString());

        assertTrue(
                perception.toString().contains(
                        "observation=" + observation));
    }
}
