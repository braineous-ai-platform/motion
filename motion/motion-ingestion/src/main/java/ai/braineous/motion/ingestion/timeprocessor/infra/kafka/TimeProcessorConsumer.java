package ai.braineous.motion.ingestion.timeprocessor.infra.kafka;

import ai.braineous.motion.ingestion.timeprocessor.orchestrator.TimeProcessorOrchestrator;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class TimeProcessorConsumer {

    @Inject
    TimeProcessorOrchestrator timeProcessorOrchestrator;

    @Incoming("motion-event-in")
    public void consume(String payload) {
        MotionEvent motionEvent = MotionEvent.fromJson(
                payload,
                MotionEvent.class);

        timeProcessorOrchestrator.process(motionEvent);
    }
}
