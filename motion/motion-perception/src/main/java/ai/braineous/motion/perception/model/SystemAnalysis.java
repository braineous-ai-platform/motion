package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class SystemAnalysis extends MotionBaseModel {

    private Perception perception;
    private String analysisId;
    private String createdAt;
    private String cognitiveQuery;

    public SystemAnalysis() {
    }

    public Perception getPerception() {
        return this.perception;
    }

    public void setPerception(Perception perception) {
        this.perception = perception;
    }

    public String getAnalysisId() {
        return this.analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
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

    @Override
    public String toString() {
        return "SystemAnalysis{" +
                "perception=" + perception +
                ", analysisId='" + analysisId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", cognitiveQuery='" + cognitiveQuery + '\'' +
                '}';
    }
}
