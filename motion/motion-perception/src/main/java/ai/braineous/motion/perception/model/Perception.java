package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class Perception extends MotionBaseModel {
    private Observation observation;

    public Observation getObservation() {
        return observation;
    }

    public void setObservation(Observation observation) {
        this.observation = observation;
    }

    @Override
    public String toString() {
        return "Perception{" +
                "observation=" + observation +
                '}';
    }
}
