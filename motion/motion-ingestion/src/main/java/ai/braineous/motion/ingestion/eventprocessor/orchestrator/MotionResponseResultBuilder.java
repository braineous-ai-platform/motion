package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionResponseResult;
import ai.braineous.motion.ingestion.eventprocessor.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionResponseResultBuilder builds deterministic Motion response
 * results from canonical Motion ingestion runtime artifacts.
 *
 * <p>
 * The builder converts normalized event state, replay guidance, and
 * publication outcome into a caller-facing response result.
 * </p>
 *
 * <p>
 * MotionResponseResultBuilder exists to keep response construction
 * separate from ingestion workflow coordination. The ingestion
 * orchestrator owns stage coordination, while this builder owns the
 * construction of the deterministic response object returned to
 * callers.
 * </p>
 *
 * <p>
 * MotionResponseResultBuilder intentionally avoids infrastructure
 * interpretation. It does not decide Kafka delivery guarantees, Flink
 * processing success, downstream persistence state, business outcomes,
 * or temporal intelligence results.
 * </p>
 *
 * <p>
 * The response produced here represents Motion ingestion response
 * truth only.
 * </p>
 */
@ApplicationScoped
public class MotionResponseResultBuilder {

    public MotionResponseResult build(MotionEvent motionEvent,
                                      MotionReplaySignal replaySignal,
                                      MotionEvent publishedEvent) {

        MotionResponseResult result =
                new MotionResponseResult();

        result.setStatus("ACCEPTED");
        result.setReasonCode("MOTION_EVENT_ACCEPTED");
        result.setMessage("Motion event accepted into ingestion pipeline");

        if (motionEvent != null) {
            result.setResultId(motionEvent.getEventId());
            result.setMotionEventId(motionEvent.getEventId());
            result.setMotionEventJson(motionEvent.toJson());
            result.setMetadataJson(motionEvent.getMetadataJson());
        }

        if (replaySignal != null) {
            result.setReplaySignalJson(replaySignal.toJson());
        }

        if (publishedEvent != null) {
            result.setMotionEventId(publishedEvent.getEventId());
            result.setMotionEventJson(publishedEvent.toJson());
            result.setMetadataJson(publishedEvent.getMetadataJson());
        }

        return result;
    }
}
