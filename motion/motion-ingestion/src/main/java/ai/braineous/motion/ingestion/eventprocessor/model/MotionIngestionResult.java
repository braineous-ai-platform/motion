package ai.braineous.motion.ingestion.eventprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionIngestionResult represents the deterministic outcome produced
 * by the Motion ingestion pipeline after validation and routing decisions.
 *
 * <p>
 * This object communicates whether an inbound event was:
 * </p>
 *
 * <ul>
 *     <li>Accepted</li>
 *     <li>Rejected</li>
 *     <li>Quarantined</li>
 *     <li>Marked replayable</li>
 * </ul>
 *
 * <p>
 * MotionIngestionResult exists primarily for:
 * </p>
 *
 * <ul>
 *     <li>Pipeline observability</li>
 *     <li>Operational debugging</li>
 *     <li>Replay coordination</li>
 *     <li>Failure traceability</li>
 *     <li>Deterministic ingestion feedback</li>
 * </ul>
 *
 * <p>
 * The ingestion result intentionally avoids business interpretation.
 * It communicates ingestion state only.
 * </p>
 *
 * <p>
 * Typical ingestion outcome metadata may include:
 * </p>
 *
 * <ul>
 *     <li>Reason codes</li>
 *     <li>Correlation identifiers</li>
 *     <li>Motion event identifiers</li>
 *     <li>Replay eligibility</li>
 *     <li>Pipeline stage outcomes</li>
 * </ul>
 *
 * <p>
 * MotionIngestionResult is designed to remain stable across:
 * </p>
 *
 * <ul>
 *     <li>Kafka infrastructure changes</li>
 *     <li>Flink processing evolution</li>
 *     <li>CGO/intelligence evolution</li>
 * </ul>
 *
 * <p>
 * It represents ingestion truth only.
 * </p>
 */
public class MotionIngestionResult extends MotionBaseModel {

    private String resultId;
    private String status;
    private String reasonCode;
    private String message;

    private String rawEventId;
    private String envelopeId;
    private String motionEventId;
    private String correlationId;
    private String traceId;

    private String targetTopic;
    private String partitionKey;

    private String rawEventJson;
    private String envelopeJson;
    private String motionEventJson;
    private String replaySignalJson;

    private String metadataJson;

    public MotionIngestionResult() {
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawEventId() {
        return rawEventId;
    }

    public void setRawEventId(String rawEventId) {
        this.rawEventId = rawEventId;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getMotionEventId() {
        return motionEventId;
    }

    public void setMotionEventId(String motionEventId) {
        this.motionEventId = motionEventId;
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

    public String getTargetTopic() {
        return targetTopic;
    }

    public void setTargetTopic(String targetTopic) {
        this.targetTopic = targetTopic;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getRawEventJson() {
        return rawEventJson;
    }

    public void setRawEventJson(String rawEventJson) {
        this.rawEventJson = rawEventJson;
    }

    public String getEnvelopeJson() {
        return envelopeJson;
    }

    public void setEnvelopeJson(String envelopeJson) {
        this.envelopeJson = envelopeJson;
    }

    public String getMotionEventJson() {
        return motionEventJson;
    }

    public void setMotionEventJson(String motionEventJson) {
        this.motionEventJson = motionEventJson;
    }

    public String getReplaySignalJson() {
        return replaySignalJson;
    }

    public void setReplaySignalJson(String replaySignalJson) {
        this.replaySignalJson = replaySignalJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "MotionIngestionResult{" +
                "resultId='" + resultId + '\'' +
                ", status='" + status + '\'' +
                ", reasonCode='" + reasonCode + '\'' +
                ", message='" + message + '\'' +
                ", rawEventId='" + rawEventId + '\'' +
                ", envelopeId='" + envelopeId + '\'' +
                ", motionEventId='" + motionEventId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", targetTopic='" + targetTopic + '\'' +
                ", partitionKey='" + partitionKey + '\'' +
                ", rawEventJson='" + rawEventJson + '\'' +
                ", envelopeJson='" + envelopeJson + '\'' +
                ", motionEventJson='" + motionEventJson + '\'' +
                ", replaySignalJson='" + replaySignalJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}