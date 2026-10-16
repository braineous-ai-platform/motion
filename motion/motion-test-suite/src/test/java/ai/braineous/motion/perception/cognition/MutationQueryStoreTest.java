package ai.braineous.motion.perception.cognition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MutationQueryStoreTest {

    @Test
    public void test_1() {
        MutationQueryStore store = new MutationQueryStore();

        Assertions.assertDoesNotThrow(
                () -> store.put(null, "select 1"));
        Assertions.assertDoesNotThrow(
                () -> store.put("", "select 1"));
        Assertions.assertDoesNotThrow(
                () -> store.put("   ", "select 1"));
    }

    @Test
    public void test_2() {
        MutationQueryStore store = new MutationQueryStore();

        Assertions.assertNull(store.get(null));
        Assertions.assertNull(store.get(""));
        Assertions.assertNull(store.get("   "));
    }

    @Test
    public void test_3() {
        MutationQueryStore store = new MutationQueryStore();

        Assertions.assertDoesNotThrow(
                () -> store.remove(null));
        Assertions.assertDoesNotThrow(
                () -> store.remove(""));
        Assertions.assertDoesNotThrow(
                () -> store.remove("   "));
    }
}
