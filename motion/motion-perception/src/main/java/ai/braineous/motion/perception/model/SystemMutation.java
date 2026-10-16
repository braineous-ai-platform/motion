package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class SystemMutation extends MotionBaseModel {

    private Perception perception;
    private String mutationId;
    private String createdAt;
    private String cognitiveQuery;
    private String cognitiveResultJson;

    public SystemMutation() {
    }

    public Perception getPerception() {
        return this.perception;
    }

    public void setPerception(Perception perception) {
        this.perception = perception;
    }

    public String getMutationId() {
        return this.mutationId;
    }

    public void setMutationId(String mutationId) {
        this.mutationId = mutationId;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCognitiveQuery() {
        return this.cognitiveQuery;
    }

    public void setCognitiveQuery(String cognitiveQuery) {
        this.cognitiveQuery = cognitiveQuery;
    }

    public String getCognitiveResultJson() {
        return this.cognitiveResultJson;
    }

    public void setCognitiveResultJson(String cognitiveResultJson) {
        this.cognitiveResultJson = cognitiveResultJson;
    }

    @Override
    public String toString() {
        return "SystemMutation{" +
                "perception=" + perception +
                ", mutationId='" + mutationId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", cognitiveQuery='" + cognitiveQuery + '\'' +
                ", cognitiveResultJson='" + cognitiveResultJson + '\'' +
                '}';
    }
}
