package ai.braineous.motion.perception.model;

import ai.braineous.rag.prompt.cgo.api.Fact;
import io.braineous.motion.core.model.MotionBaseModel;

import java.util.List;

public class Observable extends MotionBaseModel
{
    private Fact observableFactAnchor;
    private List<Fact> observableFacts;

    public Fact getObservableFactAnchor()
    {
        return observableFactAnchor;
    }

    public void setObservableFactAnchor(Fact observableFactAnchor)
    {
        this.observableFactAnchor = observableFactAnchor;
    }

    public List<Fact> getObservableFacts()
    {
        return observableFacts;
    }

    public void setObservableFacts(List<Fact> observableFacts)
    {
        this.observableFacts = observableFacts;
    }

    @Override
    public String toString()
    {
        return "Observable{" +
                "observableFactAnchor=" + observableFactAnchor +
                ", observableFacts=" + observableFacts +
                '}';
    }
}
