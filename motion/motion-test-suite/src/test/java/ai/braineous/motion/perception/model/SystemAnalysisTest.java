package ai.braineous.motion.perception.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemAnalysisTest {

    @Test
    public void test_1() {
        SystemAnalysis analysis = new SystemAnalysis();

        Assertions.assertNull(analysis.getPerception());
    }

    @Test
    public void test_2() {
        SystemAnalysis analysis = new SystemAnalysis();
        Perception perception = new Perception();

        analysis.setPerception(perception);

        Assertions.assertSame(perception, analysis.getPerception());
    }

    @Test
    public void test_3() {
        SystemAnalysis analysis = new SystemAnalysis();
        Perception perception = new Perception();

        analysis.setPerception(perception);

        String representation = analysis.toString();
        Assertions.assertTrue(representation.contains("SystemAnalysis"));
        Assertions.assertTrue(representation.contains("perception="));
    }
}
