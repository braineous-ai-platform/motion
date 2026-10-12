package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class SystemAnalysis extends MotionBaseModel {

    private Perception perception;

    public SystemAnalysis() {
    }

    public Perception getPerception() {
        return this.perception;
    }

    public void setPerception(Perception perception) {
        this.perception = perception;
    }

    @Override
    public String toString() {
        return "SystemAnalysis{" +
                "perception=" + perception +
                '}';
    }
}
