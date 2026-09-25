package ai.braineous.motion.perception.observation;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import ai.braineous.motion.perception.model.Observation;
import ai.braineous.motion.perception.model.Observable;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ObservationOrchestrator {
    // Observation assembly machinery is introduced as the observation contract evolves.
    public Observation observe(Observable observable) {
        OperationalView operationalView =
                assembleOperationalView(observable);

        GraphSnapshot reasoningView =
                assembleReasoningView(observable);

        Observation observation = new Observation();
        observation.setOperationalView(operationalView);
        observation.setReasoningView(reasoningView);

        return observation;
    }

    private OperationalView assembleOperationalView(
            Observable observable) {
        // TODO
        return null;
    }

    private GraphSnapshot assembleReasoningView(
            Observable observable) {
        // TODO
        return null;
    }
}
