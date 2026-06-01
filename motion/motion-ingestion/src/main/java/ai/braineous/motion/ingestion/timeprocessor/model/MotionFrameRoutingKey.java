package ai.braineous.motion.ingestion.timeprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionFrameRoutingKey represents the deterministic routing identity
 * used by the Motion runtime to resolve the active MotionFrame that
 * should receive an incoming MotionEvent.
 *
 * Routing keys provide a stable operational grouping mechanism that
 * allows related MotionEvents to accumulate into the same MotionFrame
 * during an active runtime window.
 *
 * The routing key does not define temporal boundaries, frame lifecycle,
 * persistence behavior, or frame evolution semantics.
 *
 * Its sole responsibility is identifying the operational continuity
 * domain against which MotionFrame resolution occurs.
 *
 * MotionProcessor uses MotionFrameRoutingKey to locate existing active
 * MotionFrames or create new MotionFrames when no active frame exists
 * for the resolved routing identity.
 */
public class MotionFrameRoutingKey extends MotionBaseModel {

    private String routingKey;
    private String subjectId;
    private String subjectType;
    private String frameType;

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
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

    public String getFrameType() {
        return frameType;
    }

    public void setFrameType(String frameType) {
        this.frameType = frameType;
    }

    @Override
    public String toString() {
        return "MotionFrameRoutingKey{" +
                "routingKey='" + routingKey + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", frameType='" + frameType + '\'' +
                '}';
    }
}