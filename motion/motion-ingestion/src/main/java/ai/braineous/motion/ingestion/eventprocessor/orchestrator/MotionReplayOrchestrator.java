package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionReplaySignal;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionReplayOrchestrator coordinates replay evaluation for
 * normalized Motion events.
 *
 * <p>
 * MotionReplayOrchestrator is responsible for determining replay
 * guidance associated with a MotionEvent and producing a
 * MotionReplaySignal for downstream operational use.
 * </p>
 *
 * <p>
 * The orchestrator coordinates replay evaluation but does not own
 * replay execution. Replay execution is performed later by runtime
 * infrastructure, operational tooling, or future temporal services.
 * </p>
 *
 * <p>
 * Typical replay responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Evaluating replay eligibility</li>
 *     <li>Producing replay reason codes</li>
 *     <li>Producing replay guidance</li>
 *     <li>Identifying replay boundaries</li>
 *     <li>Constructing MotionReplaySignal instances</li>
 * </ul>
 *
 * <p>
 * MotionReplayOrchestrator intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Replay execution</li>
 *     <li>Replay scheduling</li>
 *     <li>Kafka retry implementation</li>
 *     <li>Event publication</li>
 *     <li>Business-domain interpretation</li>
 *     <li>Temporal intelligence calculations</li>
 * </ul>
 *
 * <p>
 * Replay evaluation determines replay intent only.
 * The resulting MotionReplaySignal becomes the authoritative
 * replay contract used by downstream operational systems.
 * </p>
 */
@ApplicationScoped
public class MotionReplayOrchestrator {

    public MotionReplaySignal evaluate(MotionEvent motionEvent) {

        MotionReplaySignal replaySignal = new MotionReplaySignal();

        replaySignal.setReplayLevel("FAILURE_RECOVERY");
        replaySignal.setReasonCode("REPLAY_NOT_REQUIRED");
        replaySignal.setMessage("Replay is not required for accepted Motion event");

        if (motionEvent != null) {
            replaySignal.setMotionEventId(motionEvent.getEventId());
            replaySignal.setEventType(motionEvent.getEventType());
            replaySignal.setReplayFromTime(motionEvent.getOccurredAt());
            replaySignal.setReplayToTime(motionEvent.getOccurredAt());
            replaySignal.setAsOfTime(motionEvent.getOccurredAt());
            replaySignal.setMotionEventJson(motionEvent.toJson());
            replaySignal.setMetadataJson(motionEvent.getMetadataJson());
        }

        return replaySignal;
    }
}