package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DevxArtifactTranslatorTest {

    @Test
    public void test_1() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact summarizeAnalysis =
                artifact("summarize_analysis", "select summary from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(
                group("payment_analysis", analyzePayment, evaluateRisk),
                group("payment_summary", summarizeAnalysis)));

        List<CognitiveQueryArtifact> flattened =
                new DevxArtifactTranslator().translateAnalysis(definition);

        Assertions.assertEquals(3, flattened.size());
        Assertions.assertSame(analyzePayment, flattened.get(0));
        Assertions.assertSame(evaluateRisk, flattened.get(1));
        Assertions.assertSame(summarizeAnalysis, flattened.get(2));
        Assertions.assertEquals("analyze_payment", flattened.get(0).getName());
        Assertions.assertEquals("evaluate_risk", flattened.get(1).getName());
        Assertions.assertEquals("summarize_analysis", flattened.get(2).getName());
    }

    @Test
    public void test_2() {
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        CognitiveQueryArtifact confirmAction =
                artifact("confirm_action", "select confirmation from llm");
        CognitiveQueryArtifact recordAction =
                artifact("record_action", "select record from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setMutation(section(
                group("payment_mutation", selectAction),
                group("payment_confirmation", confirmAction, recordAction)));

        List<CognitiveQueryArtifact> flattened =
                new DevxArtifactTranslator().translateMutation(definition);

        Assertions.assertEquals(3, flattened.size());
        Assertions.assertSame(selectAction, flattened.get(0));
        Assertions.assertSame(confirmAction, flattened.get(1));
        Assertions.assertSame(recordAction, flattened.get(2));
        Assertions.assertEquals("select_action", flattened.get(0).getName());
        Assertions.assertEquals("confirm_action", flattened.get(1).getName());
        Assertions.assertEquals("record_action", flattened.get(2).getName());
    }

    @Test
    public void test_3() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        CognitiveQueryArtifact confirmAction =
                artifact("confirm_action", "select confirmation from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(
                group("payment_analysis", analyzePayment, evaluateRisk)));
        definition.setMutation(section(
                group("payment_mutation", selectAction, confirmAction)));

        List<CognitiveQueryArtifact> flattened =
                new DevxArtifactTranslator().translateAnalysis(definition);

        Assertions.assertEquals(2, flattened.size());
        Assertions.assertSame(analyzePayment, flattened.get(0));
        Assertions.assertSame(evaluateRisk, flattened.get(1));
        assertAbsent(flattened, selectAction);
        assertAbsent(flattened, confirmAction);
    }

    @Test
    public void test_4() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        CognitiveQueryArtifact confirmAction =
                artifact("confirm_action", "select confirmation from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(
                group("payment_analysis", analyzePayment, evaluateRisk)));
        definition.setMutation(section(
                group("payment_mutation", selectAction, confirmAction)));

        List<CognitiveQueryArtifact> flattened =
                new DevxArtifactTranslator().translateMutation(definition);

        Assertions.assertEquals(2, flattened.size());
        Assertions.assertSame(selectAction, flattened.get(0));
        Assertions.assertSame(confirmAction, flattened.get(1));
        assertAbsent(flattened, analyzePayment);
        assertAbsent(flattened, evaluateRisk);
    }

    @Test
    public void test_5() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(group("payment_analysis", analyzePayment)));
        definition.setMutation(section(group("payment_mutation", selectAction)));

        DevxArtifactTranslator translator = new DevxArtifactTranslator();

        Assertions.assertSame(
                analyzePayment,
                translator.translateAnalysis(definition).get(0));
        Assertions.assertSame(
                selectAction,
                translator.translateMutation(definition).get(0));
    }

    @Test
    public void test_6() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");

        List<CognitiveQueryArtifact> analysisQueries =
                queries(analyzePayment, evaluateRisk);
        List<CognitiveQueryArtifact> mutationQueries = queries(selectAction);
        CognitiveTransactionGroup analysisGroup = new CognitiveTransactionGroup();
        analysisGroup.setName("payment_analysis");
        analysisGroup.setQueries(analysisQueries);
        CognitiveTransactionGroup mutationGroup = new CognitiveTransactionGroup();
        mutationGroup.setName("payment_mutation");
        mutationGroup.setQueries(mutationQueries);
        List<CognitiveTransactionGroup> analysis = section(analysisGroup);
        List<CognitiveTransactionGroup> mutation = section(mutationGroup);

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(analysis);
        definition.setMutation(mutation);

        DevxArtifactTranslator translator = new DevxArtifactTranslator();
        translator.translateAnalysis(definition);
        translator.translateMutation(definition);

        Assertions.assertSame(analysis, definition.getAnalysis());
        Assertions.assertSame(mutation, definition.getMutation());
        Assertions.assertSame(analysisGroup, definition.getAnalysis().get(0));
        Assertions.assertSame(mutationGroup, definition.getMutation().get(0));
        Assertions.assertSame(analysisQueries, analysisGroup.getQueries());
        Assertions.assertSame(mutationQueries, mutationGroup.getQueries());
        Assertions.assertEquals(2, analysisQueries.size());
        Assertions.assertEquals(1, mutationQueries.size());
        Assertions.assertSame(analyzePayment, analysisQueries.get(0));
        Assertions.assertSame(evaluateRisk, analysisQueries.get(1));
        Assertions.assertSame(selectAction, mutationQueries.get(0));
        Assertions.assertEquals("payment_analysis", analysisGroup.getName());
        Assertions.assertEquals("payment_mutation", mutationGroup.getName());
        Assertions.assertEquals("analyze_payment", analyzePayment.getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analyzePayment.getCognitiveQuery());
        Assertions.assertEquals("evaluate_risk", evaluateRisk.getName());
        Assertions.assertEquals(
                "select risk_decision from llm",
                evaluateRisk.getCognitiveQuery());
        Assertions.assertEquals("select_action", selectAction.getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                selectAction.getCognitiveQuery());
    }

    @Test
    public void test_7() {
        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(new ArrayList<CognitiveTransactionGroup>());
        definition.setMutation(new ArrayList<CognitiveTransactionGroup>());

        DevxArtifactTranslator translator = new DevxArtifactTranslator();
        List<CognitiveQueryArtifact> analysis =
                translator.translateAnalysis(definition);
        List<CognitiveQueryArtifact> mutation =
                translator.translateMutation(definition);

        Assertions.assertNotNull(analysis);
        Assertions.assertNotNull(mutation);
        Assertions.assertEquals(0, analysis.size());
        Assertions.assertEquals(0, mutation.size());
    }

    @Test
    public void test_8() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact summarizeAnalysis =
                artifact("summarize_analysis", "select summary from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        CognitiveQueryArtifact confirmAction =
                artifact("confirm_action", "select confirmation from llm");

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(
                group("payment_analysis", analyzePayment, evaluateRisk),
                group("empty_analysis"),
                group("payment_summary", summarizeAnalysis)));
        definition.setMutation(section(
                group("payment_mutation", selectAction),
                group("empty_mutation"),
                group("payment_confirmation", confirmAction)));

        DevxArtifactTranslator translator = new DevxArtifactTranslator();
        List<CognitiveQueryArtifact> analysis =
                translator.translateAnalysis(definition);
        List<CognitiveQueryArtifact> mutation =
                translator.translateMutation(definition);

        Assertions.assertEquals(3, analysis.size());
        Assertions.assertSame(analyzePayment, analysis.get(0));
        Assertions.assertSame(evaluateRisk, analysis.get(1));
        Assertions.assertSame(summarizeAnalysis, analysis.get(2));
        Assertions.assertEquals(2, mutation.size());
        Assertions.assertSame(selectAction, mutation.get(0));
        Assertions.assertSame(confirmAction, mutation.get(1));
    }

    @Test
    public void test_9() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");

        List<CognitiveQueryArtifact> analysisQueries = queries(analyzePayment);
        List<CognitiveQueryArtifact> mutationQueries = queries(selectAction);
        CognitiveTransactionGroup analysisGroup = new CognitiveTransactionGroup();
        analysisGroup.setName("payment_analysis");
        analysisGroup.setQueries(analysisQueries);
        CognitiveTransactionGroup mutationGroup = new CognitiveTransactionGroup();
        mutationGroup.setName("payment_mutation");
        mutationGroup.setQueries(mutationQueries);
        List<CognitiveTransactionGroup> analysis = section(analysisGroup);
        List<CognitiveTransactionGroup> mutation = section(mutationGroup);

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(analysis);
        definition.setMutation(mutation);

        DevxArtifactTranslator translator = new DevxArtifactTranslator();
        List<CognitiveQueryArtifact> firstAnalysis =
                translator.translateAnalysis(definition);
        List<CognitiveQueryArtifact> secondAnalysis =
                translator.translateAnalysis(definition);
        List<CognitiveQueryArtifact> firstMutation =
                translator.translateMutation(definition);

        Assertions.assertNotSame(analysis, firstAnalysis);
        Assertions.assertNotSame(analysisQueries, firstAnalysis);
        Assertions.assertNotSame(mutation, firstMutation);
        Assertions.assertNotSame(mutationQueries, firstMutation);
        Assertions.assertNotSame(firstAnalysis, secondAnalysis);
        Assertions.assertNotSame(firstAnalysis, firstMutation);
    }

    private void assertAbsent(
            List<CognitiveQueryArtifact> flattened,
            CognitiveQueryArtifact excluded) {
        int index = 0;
        while (index < flattened.size()) {
            Assertions.assertNotSame(excluded, flattened.get(index));
            index = index + 1;
        }
    }

    private CognitiveQueryArtifact artifact(String name, String cognitiveQuery) {
        CognitiveQueryArtifact artifact = new CognitiveQueryArtifact();
        artifact.setName(name);
        artifact.setCognitiveQuery(cognitiveQuery);
        return artifact;
    }

    private CognitiveTransactionGroup group(
            String name,
            CognitiveQueryArtifact... queries) {
        CognitiveTransactionGroup group = new CognitiveTransactionGroup();
        group.setName(name);
        group.setQueries(queries(queries));
        return group;
    }

    private List<CognitiveQueryArtifact> queries(CognitiveQueryArtifact... queries) {
        List<CognitiveQueryArtifact> ordered = new ArrayList<CognitiveQueryArtifact>();
        int index = 0;
        while (index < queries.length) {
            ordered.add(queries[index]);
            index = index + 1;
        }
        return ordered;
    }

    private List<CognitiveTransactionGroup> section(CognitiveTransactionGroup... groups) {
        List<CognitiveTransactionGroup> section =
                new ArrayList<CognitiveTransactionGroup>();
        int index = 0;
        while (index < groups.length) {
            section.add(groups[index]);
            index = index + 1;
        }
        return section;
    }
}
