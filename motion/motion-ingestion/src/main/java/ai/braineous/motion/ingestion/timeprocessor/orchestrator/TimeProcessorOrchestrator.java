package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameAppendResult;
import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.motion.ingestion.timeprocessor.model.MotionProcessorResult;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningRecord;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningStore;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * TimeProcessorOrchestrator is the top-level orchestration boundary for
 * the TimeProcessor pipeline.
 *
 * The orchestrator starts after MotionIngestionOrchestrator has already
 * validated, normalized, replay-evaluated, and published a MotionEvent.
 * Its input is the canonical MotionEvent produced by ingestion.
 *
 * TimeProcessorOrchestrator coordinates the TimeProcessor components:
 *
 * MotionEvent
 * -> MotionFrameRoutingKey
 * -> active MotionFrame
 * -> appended MotionFrame
 * -> persisted happening state
 * -> MotionProcessorResult
 *
 * The orchestrator owns pipeline sequencing and active frame persistence.
 * It does not normalize raw events, evaluate ingestion replay, publish
 * MotionEvents, assemble EvolvingContext, perform operational intelligence,
 * or own low-level MongoDB implementation details.
 *
 * Its responsibility is intentionally narrow:
 *
 * process one MotionEvent into active time-frame evolution and return the
 * processor outcome.
 */
@ApplicationScoped
public class TimeProcessorOrchestrator {

    @Inject
    MotionFrameRoutingKeyResolver routingKeyResolver;

    @Inject
    MotionFrameResolver frameResolver;

    @Inject
    MotionFrameAppender frameAppender;

    @Inject
    MotionProcessorResultBuilder resultBuilder;

    @Inject
    MotionHappeningStore happeningStore;

    public MotionProcessorResult process(MotionEvent event) {

        MotionFrameRoutingKey routingKey =
                routingKeyResolver.resolve(event);

        if (routingKey == null) {
            return resultBuilder.build(
                    failureAppendResult(
                            "ROUTING_KEY_NOT_FOUND",
                            "MotionFrameRoutingKey cannot be resolved"));
        }

        MotionFrame frame =
                frameResolver.resolve(routingKey);

        MotionFrameAppendResult appendResult =
                frameAppender.append(
                        frame,
                        event);

        if ("SUCCESS".equals(appendResult.getStatus())) {
            persist(
                    routingKey,
                    appendResult.getMotionFrame());
        }

        return resultBuilder.build(
                appendResult);
    }

    private void persist(
            MotionFrameRoutingKey routingKey,
            MotionFrame frame) {

        if (routingKey == null) {
            return;
        }

        if (frame == null) {
            return;
        }

        MotionHappeningRecord record =
                new MotionHappeningRecord();

        record.setRecordId(
                safe(routingKey.getRoutingKey()) + ":happening");

        record.setRoutingKey(
                safe(routingKey.getRoutingKey()));

        record.setSubjectId(
                safe(routingKey.getSubjectId()));

        record.setSubjectType(
                safe(routingKey.getSubjectType()));

        record.setFrameId(
                safe(frame.getFrameId()));

        record.setFrameType(
                safe(frame.getFrameType()));

        record.setWindowStart(
                frame.getWindowStart());

        record.setWindowEnd(
                frame.getWindowEnd());

        record.setSequence(
                frame.getSequence());

        record.setStatus(
                frame.getStatus());

        record.setMotionFrame(
                frame);

        record.setCreatedAt(
                null);

        record.setUpdatedAt(
                null);

        happeningStore.addRecord(record);
    }

    private MotionFrameAppendResult failureAppendResult(
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