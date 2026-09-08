package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionEventNormalizer owns deterministic normalization of inbound artifacts
 * into the canonical MotionEvent. It preserves supplied source temporal truth
 * as originTime, supplies Motion-known envelope received time as receivedAt,
 * and applies the receivedAt fallback only when source origin time is absent.
 *
 * The normalizer does not implement Flink temporal mechanics, timestamp
 * parsing, window calculation, watermarking, lateness, replay placement, or
 * frame lifecycle policy.
 */
@ApplicationScoped
public class MotionEventNormalizer {

    public MotionEvent normalize(MotionEnvelope motionEnvelope) {

        MotionEvent motionEvent = new MotionEvent();

        if (motionEnvelope == null) {
            return motionEvent;
        }

        applyEnvelopeFields(motionEvent, motionEnvelope);
        applyRawEventFields(motionEvent, motionEnvelope.getRawEvent());
        applyOriginTimeFallback(motionEvent);

        return motionEvent;
    }

    private void applyEnvelopeFields(
            MotionEvent motionEvent,
            MotionEnvelope motionEnvelope) {

        motionEvent.setEventId(motionEnvelope.getEnvelopeId());
        motionEvent.setReceivedAt(motionEnvelope.getReceivedAt());
        motionEvent.setSubjectId(motionEnvelope.getCorrelationId());
        motionEvent.setMetadataJson(motionEnvelope.getMetadataJson());
    }

    private void applyRawEventFields(
            MotionEvent motionEvent,
            RawEvent rawEvent) {

        if (rawEvent == null) {
            return;
        }

        motionEvent.setEventType(rawEvent.getEventType());
        motionEvent.setOriginTime(rawEvent.getReceivedAt());
        motionEvent.setSubjectType(rawEvent.getSourceType());
        motionEvent.setOperation(rawEvent.getEventType());
        motionEvent.setPayloadJson(rawEvent.getPayloadJson());
    }

    private void applyOriginTimeFallback(MotionEvent motionEvent) {
        String originTime = motionEvent.getOriginTime();

        if (originTime == null) {
            motionEvent.setOriginTime(motionEvent.getReceivedAt());
            return;
        }

        if (originTime.trim().isEmpty()) {
            motionEvent.setOriginTime(motionEvent.getReceivedAt());
        }
    }
}
