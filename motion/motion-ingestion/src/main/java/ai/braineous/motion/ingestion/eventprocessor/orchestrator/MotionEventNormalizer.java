package ai.braineous.motion.ingestion.eventprocessor.orchestrator;

import ai.braineous.motion.ingestion.eventprocessor.model.MotionEnvelope;
import ai.braineous.motion.ingestion.eventprocessor.model.RawEvent;
import io.braineous.motion.core.model.MotionEvent;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * MotionEventNormalizer transforms inbound ingestion artifacts into
 * Motion's canonical event representation.
 *
 * <p>
 * MotionEventNormalizer is responsible for converting source-specific
 * event structures into a normalized MotionEvent that can be processed
 * consistently throughout the Motion runtime.
 * </p>
 *
 * <p>
 * The normalizer establishes Motion's canonical event boundary by
 * elevating inbound ingestion artifacts into a standardized event
 * representation.
 * </p>
 *
 * <p>
 * Typical normalization responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Reading RawEvent content</li>
 *     <li>Reading MotionEnvelope metadata</li>
 *     <li>Constructing MotionEvent identifiers</li>
 *     <li>Mapping source event metadata</li>
 *     <li>Producing canonical MotionEvent instances</li>
 * </ul>
 *
 * <p>
 * MotionEventNormalizer intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Validation decisions</li>
 *     <li>Replay evaluation</li>
 *     <li>Event publication</li>
 *     <li>Persistence operations</li>
 *     <li>Business-domain interpretation</li>
 *     <li>Temporal intelligence calculations</li>
 * </ul>
 *
 * <p>
 * MotionEventNormalizer focuses solely on canonical event creation.
 * The resulting MotionEvent becomes the authoritative event model
 * used by downstream Motion processing stages.
 * </p>
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

        return motionEvent;
    }

    private void applyEnvelopeFields(
            MotionEvent motionEvent,
            MotionEnvelope motionEnvelope) {

        motionEvent.setEventId(motionEnvelope.getEnvelopeId());
        motionEvent.setOccurredAt(motionEnvelope.getReceivedAt());
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
        motionEvent.setSubjectType(rawEvent.getSourceType());
        motionEvent.setOperation(rawEvent.getEventType());
        motionEvent.setPayloadJson(rawEvent.getPayloadJson());
    }
}