package ai.braineous.motion.ingestion.timeprocessor.infra.kafka;

import ai.braineous.motion.ingestion.sinkprocessor.SinkProcessor;
import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.motion.ingestion.timeprocessor.orchestrator.TimeProcessorOrchestrator;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class TimeProcessorConsumer {

    @Inject
    TimeProcessorOrchestrator timeProcessorOrchestrator;

    @Inject
    SinkProcessor sinkProcessor;

    @Incoming("motion-event-in")
    public void consume(String payload) {
        MotionEvent motionEvent = MotionEvent.fromJson(
                payload,
                MotionEvent.class);

        MotionProcessorResult result =
                timeProcessorOrchestrator.process(motionEvent);

        if (result != null) {
            MotionFrame motionFrame =
                    result.getMotionFrame();

            if (motionFrame != null) {
                sinkProcessor.process(motionFrame);
            }
        }
    }
}
