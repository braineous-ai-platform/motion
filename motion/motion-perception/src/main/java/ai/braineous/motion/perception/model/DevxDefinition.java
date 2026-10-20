package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

import java.util.List;

public class DevxDefinition extends MotionBaseModel {

    private List<CognitiveTransactionGroup> analysis;
    private List<CognitiveTransactionGroup> mutation;

    public DevxDefinition() {
    }

    public List<CognitiveTransactionGroup> getAnalysis() {
        return this.analysis;
    }

    public void setAnalysis(List<CognitiveTransactionGroup> analysis) {
        this.analysis = analysis;
    }

    public List<CognitiveTransactionGroup> getMutation() {
        return this.mutation;
    }

    public void setMutation(List<CognitiveTransactionGroup> mutation) {
        this.mutation = mutation;
    }

    @Override
    public String toString() {
        return "DevxDefinition{" +
                "analysis=" + analysis +
                ", mutation=" + mutation +
                '}';
    }
}
