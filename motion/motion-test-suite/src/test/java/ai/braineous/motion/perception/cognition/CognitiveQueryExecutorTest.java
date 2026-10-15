package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import io.braineous.dd.llm.orchestra.def.model.ExecutionResult;
import io.braineous.dd.llm.orchestra.def.model.Query;
import io.braineous.dd.llm.orchestra.def.model.Transaction;
import io.braineous.dd.llm.orchestra.def.model.Workflow;
import io.braineous.dd.llm.orchestra.def.service.ExecutorSPI;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CognitiveQueryExecutorTest {

    @Test
    public void test_1() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);
        queryStore.register("risk_v1", RISK_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        executor.txResult = new TxExecutionResult();

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");
        queryNames.add("risk_v1");

        analyst.analyze(perception, queryNames);

        List<Query> queries = executor.workflow.getTransaction().getQueries();
        Assertions.assertEquals(2, queries.size());
        Assertions.assertEquals("pay_v1", queries.get(0).getId());
        Assertions.assertEquals(PAY_V1_SQL, queries.get(0).getSql());
        Assertions.assertEquals("risk_v1", queries.get(1).getId());
        Assertions.assertEquals(RISK_V1_SQL, queries.get(1).getSql());
    }

    @Test
    public void test_2() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);
        queryStore.register("risk_v1", RISK_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        executor.txResult = new TxExecutionResult();

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("risk_v1");
        queryNames.add("pay_v1");

        analyst.analyze(perception, queryNames);

        List<String> commitOrder = executor.workflow.getTransaction().getCommitOrder();
        Assertions.assertEquals(2, commitOrder.size());
        Assertions.assertEquals("risk_v1", commitOrder.get(0));
        Assertions.assertEquals("pay_v1", commitOrder.get(1));
    }

    @Test
    public void test_3() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        executor.txResult = new TxExecutionResult();

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        analyst.analyze(perception, queryNames);

        Assertions.assertNotNull(executor.workflow);
        Transaction transaction = executor.workflow.getTransaction();
        Assertions.assertNotNull(transaction);
        Assertions.assertSame(transaction, executor.workflow.getTransaction());
        Assertions.assertEquals(1, transaction.getQueries().size());
        Assertions.assertSame(
                transaction.getQueries(),
                executor.workflow.getTransaction().getQueries());
        Assertions.assertSame(
                transaction.getCommitOrder(),
                executor.workflow.getTransaction().getCommitOrder());
    }

    @Test
    public void test_4() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        executor.txResult = new TxExecutionResult();

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        analyst.analyze(perception, queryNames);

        Assertions.assertEquals(1, executor.invocationCount);
    }

    @Test
    public void test_5() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        TxExecutionResult expected = new TxExecutionResult();
        executor.txResult = expected;

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        TxExecutionResult actual = analyst.analyze(perception, queryNames);

        Assertions.assertSame(expected, actual);
    }

    @Test
    public void test_6() {
        IsolatedAnalysisQueryStore queryStore = new IsolatedAnalysisQueryStore();
        queryStore.register("pay_v1", PAY_V1_SQL);

        IsolatedExecutor executor = new IsolatedExecutor();
        executor.txResult = new TxExecutionResult();

        CognitiveQueryExecutor analyst = new CognitiveQueryExecutor();
        analyst.analysisQueryStore = queryStore;
        analyst.executorSPI = executor;

        Perception perception = new Perception();
        List<String> queryNames = new ArrayList<String>();
        queryNames.add("pay_v1");

        analyst.analyze(perception, queryNames);

        Assertions.assertNotNull(executor.input);
        Assertions.assertSame(perception, executor.input.get("perception"));
    }

    private static final String PAY_V1_SQL =
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

    private static final String RISK_V1_SQL =
            "select decision, reason, code "
                    + "from llm "
                    + "where factId = 'RiskProfile:RISK-4001' "
                    + "control intent = 'assess_risk', "
                    + "action = 'determine', "
                    + "subject = 'risk_profile', "
                    + "decision = 'allow_capture', "
                    + "basis = 'related_system_facts', "
                    + "goal = 'decision'";

    static class IsolatedAnalysisQueryStore extends AnalysisQueryStore {

        private Map<String, String> sqlByName = new HashMap<String, String>();

        void register(String name, String sql) {
            this.sqlByName.put(name, sql);
        }

        @Override
        public String get(String name) {
            return this.sqlByName.get(name);
        }
    }

    static class IsolatedExecutor implements ExecutorSPI {

        int invocationCount;
        Workflow workflow;
        Map<String, Object> input;
        TxExecutionResult txResult;

        @Override
        public ExecutionResult executeTransaction(
                Workflow workflow,
                Map<String, Object> input) {
            return null;
        }

        @Override
        public TxExecutionResult executeCognitiveTransaction(
                Workflow workflow,
                Map<String, Object> input) {
            this.invocationCount = this.invocationCount + 1;
            this.workflow = workflow;
            this.input = input;
            return this.txResult;
        }

        @Override
        public String getWorkflowStatus(String executionId) {
            return null;
        }
    }
}
