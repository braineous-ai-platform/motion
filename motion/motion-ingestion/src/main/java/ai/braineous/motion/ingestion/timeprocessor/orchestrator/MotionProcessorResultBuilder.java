package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameAppendResult;
import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionProcessorResultBuilder builds the public TimeProcessor result
 * from a MotionFrameAppendResult.
 *
 * The builder represents the response-mapping boundary of the
 * TimeProcessor pipeline. It converts append-level state into the
 * processor-level result returned by the TimeProcessor orchestration
 * layer.
 *
 * MotionProcessorResultBuilder does not resolve routing identities,
 * resolve frames, append MotionEvents, evaluate temporal windows,
 * close frames, persist state, publish events, assemble EvolvingContext
 * instances, or perform operational intelligence.
 *
 * Its responsibility is intentionally narrow:
 *
 * convert append outcome into processor outcome.
 */

@ApplicationScoped
public class MotionProcessorResultBuilder {

    public MotionProcessorResult build(
            MotionFrameAppendResult appendResult) {

        if (appendResult == null) {
            return failure(
                    "APPEND_RESULT_NOT_FOUND",
                    "MotionFrameAppendResult cannot be null");
        }

        MotionProcessorResult result =
                new MotionProcessorResult();

        result.setResultId(
                safe(appendResult.getResultId()) + ":processor");

        result.setStatus(
                appendResult.getStatus());

        result.setCode(
                appendResult.getCode());

        result.setReason(
                appendResult.getReason());

        result.setAppendResultJson(
                appendResult.toJson());

        if (appendResult.getMotionFrame() != null) {
            result.setMotionFrameJson(
                    appendResult.getMotionFrame().toJson());
        }

        result.setMetadataJson(
                "{\"runtime\":\"motion\"}");

        return result;
    }

    private MotionProcessorResult failure(
            String code,
            String reason) {

        MotionProcessorResult result =
                new MotionProcessorResult();

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