package ai.braineous.motion.perception.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.braineous.motion.core.model.MotionBaseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DevxDefinitionTest {

    @Test
    public void test_1() {
        DevxDefinition definition = new DevxDefinition();

        Assertions.assertNull(definition.getAnalysis());
        Assertions.assertNull(definition.getMutation());
    }

    @Test
    public void test_2() {
        DevxDefinition definition = new DevxDefinition();
        List<CognitiveTransactionGroup> analysis = new ArrayList<CognitiveTransactionGroup>();
        analysis.add(group(
                "payment_analysis",
                artifact("analyze_payment", "select decision from llm")));

        definition.setAnalysis(analysis);

        Assertions.assertSame(analysis, definition.getAnalysis());
    }

    @Test
    public void test_3() {
        DevxDefinition definition = new DevxDefinition();
        List<CognitiveTransactionGroup> mutation = new ArrayList<CognitiveTransactionGroup>();
        mutation.add(group(
                "payment_mutation",
                artifact("select_action", "select decision from llm")));

        definition.setMutation(mutation);

        Assertions.assertSame(mutation, definition.getMutation());
    }

    @Test
    public void test_4() {
        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(new ArrayList<CognitiveTransactionGroup>());
        definition.setMutation(new ArrayList<CognitiveTransactionGroup>());

        String representation = definition.toString();
        Assertions.assertTrue(representation.contains("DevxDefinition"));
        Assertions.assertTrue(representation.contains("analysis="));
        Assertions.assertTrue(representation.contains("mutation="));
    }

    @Test
    public void test_5() {
        DevxDefinition original = new DevxDefinition();
        original.setAnalysis(section(group(
                "payment_analysis",
                artifact("analyze_payment", "select analysis_decision from llm"),
                artifact("evaluate_risk", "select risk_decision from llm"))));

        String json = original.toJson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray analysis = root.getAsJsonArray("analysis");
        JsonObject paymentAnalysis = analysis.get(0).getAsJsonObject();
        JsonArray queries = paymentAnalysis.getAsJsonArray("queries");

        Assertions.assertTrue(root.get("mutation").isJsonNull());
        Assertions.assertEquals(1, analysis.size());
        Assertions.assertEquals(2, paymentAnalysis.size());
        Assertions.assertEquals("payment_analysis", paymentAnalysis.get("name").getAsString());
        Assertions.assertTrue(paymentAnalysis.get("queries").isJsonArray());
        Assertions.assertEquals(2, queries.size());
        Assertions.assertEquals("analyze_payment", queries.get(0).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                queries.get(0).getAsJsonObject().get("cognitiveQuery").getAsString());
        Assertions.assertEquals("evaluate_risk", queries.get(1).getAsJsonObject().get("name").getAsString());

        DevxDefinition restored =
                MotionBaseModel.fromJson(json, DevxDefinition.class);

        Assertions.assertNull(restored.getMutation());
        Assertions.assertEquals(1, restored.getAnalysis().size());
        Assertions.assertEquals("payment_analysis", restored.getAnalysis().get(0).getName());
        Assertions.assertEquals(2, restored.getAnalysis().get(0).getQueries().size());
        Assertions.assertEquals(
                "analyze_payment",
                restored.getAnalysis().get(0).getQueries().get(0).getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                restored.getAnalysis().get(0).getQueries().get(0).getCognitiveQuery());
        Assertions.assertEquals(
                "evaluate_risk",
                restored.getAnalysis().get(0).getQueries().get(1).getName());
    }

    @Test
    public void test_6() {
        DevxDefinition original = new DevxDefinition();
        original.setMutation(section(group(
                "payment_mutation",
                artifact("select_action", "select mutation_decision from llm"))));

        String json = original.toJson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray mutation = root.getAsJsonArray("mutation");
        JsonObject paymentMutation = mutation.get(0).getAsJsonObject();
        JsonArray queries = paymentMutation.getAsJsonArray("queries");

        Assertions.assertTrue(root.get("analysis").isJsonNull());
        Assertions.assertEquals(1, mutation.size());
        Assertions.assertEquals(2, paymentMutation.size());
        Assertions.assertEquals("payment_mutation", paymentMutation.get("name").getAsString());
        Assertions.assertTrue(paymentMutation.get("queries").isJsonArray());
        Assertions.assertEquals(1, queries.size());
        Assertions.assertEquals("select_action", queries.get(0).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                queries.get(0).getAsJsonObject().get("cognitiveQuery").getAsString());

        DevxDefinition restored =
                MotionBaseModel.fromJson(json, DevxDefinition.class);

        Assertions.assertNull(restored.getAnalysis());
        Assertions.assertEquals(1, restored.getMutation().size());
        Assertions.assertEquals("payment_mutation", restored.getMutation().get(0).getName());
        Assertions.assertEquals(1, restored.getMutation().get(0).getQueries().size());
        Assertions.assertEquals(
                "select_action",
                restored.getMutation().get(0).getQueries().get(0).getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                restored.getMutation().get(0).getQueries().get(0).getCognitiveQuery());
    }

    @Test
    public void test_7() {
        DevxDefinition original = new DevxDefinition();
        original.setAnalysis(section(group(
                "payment_analysis",
                artifact("analyze_payment", "select analysis_decision from llm"))));
        original.setMutation(section(group(
                "payment_mutation",
                artifact("select_action", "select mutation_decision from llm"))));

        String json = original.toJson();
        DevxDefinition restored =
                MotionBaseModel.fromJson(json, DevxDefinition.class);

        Assertions.assertEquals("payment_analysis", restored.getAnalysis().get(0).getName());
        Assertions.assertEquals(
                "analyze_payment",
                restored.getAnalysis().get(0).getQueries().get(0).getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                restored.getAnalysis().get(0).getQueries().get(0).getCognitiveQuery());
        Assertions.assertEquals("payment_mutation", restored.getMutation().get(0).getName());
        Assertions.assertEquals(
                "select_action",
                restored.getMutation().get(0).getQueries().get(0).getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                restored.getMutation().get(0).getQueries().get(0).getCognitiveQuery());
    }

    @Test
    public void test_8() {
        DevxDefinition original = new DevxDefinition();
        original.setAnalysis(section(
                group(
                        "payment_analysis",
                        artifact("analyze_payment", "select analysis_decision from llm"),
                        artifact("evaluate_risk", "select risk_decision from llm")),
                group(
                        "payment_summary",
                        artifact("summarize_analysis", "select summary from llm"))));
        original.setMutation(section(
                group(
                        "payment_mutation",
                        artifact("select_action", "select mutation_decision from llm")),
                group(
                        "payment_confirmation",
                        artifact("confirm_action", "select confirmation from llm"),
                        artifact("record_action", "select record from llm"))));

        String json = original.toJson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray analysis = root.getAsJsonArray("analysis");
        JsonArray mutation = root.getAsJsonArray("mutation");

        Assertions.assertEquals(2, analysis.size());
        Assertions.assertEquals("payment_analysis", analysis.get(0).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(2, analysis.get(0).getAsJsonObject().getAsJsonArray("queries").size());
        Assertions.assertEquals("payment_summary", analysis.get(1).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(1, analysis.get(1).getAsJsonObject().getAsJsonArray("queries").size());
        Assertions.assertEquals(2, mutation.size());
        Assertions.assertEquals("payment_mutation", mutation.get(0).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(1, mutation.get(0).getAsJsonObject().getAsJsonArray("queries").size());
        Assertions.assertEquals("payment_confirmation", mutation.get(1).getAsJsonObject().get("name").getAsString());
        Assertions.assertEquals(2, mutation.get(1).getAsJsonObject().getAsJsonArray("queries").size());

        DevxDefinition restored =
                MotionBaseModel.fromJson(json, DevxDefinition.class);

        Assertions.assertEquals(2, restored.getAnalysis().size());
        Assertions.assertEquals("payment_analysis", restored.getAnalysis().get(0).getName());
        Assertions.assertEquals(2, restored.getAnalysis().get(0).getQueries().size());
        Assertions.assertEquals("payment_summary", restored.getAnalysis().get(1).getName());
        Assertions.assertEquals(1, restored.getAnalysis().get(1).getQueries().size());
        Assertions.assertEquals(2, restored.getMutation().size());
        Assertions.assertEquals("payment_mutation", restored.getMutation().get(0).getName());
        Assertions.assertEquals(1, restored.getMutation().get(0).getQueries().size());
        Assertions.assertEquals("payment_confirmation", restored.getMutation().get(1).getName());
        Assertions.assertEquals(2, restored.getMutation().get(1).getQueries().size());
    }

    @Test
    public void test_9() {
        DevxDefinition original = new DevxDefinition();
        original.setAnalysis(section(group(
                "payment_analysis",
                artifact("analyze_payment", "select first from llm"),
                artifact("evaluate_risk", "select second from llm"),
                artifact("explain_result", "select third from llm"))));
        original.setMutation(section(group(
                "payment_mutation",
                artifact("select_action", "select fourth from llm"),
                artifact("confirm_action", "select fifth from llm"))));

        DevxDefinition restored =
                MotionBaseModel.fromJson(original.toJson(), DevxDefinition.class);

        List<CognitiveQueryArtifact> analysisQueries = restored.getAnalysis().get(0).getQueries();
        List<CognitiveQueryArtifact> mutationQueries = restored.getMutation().get(0).getQueries();

        Assertions.assertEquals("analyze_payment", analysisQueries.get(0).getName());
        Assertions.assertEquals("evaluate_risk", analysisQueries.get(1).getName());
        Assertions.assertEquals("explain_result", analysisQueries.get(2).getName());
        Assertions.assertEquals("select first from llm", analysisQueries.get(0).getCognitiveQuery());
        Assertions.assertEquals("select second from llm", analysisQueries.get(1).getCognitiveQuery());
        Assertions.assertEquals("select third from llm", analysisQueries.get(2).getCognitiveQuery());
        Assertions.assertEquals("select_action", mutationQueries.get(0).getName());
        Assertions.assertEquals("confirm_action", mutationQueries.get(1).getName());
        Assertions.assertEquals("select fourth from llm", mutationQueries.get(0).getCognitiveQuery());
        Assertions.assertEquals("select fifth from llm", mutationQueries.get(1).getCognitiveQuery());
    }

    @Test
    public void test_10() {
        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(group(
                "payment_analysis",
                artifact("analyze_payment", "select analysis_decision from llm"))));
        definition.setMutation(section(group(
                "payment_mutation",
                artifact("select_action", "select mutation_decision from llm"))));

        JsonObject root = JsonParser.parseString(definition.toJson()).getAsJsonObject();

        Assertions.assertEquals(2, root.size());
        Assertions.assertTrue(root.has("analysis"));
        Assertions.assertTrue(root.has("mutation"));
        Assertions.assertTrue(root.get("analysis").isJsonArray());
        Assertions.assertTrue(root.get("mutation").isJsonArray());
    }

    @Test
    public void test_11() {
        DevxDefinition original = new DevxDefinition();

        String json = original.toJson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Assertions.assertEquals(2, root.size());
        Assertions.assertTrue(root.has("analysis"));
        Assertions.assertTrue(root.has("mutation"));
        Assertions.assertTrue(root.get("analysis").isJsonNull());
        Assertions.assertTrue(root.get("mutation").isJsonNull());

        DevxDefinition restored =
                MotionBaseModel.fromJson(json, DevxDefinition.class);

        Assertions.assertNull(restored.getAnalysis());
        Assertions.assertNull(restored.getMutation());
    }

    @Test
    public void test_12() {
        List<CognitiveTransactionGroup> emptyAnalysis =
                new ArrayList<CognitiveTransactionGroup>();
        CognitiveTransactionGroup emptyGroup = new CognitiveTransactionGroup();
        emptyGroup.setName("empty_mutation");
        emptyGroup.setQueries(new ArrayList<CognitiveQueryArtifact>());
        List<CognitiveTransactionGroup> emptyMutation =
                new ArrayList<CognitiveTransactionGroup>();
        emptyMutation.add(emptyGroup);

        DevxDefinition original = new DevxDefinition();
        original.setAnalysis(emptyAnalysis);
        original.setMutation(emptyMutation);

        DevxDefinition restored =
                MotionBaseModel.fromJson(original.toJson(), DevxDefinition.class);

        Assertions.assertNotNull(restored.getAnalysis());
        Assertions.assertNotNull(restored.getMutation());
        Assertions.assertEquals(0, restored.getAnalysis().size());
        Assertions.assertEquals(1, restored.getMutation().size());
        Assertions.assertNotNull(restored.getMutation().get(0));
        Assertions.assertEquals("empty_mutation", restored.getMutation().get(0).getName());
        Assertions.assertNotNull(restored.getMutation().get(0).getQueries());
        Assertions.assertEquals(0, restored.getMutation().get(0).getQueries().size());
    }

    private CognitiveQueryArtifact artifact(String name, String cognitiveQuery) {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        artifact.setName(name);
        artifact.setCognitiveQuery(cognitiveQuery);
        return artifact;
    }

    private CognitiveTransactionGroup group(String name, CognitiveQueryArtifact... queries) {
        CognitiveTransactionGroup group = new CognitiveTransactionGroup();
        group.setName(name);
        List<CognitiveQueryArtifact> ordered = new ArrayList<CognitiveQueryArtifact>();
        int index = 0;
        while (index < queries.length) {
            ordered.add(queries[index]);
            index = index + 1;
        }
        group.setQueries(ordered);
        return group;
    }

    private List<CognitiveTransactionGroup> section(CognitiveTransactionGroup... groups) {
        List<CognitiveTransactionGroup> section = new ArrayList<CognitiveTransactionGroup>();
        int index = 0;
        while (index < groups.length) {
            section.add(groups[index]);
            index = index + 1;
        }
        return section;
    }
}
