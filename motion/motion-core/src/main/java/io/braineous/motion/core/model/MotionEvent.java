package io.braineous.motion.core.model;

/**
 * MotionEvent represents a normalized operational signal participating
 * in temporal operational motion within the Motion runtime.
 *
 * Unlike transport-level events, MotionEvent abstracts operational
 * meaning from underlying event infrastructure and serves as the
 * foundational runtime primitive for operational evolution.
 *
 * MotionEvents are accumulated into MotionFrames across bounded
 * temporal windows, allowing the runtime to reason about operational
 * progression, transition, and evolving context over time.
 *
 * Within Motion, time acts as the primary operational axis through
 * which intelligent runtime behavior emerges from continuously
 * evolving operational state.
 */
public class MotionEvent extends MotionBaseModel{

    private String eventId;
    private String eventType;
    private String occurredAt;
    private String subjectId;
    private String subjectType;
    private String operation;
    private String payloadJson;
    private String metadataJson;

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

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
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

    //-------------------------------------------
    @Override
    public String toString() {
        return "MotionEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", occurredAt='" + occurredAt + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", operation='" + operation + '\'' +
                ", payloadJson='" + payloadJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }

    //---Json_support------------------------
}