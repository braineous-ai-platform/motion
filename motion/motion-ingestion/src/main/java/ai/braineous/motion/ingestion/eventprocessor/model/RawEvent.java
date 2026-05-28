package ai.braineous.motion.ingestion.eventprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * RawEvent represents the original inbound event entering the Motion ingestion pipeline.
 *
 * <p>
 * This object intentionally preserves the incoming event in its original shape
 * before normalization, validation, partition resolution, or routing occurs.
 * The source payload may be incomplete, unordered, duplicated, noisy,
 * infrastructure-specific, or domain-specific.
 * </p>
 *
 * <p>
 * RawEvent exists as the boundary object between external producer systems
 * and the deterministic Motion ingestion pipeline.
 * </p>
 *
 * <p>
 * Responsibilities intentionally NOT handled by RawEvent:
 * </p>
 *
 * <ul>
 *     <li>Semantic interpretation</li>
 *     <li>Domain intelligence</li>
 *     <li>Graph/context enrichment</li>
 *     <li>Time/window processing</li>
 *     <li>Replay decisions</li>
 *     <li>Kafka/Flink infrastructure behavior</li>
 * </ul>
 *
 * <p>
 * The ingestion pipeline later transforms:
 * </p>
 *
 * <pre>
 * RawEvent
 *     -> MotionEnvelope
 *     -> MotionEvent
 * </pre>
 *
 * <p>
 * RawEvent is intentionally infrastructure-agnostic and domain-neutral.
 * </p>
 */
public class RawEvent extends MotionBaseModel {

    private String rawEventId;
    private String source;
    private String sourceType;
    private String eventType;
    private String receivedAt;
    private String payloadJson;
    private String metadataJson;

    public RawEvent() {
    }

    public String getRawEventId() {
        return rawEventId;
    }

    public void setRawEventId(String rawEventId) {
        this.rawEventId = rawEventId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "RawEvent{" +
                "rawEventId='" + rawEventId + '\'' +
                ", source='" + source + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", eventType='" + eventType + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", payloadJson='" + payloadJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}