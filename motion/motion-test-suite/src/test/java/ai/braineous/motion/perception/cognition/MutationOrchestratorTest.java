package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemMutation;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MutationOrchestratorTest {

    @Test
    public void test_1() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        orchestrator.analyze(perception, queryNames);

        Assertions.assertSame(perception, analyst.perception);
        Assertions.assertSame(queryNames, analyst.queryNames);
    }

    @Test
    public void test_2() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        orchestrator.analyze(perception, queryNames);

        Assertions.assertEquals(1, analyst.invocationCount);
    }

    @Test
    public void test_3() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        analyst.txResult = new TxExecutionResult();

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemMutation mutation = orchestrator.analyze(perception, queryNames);

        Assertions.assertNotNull(mutation);
        Assertions.assertSame(perception, mutation.getPerception());
    }

    @Test
    public void test_4() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        TxExecutionResult txResult = new TxExecutionResult();
        analyst.txResult = txResult;

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemMutation mutation = orchestrator.analyze(perception, queryNames);

        Assertions.assertNotNull(mutation.getMutationId());
        Assertions.assertFalse(mutation.getMutationId().trim().isEmpty());
    }

    @Test
    public void test_5() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        TxExecutionResult txResult = new TxExecutionResult();
        txResult.setDescription("DD-LLM-QUERY1");
        analyst.txResult = txResult;

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemMutation mutation = orchestrator.analyze(perception, queryNames);

        Assertions.assertEquals(
                txResult.toJson(),
                mutation.getCognitiveResultJson());
        Assertions.assertTrue(
                mutation.getCognitiveResultJson().contains("DD-LLM-QUERY1"));
    }

    @Test
    public void test_6() {
        IsolatedMutationCognitiveQueryExecutor analyst =
                new IsolatedMutationCognitiveQueryExecutor();
        analyst.txResult = null;

        MutationOrchestrator orchestrator = new MutationOrchestrator();
        orchestrator.mutationCognitiveQueryExecutor = analyst;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        SystemMutation mutation = orchestrator.analyze(perception, queryNames);

        Assertions.assertNotNull(mutation);
        Assertions.assertSame(perception, mutation.getPerception());
        Assertions.assertNotNull(mutation.getMutationId());
        Assertions.assertFalse(mutation.getMutationId().trim().isEmpty());
        Assertions.assertNull(mutation.getCognitiveResultJson());
    }

    static class IsolatedMutationCognitiveQueryExecutor
            extends MutationCognitiveQueryExecutor {

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
}
