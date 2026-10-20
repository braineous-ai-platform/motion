package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class DevxQueryOrchestrator {

    @Inject
    AnalysisGroupStore analysisGroupStore;

    @Inject
    AnalysisQueryStore analysisQueryStore;

    @Inject
    MutationGroupStore mutationGroupStore;

    @Inject
    MutationQueryStore mutationQueryStore;

    public DevxQueryOrchestrator() {
    }

    public void publish(DevxDefinition definition) {
        List<CognitiveTransactionGroup> analysisGroups = definition.getAnalysis();
        if (analysisGroups != null) {
            int groupIndex = 0;
            while (groupIndex < analysisGroups.size()) {
                this.analysisGroupStore.put(analysisGroups.get(groupIndex));
                groupIndex = groupIndex + 1;
            }

            List<CognitiveQueryArtifact> analysisQueries =
                    new DevxArtifactTranslator().translateAnalysis(definition);
            int queryIndex = 0;
            while (queryIndex < analysisQueries.size()) {
                CognitiveQueryArtifact artifact = analysisQueries.get(queryIndex);
                this.analysisQueryStore.put(
                        artifact.getName(),
                        artifact.getCognitiveQuery());
                queryIndex = queryIndex + 1;
            }
        }

        List<CognitiveTransactionGroup> mutationGroups = definition.getMutation();
        if (mutationGroups != null) {
            int groupIndex = 0;
            while (groupIndex < mutationGroups.size()) {
                this.mutationGroupStore.put(mutationGroups.get(groupIndex));
                groupIndex = groupIndex + 1;
            }

            List<CognitiveQueryArtifact> mutationQueries =
                    new DevxArtifactTranslator().translateMutation(definition);
            int queryIndex = 0;
            while (queryIndex < mutationQueries.size()) {
                CognitiveQueryArtifact artifact = mutationQueries.get(queryIndex);
                this.mutationQueryStore.put(
                        artifact.getName(),
                        artifact.getCognitiveQuery());
                queryIndex = queryIndex + 1;
            }
        }
    }
}
