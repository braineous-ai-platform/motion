package ai.braineous.motion.perception.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionBaseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SystemMutationTest {

    @Test
    public void test_1() {
        SystemMutation mutation = new SystemMutation();

        Assertions.assertNull(mutation.getPerception());
    }

    @Test
    public void test_2() {
        SystemMutation mutation = new SystemMutation();
        Perception perception = new Perception();

        mutation.setPerception(perception);

        Assertions.assertSame(perception, mutation.getPerception());
    }

    @Test
    public void test_3() {
        SystemMutation mutation = new SystemMutation();
        Perception perception = new Perception();

        mutation.setPerception(perception);

        String representation = mutation.toString();
        Assertions.assertTrue(representation.contains("SystemMutation"));
        Assertions.assertTrue(representation.contains("perception="));
    }

    @Test
    public void test_4() {
        SystemMutation mutation = new SystemMutation();

        Assertions.assertNull(mutation.getMutationId());

        mutation.setMutationId("mutation-1");

        Assertions.assertEquals("mutation-1", mutation.getMutationId());

        String representation = mutation.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("mutationId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
        Assertions.assertTrue(representation.contains("cognitiveResultJson="));
    }

    @Test
    public void test_5() {
        SystemMutation mutation = new SystemMutation();

        Assertions.assertNull(mutation.getCreatedAt());

        mutation.setCreatedAt("2026-10-12T12:20:00Z");

        Assertions.assertEquals("2026-10-12T12:20:00Z", mutation.getCreatedAt());

        String representation = mutation.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("mutationId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
        Assertions.assertTrue(representation.contains("cognitiveResultJson="));
    }

    @Test
    public void test_6() {
        SystemMutation mutation = new SystemMutation();
        String payV1Sql =
                "select decision, reason, code "
                        + "from llm "
                        + "where factId = 'PaymentRequest:PAY-1001' "
                        + "and relatedFactIds = 'CustomerAccount:CUST-2001,"
                        + "PaymentMethod:PM-3001,"
                        + "RiskProfile:RISK-4001,"
                        + "MerchantPolicy:POL-5001' "
                        + "control intent = 'decide_payment_capture', "
                        + "action = 'determine', "
                        + "subject = 'primary_payment_request', "
                        + "decision = 'allow_capture', "
                        + "basis = 'related_system_facts', "
                        + "goal = 'decision'";

        Assertions.assertNull(mutation.getCognitiveQuery());

        mutation.setCognitiveQuery(payV1Sql);

        Assertions.assertEquals(payV1Sql, mutation.getCognitiveQuery());

        String representation = mutation.toString();
        Assertions.assertTrue(representation.contains("perception="));
        Assertions.assertTrue(representation.contains("mutationId="));
        Assertions.assertTrue(representation.contains("createdAt="));
        Assertions.assertTrue(representation.contains("cognitiveQuery="));
        Assertions.assertTrue(representation.contains("cognitiveResultJson="));
        Assertions.assertTrue(representation.contains(payV1Sql));
    }

    @Test
    public void test_7() {
        SystemMutation mutation = new SystemMutation();
        String cognitiveResultJson =
                "{\"gateResult\":{\"ok\":true,\"why\":\"approved\",\"commitId\":null},"
                        + "\"commitOrder\":[\"mcqe_it_q1_fetch_options\"]}";

        Assertions.assertNull(mutation.getCognitiveResultJson());

        mutation.setCognitiveResultJson(cognitiveResultJson);

        Assertions.assertEquals(cognitiveResultJson, mutation.getCognitiveResultJson());

        String representation = mutation.toString();
        Assertions.assertTrue(representation.contains("cognitiveResultJson="));
        Assertions.assertTrue(representation.contains(cognitiveResultJson));
    }

    @Test
    public void test_8() {
        Perception perception = new Perception();
        Observation observation = new Observation();
        perception.setObservation(observation);

        SystemMutation original = new SystemMutation();
        original.setPerception(perception);
        original.setMutationId("mutation-1");
        original.setCreatedAt("2026-10-16T18:00:00Z");
        original.setCognitiveQuery("select decision from llm");
        original.setCognitiveResultJson(
                "{\"gateResult\":{\"ok\":true,\"why\":\"approved\"},"
                        + "\"queryExecutionJson\":{}}");

        String json = original.toJson();
        SystemMutation restored =
                MotionBaseModel.fromJson(json, SystemMutation.class);

        Assertions.assertNotNull(restored);
        Assertions.assertEquals(original.getMutationId(), restored.getMutationId());
        Assertions.assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        Assertions.assertEquals(original.getCognitiveQuery(), restored.getCognitiveQuery());
        Assertions.assertEquals(
                original.getCognitiveResultJson(),
                restored.getCognitiveResultJson());
        Assertions.assertNotNull(restored.getPerception());
        Assertions.assertNotNull(restored.getPerception().getObservation());
        Assertions.assertTrue(json.contains("mutationId"));
        Assertions.assertTrue(json.contains("cognitiveResultJson"));
        Assertions.assertTrue(json.contains("queryExecutionJson"));
    }

    @Test
    public void test_9() {
        Observation observation = new Observation();
        Perception perception = new Perception();
        perception.setObservation(observation);

        String mutationId = "mutation-event-1";
        String cognitiveResultJson =
                "{\"description\":null,"
                        + "\"commitOrder\":[\"mo_it_q1_fetch_options\"],"
                        + "\"gateResult\":{\"ok\":true,\"why\":\"approved\",\"commitId\":null},"
                        + "\"stepResults\":[{\"id\":\"mo_it_q1_fetch_options\","
                        + "\"queryResult\":{\"ok\":true,"
                        + "\"requestJson\":{},"
                        + "\"queryExecutionJson\":{}}}]}";

        SystemMutation original = new SystemMutation();
        original.setPerception(perception);
        original.setMutationId(mutationId);
        original.setCognitiveResultJson(cognitiveResultJson);

        String mutationEvent = original.toJson();
        JsonObject root = JsonParser.parseString(mutationEvent).getAsJsonObject();

        Assertions.assertEquals(5, root.size());
        Assertions.assertTrue(root.has("perception"));
        Assertions.assertTrue(root.has("mutationId"));
        Assertions.assertTrue(root.has("createdAt"));
        Assertions.assertTrue(root.has("cognitiveQuery"));
        Assertions.assertTrue(root.has("cognitiveResultJson"));
        Assertions.assertFalse(root.has("eventId"));
        Assertions.assertFalse(root.has("eventType"));
        Assertions.assertFalse(root.has("payloadJson"));
        Assertions.assertFalse(root.has("originTime"));
        Assertions.assertFalse(root.has("receivedAt"));
        Assertions.assertFalse(root.has("replaySignal"));

        Assertions.assertEquals(mutationId, root.get("mutationId").getAsString());
        Assertions.assertTrue(root.get("perception").isJsonObject());
        Assertions.assertTrue(
                root.get("perception").getAsJsonObject().has("observation"));
        Assertions.assertTrue(root.get("cognitiveResultJson").isJsonPrimitive());
        Assertions.assertEquals(
                cognitiveResultJson,
                root.get("cognitiveResultJson").getAsString());

        SystemMutation restored =
                MotionBaseModel.fromJson(mutationEvent, SystemMutation.class);

        Assertions.assertEquals(mutationId, restored.getMutationId());
        Assertions.assertEquals(
                cognitiveResultJson,
                restored.getCognitiveResultJson());
        Assertions.assertNotNull(restored.getPerception());
        Assertions.assertNotNull(restored.getPerception().getObservation());
        Assertions.assertNull(restored.getCreatedAt());
        Assertions.assertNull(restored.getCognitiveQuery());
    }
}
