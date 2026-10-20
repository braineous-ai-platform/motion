package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

import java.util.List;

public class CognitiveTransactionGroup extends MotionBaseModel {

    private String name;
    private List<CognitiveQueryArtifact> queries;

    public CognitiveTransactionGroup() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CognitiveQueryArtifact> getQueries() {
        return this.queries;
    }

    public void setQueries(List<CognitiveQueryArtifact> queries) {
        this.queries = queries;
    }

    @Override
    public String toString() {
        return "CognitiveTransactionGroup{" +
                "name='" + name + '\'' +
                ", queries=" + queries +
                '}';
    }
}
