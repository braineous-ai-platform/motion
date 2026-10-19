package ai.braineous.motion.perception.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionBaseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CognitiveQueryArtifactTest {

    @Test
    public void test_1() {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();

        Assertions.assertNull(artifact.getName());
        Assertions.assertNull(artifact.getCognitiveQuery());
    }

    @Test
    public void test_2() {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();

        artifact.setName("pay_v1");

        Assertions.assertEquals("pay_v1", artifact.getName());
    }

    @Test
    public void test_3() {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        String cognitiveQuery = "select decision from llm";

        artifact.setCognitiveQuery(cognitiveQuery);

        Assertions.assertEquals(cognitiveQuery, artifact.getCognitiveQuery());
    }

    @Test
    public void test_4() {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        String cognitiveQuery = "select decision from llm";

        artifact.setName("pay_v1");
        artifact.setCognitiveQuery(cognitiveQuery);

        String representation = artifact.toString();
        Assertions.assertTrue(representation.contains("CognitiveQueryArtifact"));
        Assertions.assertTrue(representation.contains("name="));
        Assertions.assertTrue(representation.contains("pay_v1"));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
        Assertions.assertTrue(representation.contains(cognitiveQuery));
    }

    @Test
    public void test_5() {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        String cognitiveQuery = "select decision from llm";

        artifact.setName("pay_v1");
        artifact.setCognitiveQuery(cognitiveQuery);

        JsonObject root = JsonParser.parseString(artifact.toJson()).getAsJsonObject();

        Assertions.assertEquals(2, root.size());
        Assertions.assertTrue(root.has("name"));
        Assertions.assertTrue(root.has("cognitiveQuery"));
        Assertions.assertTrue(root.get("name").isJsonPrimitive());
        Assertions.assertTrue(root.get("cognitiveQuery").isJsonPrimitive());
        Assertions.assertEquals("pay_v1", root.get("name").getAsString());
        Assertions.assertEquals(cognitiveQuery, root.get("cognitiveQuery").getAsString());
    }

    @Test
    public void test_6() {
        CognitiveQueryArtifact original = new CognitiveQueryArtifact();
        String cognitiveQuery = "select decision from llm";

        original.setName("pay_v1");
        original.setCognitiveQuery(cognitiveQuery);

        String json = original.toJson();
        CognitiveQueryArtifact restored =
                MotionBaseModel.fromJson(json, CognitiveQueryArtifact.class);

        Assertions.assertNotNull(restored);
        Assertions.assertEquals(original.getName(), restored.getName());
        Assertions.assertEquals(
                original.getCognitiveQuery(),
                restored.getCognitiveQuery());
    }

    @Test
    public void test_7() {
        CognitiveQueryArtifact original = new CognitiveQueryArtifact();

        String json = original.toJson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Assertions.assertEquals(2, root.size());
        Assertions.assertTrue(root.has("name"));
        Assertions.assertTrue(root.has("cognitiveQuery"));
        Assertions.assertTrue(root.get("name").isJsonNull());
        Assertions.assertTrue(root.get("cognitiveQuery").isJsonNull());

        CognitiveQueryArtifact restored =
                MotionBaseModel.fromJson(json, CognitiveQueryArtifact.class);

        Assertions.assertNotNull(restored);
        Assertions.assertNull(restored.getName());
        Assertions.assertNull(restored.getCognitiveQuery());
    }
}
