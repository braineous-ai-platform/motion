package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameAppendResult;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;

/**
 * MotionFrameAppender appends a MotionEvent to an active MotionFrame.
 *
 * The appender represents the first runtime evolution step inside the
 * TimeProcessor pipeline. Once a MotionFrame has been resolved, the
 * appender is responsible for attaching the incoming MotionEvent to
 * the active frame timeline.
 *
 * MotionFrameAppender does not resolve routing identities, resolve
 * active frames, evaluate temporal windows, close frames, persist
 * state, publish events, assemble EvolvingContext instances, build
 * MotionProcessorResult responses, perform operational intelligence,
 * or own persistence implementation details.
 *
 * Its responsibility is intentionally narrow:
 *
 * append a MotionEvent to a MotionFrame and return deterministic
 * append state.
 */
@ApplicationScoped
public class MotionFrameAppender {

    public MotionFrameAppendResult append(
            MotionFrame frame,
            MotionEvent event) {

        if (frame == null) {
            return failure(
                    "FRAME_NOT_FOUND",
                    "MotionFrame cannot be null");
        }

        if (event == null) {
            return failure(
                    "EVENT_NOT_FOUND",
                    "MotionEvent cannot be null");
        }

        if (frame.getMotionEvents() == null) {
            frame.setMotionEvents(
                    new ArrayList<MotionEvent>());
        }

        frame.getMotionEvents().add(event);

        MotionFrameAppendResult result =
                new MotionFrameAppendResult();

        result.setResultId(
                safe(frame.getFrameId()) + ":append");

        result.setStatus("SUCCESS");
        result.setCode("EVENT_APPENDED");
        result.setReason("MotionEvent appended to MotionFrame");

        result.setFrameCreated("false");
        result.setFrameReused("true");

        result.setMotionFrame(frame);
        result.setMetadataJson("{\"runtime\":\"motion\"}");

        return result;
    }

    private MotionFrameAppendResult failure(
            String code,
            String reason) {

        MotionFrameAppendResult result =
                new MotionFrameAppendResult();

        result.setStatus("FAILURE");
        result.setCode(code);
        result.setReason(reason);
        result.setMetadataJson("{\"runtime\":\"motion\"}");

        return result;
    }

    private String safe(String value) {

        if (value == null) {
            return "unknown";
        }

        String trimmed =
                value.trim();

        if (trimmed.isEmpty()) {
            return "unknown";
        }

        return trimmed;
    }
}