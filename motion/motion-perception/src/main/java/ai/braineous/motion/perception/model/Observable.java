package ai.braineous.motion.perception.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import io.braineous.motion.core.model.MotionBaseModel;

import java.util.List;

public class Observable extends MotionBaseModel {
    private List<Fact> facts;

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    @Override
    public String toString() {
        return "Observable{" +
                "facts=" + facts +
                '}';
    }
}
