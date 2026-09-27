package ai.braineous.motion.perception.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompareTest {

    @Test
    public void test_1() {
        Compare compare = new Compare();

        Assertions.assertNull(compare.getLeft());
        Assertions.assertNull(compare.getRight());
    }

    @Test
    public void test_2() {
        Compare compare = new Compare();
        Perception left = new Perception();
        HistoricalView right = new HistoricalView();

        compare.setLeft(left);
        compare.setRight(right);

        Assertions.assertSame(left, compare.getLeft());
        Assertions.assertSame(right, compare.getRight());
    }

    @Test
    public void test_3() {
        Compare compare = new Compare();
        Perception left = new Perception();
        HistoricalView right = new HistoricalView();

        compare.setLeft(left);
        compare.setRight(right);

        String representation = compare.toString();
        Assertions.assertTrue(representation.contains("Compare"));
        Assertions.assertTrue(representation.contains("left="));
        Assertions.assertTrue(representation.contains("right="));
    }
}
