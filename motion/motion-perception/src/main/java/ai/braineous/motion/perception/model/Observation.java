package ai.braineous.motion.perception.model;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import io.braineous.motion.core.model.MotionBaseModel;

public class Observation extends MotionBaseModel
{
    private Observable observable;
    private OperationalView operationalView;
    private GraphSnapshot reasoningView;

    public Observable getObservable()
    {
        return observable;
    }

    public void setObservable(Observable observable)
    {
        this.observable = observable;
    }

    public OperationalView getOperationalView()
    {
        return operationalView;
    }

    public void setOperationalView(OperationalView operationalView)
    {
        this.operationalView = operationalView;
    }

    public GraphSnapshot getReasoningView()
    {
        return reasoningView;
    }

    public void setReasoningView(GraphSnapshot reasoningView)
    {
        this.reasoningView = reasoningView;
    }

    @Override
    public String toString()
    {
        return "Observation{" +
                "observable=" + observable +
                ", operationalView=" + operationalView +
                ", reasoningView=" + reasoningView +
                '}';
    }
}
