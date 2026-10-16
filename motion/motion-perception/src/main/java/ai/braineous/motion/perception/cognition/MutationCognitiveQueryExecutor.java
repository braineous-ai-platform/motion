package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import io.braineous.dd.llm.orchestra.def.model.Query;
import io.braineous.dd.llm.orchestra.def.model.Transaction;
import io.braineous.dd.llm.orchestra.def.model.Workflow;
import io.braineous.dd.llm.orchestra.def.service.ExecutorSPI;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MutationCognitiveQueryExecutor {

    @Inject
    MutationQueryStore mutationQueryStore;

    @Inject
    ExecutorSPI executorSPI;

    public MutationCognitiveQueryExecutor() {
    }

    public TxExecutionResult analyze(
            Perception perception,
            List<String> queryNames) {

        List<Query> queries = new ArrayList<Query>();
        List<String> commitOrder = new ArrayList<String>();

        if (queryNames != null) {
            for (int i = 0; i < queryNames.size(); i++) {
                String name = queryNames.get(i);
                String sql = this.mutationQueryStore.get(name);

                Query query = new Query();
                query.setId(name);
                query.setSql(sql);
                queries.add(query);
                commitOrder.add(name);
            }
        }

        Transaction transaction = new Transaction();
        transaction.setQueries(queries);
        transaction.setCommitOrder(commitOrder);

        Workflow workflow = new Workflow();
        workflow.setTransaction(transaction);

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("perception", perception);

        return this.executorSPI.executeCognitiveTransaction(workflow, input);
    }
}
