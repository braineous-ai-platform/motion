package io.braineous.motion.core.model;

/**
 * MotionEvent is the canonical Motion ingestion and runtime event primitive.
 * It preserves originTime as the source-declared operational occurrence time
 * and receivedAt as Motion's observation and admission time. Both clocks are
 * retained because Motion Time-axis semantics require both temporal facts.
 * When source origin time is absent, normalization assigns receivedAt as the
 * deterministic originTime fallback.
 *
 * Flink may later execute against this Motion-owned model but does not define
 * it. MotionEvent does not parse timestamps or establish timezone, window,
 * watermark, lateness, replay-placement, or frame-lifecycle policy.
 */
public class MotionEvent extends MotionBaseModel {

    private String eventId;
    private String eventType;
    private String originTime;
    private String receivedAt;
    private String subjectId;
    private String subjectType;
    private String operation;
    private String payloadJson;
    private String metadataJson;
    private MotionReplaySignal replaySignal;

    public MotionEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOriginTime() {
        return originTime;
    }

    public void setOriginTime(String originTime) {
        this.originTime = originTime;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

    public MotionReplaySignal getReplaySignal() {
        return replaySignal;
    }

    public void setReplaySignal(MotionReplaySignal replaySignal) {
        this.replaySignal = replaySignal;
    }

    //-------------------------------------------
    @Override
    public String toString() {
        return "MotionEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", originTime='" + originTime + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", operation='" + operation + '\'' +
                ", payloadJson='" + payloadJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                ", replaySignal=" + replaySignal +
                '}';
    }

    //---Json_support------------------------
}
