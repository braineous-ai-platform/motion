package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnalysisGroupStoreTest {

    @Test
    public void test_1() {
        AnalysisGroupStore store = new AnalysisGroupStore();

        Assertions.assertDoesNotThrow(
                () -> store.put(null));

        CognitiveTransactionGroup nullName = new CognitiveTransactionGroup();
        nullName.setName(null);
        Assertions.assertDoesNotThrow(
                () -> store.put(nullName));

        CognitiveTransactionGroup blankName = new CognitiveTransactionGroup();
        blankName.setName("");
        Assertions.assertDoesNotThrow(
                () -> store.put(blankName));

        CognitiveTransactionGroup whitespaceName = new CognitiveTransactionGroup();
        whitespaceName.setName("   ");
        Assertions.assertDoesNotThrow(
                () -> store.put(whitespaceName));
    }

    @Test
    public void test_2() {
        AnalysisGroupStore store = new AnalysisGroupStore();

        Assertions.assertNull(store.get(null));
        Assertions.assertNull(store.get(""));
        Assertions.assertNull(store.get("   "));
    }

    @Test
    public void test_3() {
        AnalysisGroupStore store = new AnalysisGroupStore();

        Assertions.assertDoesNotThrow(
                () -> store.remove(null));
        Assertions.assertDoesNotThrow(
                () -> store.remove(""));
        Assertions.assertDoesNotThrow(
                () -> store.remove("   "));
    }
}
