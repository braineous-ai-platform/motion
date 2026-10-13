package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.SystemAnalysis;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnalysisStoreTest {

    @Test
    public void test_1() {
        AnalysisStore store = new AnalysisStore();

        Assertions.assertDoesNotThrow(
                () -> store.put(null));

        SystemAnalysis nullId = new SystemAnalysis();
        nullId.setAnalysisId(null);
        Assertions.assertDoesNotThrow(
                () -> store.put(nullId));

        SystemAnalysis blankId = new SystemAnalysis();
        blankId.setAnalysisId("");
        Assertions.assertDoesNotThrow(
                () -> store.put(blankId));

        SystemAnalysis whitespaceId = new SystemAnalysis();
        whitespaceId.setAnalysisId("   ");
        Assertions.assertDoesNotThrow(
                () -> store.put(whitespaceId));
    }

    @Test
    public void test_2() {
        AnalysisStore store = new AnalysisStore();

        Assertions.assertNull(store.get(null));
        Assertions.assertNull(store.get(""));
        Assertions.assertNull(store.get("   "));
    }

    @Test
    public void test_3() {
        AnalysisStore store = new AnalysisStore();

        Assertions.assertDoesNotThrow(
                () -> store.remove(null));
        Assertions.assertDoesNotThrow(
                () -> store.remove(""));
        Assertions.assertDoesNotThrow(
                () -> store.remove("   "));
    }
}
