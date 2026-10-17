package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemMutation;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MutationOrchestrator {

    @Inject
    MutationCognitiveQueryExecutor mutationCognitiveQueryExecutor;

    public MutationOrchestrator() {
    }

    public SystemMutation analyze(
            Perception perception,
            List<String> queryNames) {

        TxExecutionResult txResult =
                this.mutationCognitiveQueryExecutor.analyze(perception, queryNames);

        SystemMutation mutation = new SystemMutation();
        mutation.setPerception(perception);
        mutation.setMutationId(UUID.randomUUID().toString());

        if (txResult != null) {
            mutation.setCognitiveResultJson(txResult.toJson());
        }

        return mutation;
    }
}
