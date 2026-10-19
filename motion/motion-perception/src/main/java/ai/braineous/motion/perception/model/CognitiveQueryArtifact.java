package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class CognitiveQueryArtifact extends MotionBaseModel {

    private String name;
    private String cognitiveQuery;

    public CognitiveQueryArtifact() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCognitiveQuery() {
        return this.cognitiveQuery;
    }

    public void setCognitiveQuery(String cognitiveQuery) {
        this.cognitiveQuery = cognitiveQuery;
    }

    @Override
    public String toString() {
        return "CognitiveQueryArtifact{" +
                "name='" + name + '\'' +
                ", cognitiveQuery='" + cognitiveQuery + '\'' +
                '}';
    }
}
