package ai.braineous.motion.perception.cognition;

import ai.braineous.motion.perception.model.CognitiveQueryArtifact;
import ai.braineous.motion.perception.model.CognitiveTransactionGroup;
import ai.braineous.motion.perception.model.DevxDefinition;

import java.util.ArrayList;
import java.util.List;

public class DevxArtifactTranslator {

    public DevxArtifactTranslator() {
    }

    public List<CognitiveQueryArtifact> translateAnalysis(
            DevxDefinition definition) {
        List<CognitiveQueryArtifact> flattened =
                new ArrayList<CognitiveQueryArtifact>();
        List<CognitiveTransactionGroup> groups = definition.getAnalysis();
        int groupIndex = 0;
        while (groupIndex < groups.size()) {
            CognitiveTransactionGroup group = groups.get(groupIndex);
            List<CognitiveQueryArtifact> queries = group.getQueries();
            int queryIndex = 0;
            while (queryIndex < queries.size()) {
                flattened.add(queries.get(queryIndex));
                queryIndex = queryIndex + 1;
            }
            groupIndex = groupIndex + 1;
        }
        return flattened;
    }

    public List<CognitiveQueryArtifact> translateMutation(
            DevxDefinition definition) {
        List<CognitiveQueryArtifact> flattened =
                new ArrayList<CognitiveQueryArtifact>();
        List<CognitiveTransactionGroup> groups = definition.getMutation();
        int groupIndex = 0;
        while (groupIndex < groups.size()) {
            CognitiveTransactionGroup group = groups.get(groupIndex);
            List<CognitiveQueryArtifact> queries = group.getQueries();
            int queryIndex = 0;
            while (queryIndex < queries.size()) {
                flattened.add(queries.get(queryIndex));
                queryIndex = queryIndex + 1;
            }
            groupIndex = groupIndex + 1;
        }
        return flattened;
    }
}
