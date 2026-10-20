package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DevxQueryOrchestratorTest {

    @Test
    public void test_1() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact evaluateRisk =
                artifact("evaluate_risk", "select risk_decision from llm");
        CognitiveQueryArtifact summarizeAnalysis =
                artifact("summarize_analysis", "select summary from llm");
        CognitiveTransactionGroup paymentAnalysis =
                group("payment_analysis", analyzePayment, evaluateRisk);
        CognitiveTransactionGroup paymentSummary =
                group("payment_summary", summarizeAnalysis);
        List<CognitiveTransactionGroup> analysis =
                section(paymentAnalysis, paymentSummary);

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(analysis);

        IsolatedAnalysisGroupStore analysisGroups = new IsolatedAnalysisGroupStore();
        IsolatedAnalysisQueryStore analysisQueries = new IsolatedAnalysisQueryStore();
        IsolatedMutationGroupStore mutationGroups = new IsolatedMutationGroupStore();
        IsolatedMutationQueryStore mutationQueries = new IsolatedMutationQueryStore();

        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(definition);

        Assertions.assertEquals(2, analysisGroups.putCount);
        Assertions.assertSame(paymentAnalysis, analysisGroups.groups.get(0));
        Assertions.assertSame(paymentSummary, analysisGroups.groups.get(1));
        Assertions.assertEquals(3, analysisQueries.putCount);
        Assertions.assertEquals("analyze_payment", analysisQueries.names.get(0));
        Assertions.assertEquals("select analysis_decision from llm", analysisQueries.sqls.get(0));
        Assertions.assertEquals("evaluate_risk", analysisQueries.names.get(1));
        Assertions.assertEquals("select risk_decision from llm", analysisQueries.sqls.get(1));
        Assertions.assertEquals("summarize_analysis", analysisQueries.names.get(2));
        Assertions.assertEquals("select summary from llm", analysisQueries.sqls.get(2));
        Assertions.assertEquals(0, mutationGroups.putCount);
        Assertions.assertEquals(0, mutationQueries.putCount);
        Assertions.assertSame(analysis, definition.getAnalysis());
        Assertions.assertNull(definition.getMutation());
        Assertions.assertEquals("analyze_payment", analyzePayment.getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analyzePayment.getCognitiveQuery());
    }

    @Test
    public void test_2() {
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        CognitiveQueryArtifact confirmAction =
                artifact("confirm_action", "select confirmation from llm");
        CognitiveQueryArtifact recordAction =
                artifact("record_action", "select record from llm");
        CognitiveTransactionGroup paymentMutation =
                group("payment_mutation", selectAction);
        CognitiveTransactionGroup paymentConfirmation =
                group("payment_confirmation", confirmAction, recordAction);
        List<CognitiveTransactionGroup> mutation =
                section(paymentMutation, paymentConfirmation);

        DevxDefinition definition = new DevxDefinition();
        definition.setMutation(mutation);

        IsolatedAnalysisGroupStore analysisGroups = new IsolatedAnalysisGroupStore();
        IsolatedAnalysisQueryStore analysisQueries = new IsolatedAnalysisQueryStore();
        IsolatedMutationGroupStore mutationGroups = new IsolatedMutationGroupStore();
        IsolatedMutationQueryStore mutationQueries = new IsolatedMutationQueryStore();

        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(definition);

        Assertions.assertEquals(2, mutationGroups.putCount);
        Assertions.assertSame(paymentMutation, mutationGroups.groups.get(0));
        Assertions.assertSame(paymentConfirmation, mutationGroups.groups.get(1));
        Assertions.assertEquals(3, mutationQueries.putCount);
        Assertions.assertEquals("select_action", mutationQueries.names.get(0));
        Assertions.assertEquals("select mutation_decision from llm", mutationQueries.sqls.get(0));
        Assertions.assertEquals("confirm_action", mutationQueries.names.get(1));
        Assertions.assertEquals("select confirmation from llm", mutationQueries.sqls.get(1));
        Assertions.assertEquals("record_action", mutationQueries.names.get(2));
        Assertions.assertEquals("select record from llm", mutationQueries.sqls.get(2));
        Assertions.assertEquals(0, analysisGroups.putCount);
        Assertions.assertEquals(0, analysisQueries.putCount);
        Assertions.assertNull(definition.getAnalysis());
        Assertions.assertSame(mutation, definition.getMutation());
        Assertions.assertEquals("select_action", selectAction.getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                selectAction.getCognitiveQuery());
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
        CognitiveTransactionGroup paymentAnalysis =
                group("payment_analysis", analyzePayment, evaluateRisk);
        CognitiveTransactionGroup paymentMutation =
                group("payment_mutation", selectAction, confirmAction);

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(section(paymentAnalysis));
        definition.setMutation(section(paymentMutation));

        IsolatedAnalysisGroupStore analysisGroups = new IsolatedAnalysisGroupStore();
        IsolatedAnalysisQueryStore analysisQueries = new IsolatedAnalysisQueryStore();
        IsolatedMutationGroupStore mutationGroups = new IsolatedMutationGroupStore();
        IsolatedMutationQueryStore mutationQueries = new IsolatedMutationQueryStore();

        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(definition);

        Assertions.assertEquals(1, analysisGroups.putCount);
        Assertions.assertEquals(2, analysisQueries.putCount);
        Assertions.assertEquals(1, mutationGroups.putCount);
        Assertions.assertEquals(2, mutationQueries.putCount);
        Assertions.assertSame(paymentAnalysis, analysisGroups.groups.get(0));
        Assertions.assertSame(paymentMutation, mutationGroups.groups.get(0));
        Assertions.assertEquals("analyze_payment", analysisQueries.names.get(0));
        Assertions.assertEquals("evaluate_risk", analysisQueries.names.get(1));
        Assertions.assertEquals("select_action", mutationQueries.names.get(0));
        Assertions.assertEquals("confirm_action", mutationQueries.names.get(1));
        assertAbsent(analysisQueries.names, "select_action");
        assertAbsent(analysisQueries.names, "confirm_action");
        assertAbsent(mutationQueries.names, "analyze_payment");
        assertAbsent(mutationQueries.names, "evaluate_risk");
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analysisQueries.sqls.get(0));
        Assertions.assertEquals(
                "select mutation_decision from llm",
                mutationQueries.sqls.get(0));
    }

    @Test
    public void test_4() {
        CognitiveTransactionGroup paymentMutation =
                group("payment_mutation", artifact("select_action", "select mutation_decision from llm"));
        DevxDefinition mutationOnly = new DevxDefinition();
        mutationOnly.setMutation(section(paymentMutation));

        IsolatedAnalysisGroupStore analysisGroups = new IsolatedAnalysisGroupStore();
        IsolatedAnalysisQueryStore analysisQueries = new IsolatedAnalysisQueryStore();
        IsolatedMutationGroupStore mutationGroups = new IsolatedMutationGroupStore();
        IsolatedMutationQueryStore mutationQueries = new IsolatedMutationQueryStore();
        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(mutationOnly);

        Assertions.assertEquals(0, analysisGroups.putCount);
        Assertions.assertEquals(0, analysisQueries.putCount);
        Assertions.assertEquals(1, mutationGroups.putCount);
        Assertions.assertEquals(1, mutationQueries.putCount);

        CognitiveTransactionGroup paymentAnalysis =
                group("payment_analysis", artifact("analyze_payment", "select analysis_decision from llm"));
        DevxDefinition analysisOnly = new DevxDefinition();
        analysisOnly.setAnalysis(section(paymentAnalysis));

        analysisGroups = new IsolatedAnalysisGroupStore();
        analysisQueries = new IsolatedAnalysisQueryStore();
        mutationGroups = new IsolatedMutationGroupStore();
        mutationQueries = new IsolatedMutationQueryStore();
        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(analysisOnly);

        Assertions.assertEquals(1, analysisGroups.putCount);
        Assertions.assertEquals(1, analysisQueries.putCount);
        Assertions.assertEquals(0, mutationGroups.putCount);
        Assertions.assertEquals(0, mutationQueries.putCount);

        DevxDefinition neither = new DevxDefinition();
        analysisGroups = new IsolatedAnalysisGroupStore();
        analysisQueries = new IsolatedAnalysisQueryStore();
        mutationGroups = new IsolatedMutationGroupStore();
        mutationQueries = new IsolatedMutationQueryStore();
        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(neither);

        Assertions.assertEquals(0, analysisGroups.putCount);
        Assertions.assertEquals(0, analysisQueries.putCount);
        Assertions.assertEquals(0, mutationGroups.putCount);
        Assertions.assertEquals(0, mutationQueries.putCount);
    }

    @Test
    public void test_5() {
        CognitiveQueryArtifact analyzePayment =
                artifact("analyze_payment", "select analysis_decision from llm");
        CognitiveQueryArtifact selectAction =
                artifact("select_action", "select mutation_decision from llm");
        List<CognitiveQueryArtifact> analysisQueryList = queries(analyzePayment);
        List<CognitiveQueryArtifact> mutationQueryList = queries(selectAction);
        CognitiveTransactionGroup paymentAnalysis = new CognitiveTransactionGroup();
        paymentAnalysis.setName("payment_analysis");
        paymentAnalysis.setQueries(analysisQueryList);
        CognitiveTransactionGroup paymentMutation = new CognitiveTransactionGroup();
        paymentMutation.setName("payment_mutation");
        paymentMutation.setQueries(mutationQueryList);
        List<CognitiveTransactionGroup> analysis = section(paymentAnalysis);
        List<CognitiveTransactionGroup> mutation = section(paymentMutation);

        DevxDefinition definition = new DevxDefinition();
        definition.setAnalysis(analysis);
        definition.setMutation(mutation);

        IsolatedAnalysisGroupStore analysisGroups = new IsolatedAnalysisGroupStore();
        IsolatedAnalysisQueryStore analysisQueries = new IsolatedAnalysisQueryStore();
        IsolatedMutationGroupStore mutationGroups = new IsolatedMutationGroupStore();
        IsolatedMutationQueryStore mutationQueries = new IsolatedMutationQueryStore();

        orchestrator(
                analysisGroups,
                analysisQueries,
                mutationGroups,
                mutationQueries).publish(definition);

        Assertions.assertSame(paymentAnalysis, analysisGroups.groups.get(0));
        Assertions.assertSame(paymentMutation, mutationGroups.groups.get(0));
        Assertions.assertEquals("analyze_payment", analysisQueries.names.get(0));
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analysisQueries.sqls.get(0));
        Assertions.assertEquals("select_action", mutationQueries.names.get(0));
        Assertions.assertEquals(
                "select mutation_decision from llm",
                mutationQueries.sqls.get(0));
        Assertions.assertSame(analysis, definition.getAnalysis());
        Assertions.assertSame(mutation, definition.getMutation());
        Assertions.assertSame(paymentAnalysis, definition.getAnalysis().get(0));
        Assertions.assertSame(paymentMutation, definition.getMutation().get(0));
        Assertions.assertSame(analysisQueryList, paymentAnalysis.getQueries());
        Assertions.assertSame(mutationQueryList, paymentMutation.getQueries());
        Assertions.assertSame(analyzePayment, paymentAnalysis.getQueries().get(0));
        Assertions.assertSame(selectAction, paymentMutation.getQueries().get(0));
        Assertions.assertEquals("payment_analysis", paymentAnalysis.getName());
        Assertions.assertEquals("payment_mutation", paymentMutation.getName());
        Assertions.assertEquals("analyze_payment", analyzePayment.getName());
        Assertions.assertEquals(
                "select analysis_decision from llm",
                analyzePayment.getCognitiveQuery());
        Assertions.assertEquals("select_action", selectAction.getName());
        Assertions.assertEquals(
                "select mutation_decision from llm",
                selectAction.getCognitiveQuery());
    }

    private DevxQueryOrchestrator orchestrator(
            IsolatedAnalysisGroupStore analysisGroups,
            IsolatedAnalysisQueryStore analysisQueries,
            IsolatedMutationGroupStore mutationGroups,
            IsolatedMutationQueryStore mutationQueries) {
        DevxQueryOrchestrator orchestrator = new DevxQueryOrchestrator();
        orchestrator.analysisGroupStore = analysisGroups;
        orchestrator.analysisQueryStore = analysisQueries;
        orchestrator.mutationGroupStore = mutationGroups;
        orchestrator.mutationQueryStore = mutationQueries;
        return orchestrator;
    }

    private void assertAbsent(List<String> names, String excluded) {
        int index = 0;
        while (index < names.size()) {
            Assertions.assertNotEquals(excluded, names.get(index));
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

    private List<CognitiveQueryArtifact> queries(CognitiveQueryArtifact... artifacts) {
        List<CognitiveQueryArtifact> ordered = new ArrayList<CognitiveQueryArtifact>();
        int index = 0;
        while (index < artifacts.length) {
            ordered.add(artifacts[index]);
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

    static class IsolatedAnalysisGroupStore extends AnalysisGroupStore {

        int putCount;
        List<CognitiveTransactionGroup> groups =
                new ArrayList<CognitiveTransactionGroup>();

        @Override
        public void put(CognitiveTransactionGroup group) {
            this.putCount = this.putCount + 1;
            this.groups.add(group);
        }
    }

    static class IsolatedAnalysisQueryStore extends AnalysisQueryStore {

        int putCount;
        List<String> names = new ArrayList<String>();
        List<String> sqls = new ArrayList<String>();

        @Override
        public void put(String name, String sql) {
            this.putCount = this.putCount + 1;
            this.names.add(name);
            this.sqls.add(sql);
        }
    }

    static class IsolatedMutationGroupStore extends MutationGroupStore {

        int putCount;
        List<CognitiveTransactionGroup> groups =
                new ArrayList<CognitiveTransactionGroup>();

        @Override
        public void put(CognitiveTransactionGroup group) {
            this.putCount = this.putCount + 1;
            this.groups.add(group);
        }
    }

    static class IsolatedMutationQueryStore extends MutationQueryStore {

        int putCount;
        List<String> names = new ArrayList<String>();
        List<String> sqls = new ArrayList<String>();

        @Override
        public void put(String name, String sql) {
            this.putCount = this.putCount + 1;
            this.names.add(name);
            this.sqls.add(sql);
        }
    }
}
