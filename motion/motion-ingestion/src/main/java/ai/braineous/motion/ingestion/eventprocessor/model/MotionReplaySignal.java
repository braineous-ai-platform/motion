package ai.braineous.motion.ingestion.eventprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionReplaySignal represents replay intent generated during motion
 * ingestion, motion processing, or intelligence execution.
 *
 * <p>
 * This object does not perform replay. It only describes the replay
 * context, replay scope, and replay guidance associated with a motion.
 * </p>
 *
 * <p>
 * Replay in Motion is broader than operational recovery.
 * Replay may be used for:
 * </p>
 *
 * <ul>
 *     <li>Failure recovery after infrastructure or processing errors</li>
 *     <li>Motion reconstruction from historical event streams</li>
 *     <li>Predictive Intelligence backtesting and temporal analysis</li>
 * </ul>
 *
 * <p>
 * MotionReplaySignal exists to provide deterministic replay guidance
 * without coupling replay behavior to any specific infrastructure,
 * storage engine, orchestration framework, or intelligence runtime.
 * </p>
 *
 * <p>
 * Typical replay signal responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Identifying replay intent and replay level</li>
 *     <li>Capturing replay reason codes</li>
 *     <li>Providing replay timeline boundaries</li>
 *     <li>Identifying replay starting context</li>
 *     <li>Supporting deterministic operational recovery</li>
 *     <li>Supporting temporal reconstruction and backtesting</li>
 * </ul>
 *
 * <p>
 * MotionReplaySignal intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Kafka retry implementation</li>
 *     <li>Replay execution logic</li>
 *     <li>Replay scheduling</li>
 *     <li>Business correction logic</li>
 *     <li>Policy decisions</li>
 *     <li>Predictive Intelligence calculations</li>
 * </ul>
 *
 * <p>
 * Replay behavior is executed later by infrastructure services,
 * motion services, intelligence services, or operational tooling.
 * This object only carries replay intent and replay metadata.
 * </p>
 */
public class MotionReplaySignal extends MotionBaseModel {

    private String replaySignalId;
    private String replayLevel;
    private String reasonCode;
    private String message;

    private String rawEventId;
    private String envelopeId;
    private String motionEventId;
    private String correlationId;
    private String traceId;

    private String tenantId;
    private String source;
    private String eventType;

    private String replayFromTime;
    private String replayToTime;
    private String asOfTime;
    private String timelineId;

    private String targetTopic;
    private String partitionKey;

    private String rawEventJson;
    private String envelopeJson;
    private String motionEventJson;
    private String cgoStateRefJson;
    private String actualOutcomeJson;
    private String metadataJson;

    public String getReplaySignalId() {
        return replaySignalId;
    }

    public void setReplaySignalId(String replaySignalId) {
        this.replaySignalId = replaySignalId;
    }

    public String getReplayLevel() {
        return replayLevel;
    }

    public void setReplayLevel(String replayLevel) {
        this.replayLevel = replayLevel;
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReplayFromTime() {
        return replayFromTime;
    }

    public void setReplayFromTime(String replayFromTime) {
        this.replayFromTime = replayFromTime;
    }

    public String getReplayToTime() {
        return replayToTime;
    }

    public void setReplayToTime(String replayToTime) {
        this.replayToTime = replayToTime;
    }

    public String getAsOfTime() {
        return asOfTime;
    }

    public void setAsOfTime(String asOfTime) {
        this.asOfTime = asOfTime;
    }

    public String getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(String timelineId) {
        this.timelineId = timelineId;
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

    public String getCgoStateRefJson() {
        return cgoStateRefJson;
    }

    public void setCgoStateRefJson(String cgoStateRefJson) {
        this.cgoStateRefJson = cgoStateRefJson;
    }

    public String getActualOutcomeJson() {
        return actualOutcomeJson;
    }

    public void setActualOutcomeJson(String actualOutcomeJson) {
        this.actualOutcomeJson = actualOutcomeJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}