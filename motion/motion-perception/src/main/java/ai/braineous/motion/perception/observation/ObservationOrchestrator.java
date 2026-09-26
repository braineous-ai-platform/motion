package ai.braineous.motion.perception.observation;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.rag.prompt.cgo.api.Edge;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphBuilder;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ObservationOrchestrator {
    public Observation observe(Observable observable) {
        GraphSnapshot reasoningView =
                assembleReasoningView(observable);

        OperationalView operationalView =
                assembleOperationalView(reasoningView);

        Observation observation = new Observation();
        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        return observation;
    }

    private GraphSnapshot assembleReasoningView(
            Observable observable) {
        GraphSnapshot graphSnapshot =
                GraphBuilder.getInstance().snapshot();

        Map<String, Fact> selectedNodes =
                new HashMap<String, Fact>();

        for (Fact persistedFact : graphSnapshot.nodes().values()) {
            String[] idTokens = persistedFact.getId().split(":");
            String persistedFactKind = idTokens[0];

            for (Fact observableFact : observable.getFacts()) {
                if (persistedFactKind.equals(observableFact.getId())) {
                    selectedNodes.put(
                            persistedFact.getId(),
                            persistedFact);
                    break;
                }
            }
        }

        Map<String, Edge> selectedEdges =
                new HashMap<String, Edge>();

        return new GraphSnapshot(selectedNodes, selectedEdges);
    }

    private OperationalView assembleOperationalView(
            GraphSnapshot reasoningView) {
        OperationalView operationalView = new OperationalView();
        List<Fact> observedView = new ArrayList<Fact>();

        for (Fact fact : reasoningView.nodes().values()) {
            observedView.add(fact);
        }

        operationalView.setObservedView(observedView);

        return operationalView;
    }
}
