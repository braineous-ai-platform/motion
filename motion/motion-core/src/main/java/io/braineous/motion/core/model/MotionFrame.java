package io.braineous.motion.core.model;

/**
 * MotionFrame represents a bounded unit of operational evolution
 * accumulated across a deterministic temporal runtime window.
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
 * The temporal boundary of a MotionFrame is controlled internally
 * by the runtime to preserve deterministic operational semantics
 * across evolving event-native systems.
 *
 * MotionFrames accumulate into EvolvingContext, allowing intelligent
 * systems to reason against operational evolution rather than
 * disconnected events alone.
 */
public class MotionFrame extends MotionBaseModel {

    private String frameId;
    private String frameType;
    private String windowStart;
    private String windowEnd;
    private String sequence;
    private String status;
    private String eventsJson;
    private String metadataJson;

    public MotionFrame() {
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

    public String getEventsJson() {
        return eventsJson;
    }

    public void setEventsJson(String eventsJson) {
        this.eventsJson = eventsJson;
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
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", sequence='" + sequence + '\'' +
                ", status='" + status + '\'' +
                ", eventsJson='" + eventsJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}