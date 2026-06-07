package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionFrameRoutingKeyResolver derives the deterministic routing
 * identity used by the TimeProcessor runtime to locate the active
 * MotionFrame for an incoming MotionEvent.
 *
 * The resolver translates MotionEvent subject identity into a stable
 * MotionFrameRoutingKey that can be used by downstream runtime
 * components to resolve active operational continuity.
 *
 * The routing key represents the operational continuity domain for
 * frame resolution. It does not define temporal windows, frame
 * lifecycle behavior, persistence behavior, append behavior, replay
 * semantics, EvolvingContext assembly, operational intelligence, or
 * governance behavior.
 *
 * MotionFrameRoutingKeyResolver is intentionally deterministic:
 * the same MotionEvent subject identity produces the same routing
 * identity.
 *
 * Current routing identity format:
 *
 * subjectId:subjectType:frameType
 *
 * Current frameType convention:
 *
 * subjectType + "_OPERATION_FRAME"
 *
 * Example:
 *
 * ORDER-1001:ORDER:ORDER_OPERATION_FRAME
 */
@ApplicationScoped
public class MotionFrameRoutingKeyResolver {

    public MotionFrameRoutingKey resolve(MotionEvent motionEvent) {

        if (motionEvent == null) {
            return null;
        }

        String subjectId =
                safe(motionEvent.getSubjectId());

        String subjectType =
                safe(motionEvent.getSubjectType());

        if (subjectId == null || subjectType == null) {
            return null;
        }

        String frameType =
                subjectType + "_OPERATION_FRAME";

        String routingKey =
                subjectId + ":" + subjectType + ":" + frameType;

        MotionFrameRoutingKey out =
                new MotionFrameRoutingKey();

        out.setRoutingKey(routingKey);
        out.setSubjectId(subjectId);
        out.setSubjectType(subjectType);
        out.setFrameType(frameType);

        return out;
    }

    private String safe(String value) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}
