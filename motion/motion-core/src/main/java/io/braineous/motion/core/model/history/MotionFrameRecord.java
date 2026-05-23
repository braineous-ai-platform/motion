package io.braineous.motion.core.model.history;

import io.braineous.motion.core.model.MotionBaseModel;
import io.braineous.motion.core.model.MotionFrame;

import java.time.Instant;

/**
 * Atomic persistence boundary for Motion history.
 *
 * A MotionFrameRecord stores one bounded slice of operational evolution.
 *
 * MotionFrame remains the semantic payload.
 * The duplicated scalar fields are storage/query helpers so Mongo can find
 * frames without unpacking the full payload every time.
 *
 * This record does not own governance lifecycle, approval state, scoring,
 * or EvolvingContext assembly.
 *
 * It simply preserves one time-windowed MotionFrame so continuity can be
 * reconstructed later with boring, deterministic storage semantics.
 */
public class MotionFrameRecord extends MotionBaseModel {

    private String recordId;
    private String contextId;
    private String contextType;
    private String subjectId;
    private String subjectType;

    private String frameId;
    private String frameType;
    private String windowStart;
    private String windowEnd;
    private String sequence;

    private MotionFrame motionFrame;

    private Instant createdAt;
    private Instant updatedAt;

    public MotionFrameRecord() {
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
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

    public MotionFrame getMotionFrame() {
        return motionFrame;
    }

    public void setMotionFrame(MotionFrame motionFrame) {
        this.motionFrame = motionFrame;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "MotionFrameRecord{" +
                "recordId='" + recordId + '\'' +
                ", contextId='" + contextId + '\'' +
                ", contextType='" + contextType + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", frameId='" + frameId + '\'' +
                ", frameType='" + frameType + '\'' +
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", sequence='" + sequence + '\'' +
                ", motionFrame=" + motionFrame +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}