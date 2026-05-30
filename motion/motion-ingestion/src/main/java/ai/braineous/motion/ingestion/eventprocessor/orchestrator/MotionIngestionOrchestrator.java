package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

/**
 * MotionIngestionOrchestrator coordinates the end-to-end ingestion
 * lifecycle for inbound motion events.
 *
 * <p>
 * MotionIngestionOrchestrator serves as the primary entry point into
 * the Motion ingestion runtime. It accepts inbound event requests and
 * coordinates validation, normalization, replay evaluation, result
 * generation, and event publication.
 * </p>
 *
 * <p>
 * The orchestrator owns workflow coordination but does not own the
 * implementation details of any individual processing stage.
 * Processing responsibilities are delegated to specialized runtime
 * components.
 * </p>
 *
 * <p>
 * Typical orchestration responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Accepting inbound motion ingestion requests</li>
 *     <li>Coordinating validation execution</li>
 *     <li>Coordinating event normalization</li>
 *     <li>Coordinating replay evaluation</li>
 *     <li>Coordinating event publication</li>
 *     <li>Producing ingestion results</li>
 * </ul>
 *
 * <p>
 * MotionIngestionOrchestrator intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Validation rule implementation</li>
 *     <li>Event transformation logic</li>
 *     <li>Replay decision logic</li>
 *     <li>Messaging implementation details</li>
 *     <li>Persistence implementation details</li>
 *     <li>Temporal intelligence calculations</li>
 * </ul>
 *
 * <p>
 * The orchestrator is responsible for ingestion workflow execution
 * only. Downstream consumers such as motion processing, temporal
 * evolution, intelligence services, and future predictive systems
 * operate independently after ingestion completes.
 * </p>
 */
import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MotionIngestionOrchestrator {

    @Inject
    MotionValidationOrchestrator validationOrchestrator;

    @Inject
    MotionEventNormalizer eventNormalizer;

    @Inject
    MotionReplayOrchestrator replayOrchestrator;

    @Inject
    MotionEventPublisher eventPublisher;

    public MotionEvent ingest(MotionEnvelope motionEnvelope) {

        boolean valid =
                validationOrchestrator.validate(motionEnvelope);

        if (!valid) {
            return null;
        }

        MotionEvent motionEvent =
                eventNormalizer.normalize(motionEnvelope);

        MotionReplaySignal replaySignal =
                replayOrchestrator.evaluate(motionEvent);

        MotionEvent publishedEvent =
                eventPublisher.publish(motionEvent);

        return publishedEvent;
    }
}
