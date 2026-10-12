package ai.braineous.motion.perception.cognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnalysisQueryStoreTest {

    @Test
    public void test_1() {
        AnalysisQueryStore store = new AnalysisQueryStore();

        Assertions.assertDoesNotThrow(
                () -> store.put(null, "select 1"));
        Assertions.assertDoesNotThrow(
                () -> store.put("", "select 1"));
        Assertions.assertDoesNotThrow(
                () -> store.put("   ", "select 1"));
    }

    @Test
    public void test_2() {
        AnalysisQueryStore store = new AnalysisQueryStore();

        Assertions.assertNull(store.get(null));
        Assertions.assertNull(store.get(""));
        Assertions.assertNull(store.get("   "));
    }

    @Test
    public void test_3() {
        AnalysisQueryStore store = new AnalysisQueryStore();

        Assertions.assertDoesNotThrow(
                () -> store.remove(null));
        Assertions.assertDoesNotThrow(
                () -> store.remove(""));
        Assertions.assertDoesNotThrow(
                () -> store.remove("   "));
    }
}
