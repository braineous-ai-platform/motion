package ai.braineous.motion.ingestion.eventprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionResponseResult represents the deterministic response produced
 * by the Motion ingestion pipeline after validation, normalization,
 * replay evaluation, and publication.
 *
 * <p>
 * This object communicates the caller-facing outcome of an inbound
 * Motion ingestion request.
 * </p>
 *
 * <p>
 * MotionResponseResult exists primarily for:
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
 * The response result intentionally avoids business interpretation.
 * It communicates Motion ingestion state only.
 * </p>
 *
 * <p>
 * MotionResponseResult is designed to remain stable across:
 * </p>
 *
 * <ul>
 *     <li>Kafka infrastructure changes</li>
 *     <li>Flink processing evolution</li>
 *     <li>CGO/intelligence evolution</li>
 * </ul>
 *
 * <p>
 * It represents ingestion response truth only.
 * </p>
 */
public class MotionResponseResult extends MotionBaseModel {

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

    public MotionResponseResult() {
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
}