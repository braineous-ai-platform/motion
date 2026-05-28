package ai.braineous.motion.ingestion.eventprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionEnvelope represents the standardized ingestion wrapper
 * surrounding a RawEvent as it enters the Motion pipeline.
 *
 * <p>
 * The purpose of MotionEnvelope is to establish a deterministic
 * ingestion boundary independent of the originating producer system.
 * </p>
 *
 * <p>
 * MotionEnvelope introduces stable ingestion metadata required for:
 * </p>
 *
 * <ul>
 *     <li>Traceability</li>
 *     <li>Correlation</li>
 *     <li>Tenant/context isolation</li>
 *     <li>Replay coordination</li>
 *     <li>Pipeline observability</li>
 *     <li>Deterministic routing</li>
 * </ul>
 *
 * <p>
 * MotionEnvelope does not perform semantic interpretation of the event.
 * It exists strictly as an ingestion-level transport and coordination wrapper.
 * </p>
 *
 * <p>
 * Typical envelope responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Assigning stable ingestion identifiers</li>
 *     <li>Capturing source metadata</li>
 *     <li>Capturing ingestion timestamps</li>
 *     <li>Capturing correlation and trace identifiers</li>
 *     <li>Preserving original payload boundaries</li>
 * </ul>
 *
 * <p>
 * MotionEnvelope is transformed later into MotionEvent,
 * where normalized motion semantics are introduced.
 * </p>
 *
 * <pre>
 * RawEvent
 *     -> MotionEnvelope
 *     -> MotionEvent
 * </pre>
 *
 * <p>
 * MotionEnvelope intentionally remains:
 * </p>
 *
 * <ul>
 *     <li>Domain-neutral</li>
 *     <li>Infrastructure-agnostic</li>
 *     <li>Execution-safe</li>
 *     <li>Replay-aware</li>
 * </ul>
 */
public class MotionEnvelope extends MotionBaseModel {

    private String envelopeId;
    private String tenantId;
    private String correlationId;
    private String traceId;
    private String receivedAt;
    private RawEvent rawEvent;
    private String metadataJson;

    public MotionEnvelope() {
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public RawEvent getRawEvent() {
        return rawEvent;
    }

    public void setRawEvent(RawEvent rawEvent) {
        this.rawEvent = rawEvent;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "MotionEnvelope{" +
                "envelopeId='" + envelopeId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", rawEvent=" + rawEvent +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}