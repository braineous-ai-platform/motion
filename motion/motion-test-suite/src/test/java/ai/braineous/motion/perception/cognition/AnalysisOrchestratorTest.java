package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemAnalysis;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AnalysisOrchestratorTest {

    @Test
    public void test_1() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        orchestrator.analyze(perception, queryNames);

        Assertions.assertSame(perception, analyst.perception);
        Assertions.assertSame(queryNames, analyst.queryNames);
    }

    @Test
    public void test_2() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        orchestrator.analyze(perception, queryNames);

        Assertions.assertEquals(1, analyst.invocationCount);
    }

    @Test
    public void test_3() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemAnalysis analysis = orchestrator.analyze(perception, queryNames);

        Assertions.assertNotNull(analysis);
        Assertions.assertSame(perception, analysis.getPerception());
    }

    @Test
    public void test_4() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        TxExecutionResult txResult = new TxExecutionResult();
        analyst.txResult = txResult;

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemAnalysis analysis = orchestrator.analyze(perception, queryNames);

        Assertions.assertNotNull(analysis.getAnalysisId());
        Assertions.assertFalse(analysis.getAnalysisId().trim().isEmpty());
    }

    @Test
    public void test_5() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        TxExecutionResult txResult = new TxExecutionResult();
        txResult.setDescription("DD-LLM-QUERY1");
        analyst.txResult = txResult;

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemAnalysis analysis = orchestrator.analyze(perception, queryNames);

        Assertions.assertEquals(
                txResult.toJson(),
                analysis.getCognitiveResultJson());
        Assertions.assertTrue(
                analysis.getCognitiveResultJson().contains("DD-LLM-QUERY1"));
    }

    @Test
    public void test_6() {
        IsolatedCognitiveQueryExecutor analyst = new IsolatedCognitiveQueryExecutor();
        TxExecutionResult txResult = new TxExecutionResult();
        analyst.txResult = txResult;

        IsolatedAnalysisStore store = new IsolatedAnalysisStore();

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator();
        orchestrator.cognitiveQueryExecutor = analyst;
        orchestrator.analysisStore = store;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemAnalysis analysis = orchestrator.analyze(perception, queryNames);

        Assertions.assertEquals(1, store.putCount);
        Assertions.assertSame(analysis, store.last);
        Assertions.assertNotNull(store.last.getAnalysisId());
        Assertions.assertFalse(store.last.getAnalysisId().trim().isEmpty());
        Assertions.assertEquals(analysis.getAnalysisId(), store.last.getAnalysisId());
    }

    static class IsolatedCognitiveQueryExecutor extends CognitiveQueryExecutor {

        int invocationCount;
        Perception perception;
        List<String> queryNames;
        TxExecutionResult txResult;

        @Override
        public TxExecutionResult analyze(
                Perception perception,
                List<String> queryNames) {
            this.invocationCount = this.invocationCount + 1;
            this.perception = perception;
            this.queryNames = queryNames;
            return this.txResult;
        }
    }

    static class IsolatedAnalysisStore extends AnalysisStore {

        int putCount;
        SystemAnalysis last;

        @Override
        public void put(SystemAnalysis analysis) {
            this.putCount = this.putCount + 1;
            this.last = analysis;
        }
    }
}
