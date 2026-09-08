package io.braineous.motion.core.model;

/**
 * MotionFrame is Motion's bounded temporal domain frame for one
 * operational continuity. Its temporal boundary is represented by
 * MotionTimeWindow.
 *
 * A MotionFrame contains a collection of MotionEvents that together
 * describe meaningful operational movement over time rather than
 * isolated operational activity.
 *
 * Within the Motion runtime, MotionFrames serve as the primary
 * temporal structure through which operational progression is
 * organized, accumulated, and evolved into higher-order runtime
 * context.
 *
 * A MotionFrame owns its accumulated MotionEvents and lifecycle/model
 * state. It does not define Flink execution mechanics. MotionTimeWindow
 * represents the boundary; execution policy is defined separately.
 *
 * MotionFrames accumulate into EvolvingContext, allowing intelligent
 * systems to reason against operational evolution rather than
 * disconnected events alone.
 */
public class MotionFrame extends MotionBaseModel {

    private String frameId;
    private String frameType;
    private MotionTimeWindow timeWindow;
    private String sequence;
    private String status;
    private java.util.List<MotionEvent> motionEvents;
    private String metadataJson;

    public MotionFrame() {
        this.motionEvents = new java.util.ArrayList<MotionEvent>();
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

    public MotionTimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(MotionTimeWindow timeWindow) {
        this.timeWindow = timeWindow;
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

    public java.util.List<MotionEvent> getMotionEvents() {
        return motionEvents;
    }

    public void setMotionEvents(java.util.List<MotionEvent> motionEvents) {
        this.motionEvents = motionEvents;
    }

    public void addMotionEvent(MotionEvent motionEvent) {
        if (this.motionEvents == null) {
            this.motionEvents = new java.util.ArrayList<MotionEvent>();
        }
        if (motionEvent != null) {
            this.motionEvents.add(motionEvent);
        }
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "MotionFrame{" +
                "frameId='" + frameId + '\'' +
                ", frameType='" + frameType + '\'' +
                ", timeWindow=" + timeWindow +
                ", sequence='" + sequence + '\'' +
                ", status='" + status + '\'' +
                ", motionEvents=" + motionEvents +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}
