package ai.braineous.motion.ingestion.timeprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;
import io.braineous.motion.core.model.MotionFrame;

/**
 * MotionFrameAppendResult represents the deterministic outcome
 * of applying an incoming MotionEvent to a resolved MotionFrame.
 *
 * Append results allow the Motion runtime to distinguish whether
 * a MotionFrame was newly created, reused, updated, or rejected
 * during TimeProcessor execution.
 *
 * This model does not perform append behavior, persistence behavior,
 * routing, window evaluation, or frame lifecycle decisions.
 *
 * Its responsibility is to carry the result of MotionFrame append
 * orchestration in a stable structure that can be inspected by
 * MotionProcessor and tested independently.
 *
 * MotionProcessor uses MotionFrameAppendResult to continue the
 * deterministic flow from incoming MotionEvent to persisted
 * MotionFrame.
 */
public class MotionFrameAppendResult extends MotionBaseModel {

    private String resultId;
    private String status;
    private String code;
    private String reason;
    private String frameCreated;
    private String frameReused;
    private MotionFrame motionFrame;
    private String metadataJson;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFrameCreated() {
        return frameCreated;
    }

    public void setFrameCreated(String frameCreated) {
        this.frameCreated = frameCreated;
    }

    public String getFrameReused() {
        return frameReused;
    }

    public void setFrameReused(String frameReused) {
        this.frameReused = frameReused;
    }

    public MotionFrame getMotionFrame() {
        return motionFrame;
    }

    public void setMotionFrame(MotionFrame motionFrame) {
        this.motionFrame = motionFrame;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "MotionFrameAppendResult{" +
                "resultId='" + resultId + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", reason='" + reason + '\'' +
                ", frameCreated='" + frameCreated + '\'' +
                ", frameReused='" + frameReused + '\'' +
                ", motionFrame=" + motionFrame +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}
