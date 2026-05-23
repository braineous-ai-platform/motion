package io.braineous.motion.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * EvolvingContext represents the accumulated operational evolution
 * of a system across ordered MotionFrames over time.
 *
 * Unlike static operational state, EvolvingContext continuously
 * evolves as new MotionFrames are introduced into the runtime,
 * allowing intelligent systems to reason against operational
 * progression, temporal relationships, and changing runtime
 * conditions.
 *
 * Within Motion, EvolvingContext acts as the temporal operational
 * substrate from which higher-order runtime reasoning and governed
 * execution workflows can emerge.
 *
 * MotionFrames provide bounded operational evolution units while
 * EvolvingContext provides continuity of operational movement
 * across time.
 *
 * This allows intelligent systems to reason not only about what
 * operational state currently is, but also how operational state
 * has evolved over time.
 */
public class EvolvingContext extends MotionBaseModel {

    private String contextId;
    private String contextType;
    private String subjectId;
    private String subjectType;
    private String status;
    private List<MotionFrame> motionFrames;
    private String metadataJson;

    public EvolvingContext() {
        this.motionFrames = new ArrayList<MotionFrame>();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MotionFrame> getMotionFrames() {
        return motionFrames;
    }

    public void setMotionFrames(List<MotionFrame> motionFrames) {
        this.motionFrames = motionFrames;
    }

    public void addMotionFrame(MotionFrame motionFrame) {
        this.motionFrames.add(motionFrame);
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "EvolvingContext{" +
                "contextId='" + contextId + '\'' +
                ", contextType='" + contextType + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", status='" + status + '\'' +
                ", motionFrames=" + motionFrames +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}