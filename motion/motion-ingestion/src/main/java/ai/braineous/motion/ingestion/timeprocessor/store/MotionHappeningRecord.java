package ai.braineous.motion.ingestion.timeprocessor.store;

import io.braineous.motion.core.model.MotionBaseModel;
import io.braineous.motion.core.model.MotionFrame;

/**
 * Atomic persistence boundary for active operational continuity.
 *
 * A MotionHappeningRecord stores the currently active MotionFrame
 * being evolved by the Motion runtime.
 *
 * Unlike MotionFrameRecord, which preserves completed historical
 * evolution windows, MotionHappeningRecord represents runtime state
 * that is still accepting MotionEvents and may continue evolving
 * through additional operational activity.
 *
 * MotionFrame remains the semantic payload.
 *
 * The duplicated scalar fields exist only to support deterministic
 * active-frame lookup and runtime resolution without requiring full
 * MotionFrame deserialization for every operation.
 *
 * This record does not own routing behavior, window evaluation,
 * append orchestration, frame lifecycle decisions, frame closure,
 * EvolvingContext assembly, operational intelligence processing,
 * governance behavior, or persistence orchestration.
 *
 * It simply preserves the currently happening MotionFrame so the
 * Motion runtime can recover, reuse, and continue operational
 * continuity across processing boundaries.
 */
public class MotionHappeningRecord extends MotionBaseModel {

    private String recordId;

    private String routingKey;

    private String subjectId;
    private String subjectType;

    private String frameId;
    private String frameType;

    private String windowStart;
    private String windowEnd;

    private String sequence;
    private String status;

    private MotionFrame motionFrame;

    private String createdAt;
    private String updatedAt;

    public MotionHappeningRecord() {
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

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

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getFrameType() {
        return frameType;
    }

    public void setFrameType(String frameType) {
        this.frameType = frameType;
    }

    public String getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(String windowStart) {
        this.windowStart = windowStart;
    }

    public String getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(String windowEnd) {
        this.windowEnd = windowEnd;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MotionFrame getMotionFrame() {
        return motionFrame;
    }

    public void setMotionFrame(MotionFrame motionFrame) {
        this.motionFrame = motionFrame;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "MotionHappeningRecord{" +
                "recordId='" + recordId + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", frameId='" + frameId + '\'' +
                ", frameType='" + frameType + '\'' +
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", sequence='" + sequence + '\'' +
                ", status='" + status + '\'' +
                ", motionFrame=" + motionFrame +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}