package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.Perception;
import ai.braineous.motion.perception.model.SystemAnalysis;
import io.braineous.dd.llm.transaction.model.TxExecutionResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AnalysisOrchestrator {

    @Inject
    CognitiveQueryExecutor cognitiveQueryExecutor;

    @Inject
    AnalysisStore analysisStore;

    public AnalysisOrchestrator() {
    }

    public SystemAnalysis analyze(
            Perception perception,
            List<String> queryNames) {

        TxExecutionResult txResult =
                this.cognitiveQueryExecutor.analyze(perception, queryNames);

        SystemAnalysis analysis = new SystemAnalysis();
        analysis.setPerception(perception);
        analysis.setAnalysisId(UUID.randomUUID().toString());

        if (txResult != null) {
            analysis.setCognitiveResultJson(txResult.toJson());
        }

        this.analysisStore.put(analysis);
        return analysis;
    }
}
