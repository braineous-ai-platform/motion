package ai.braineous.motion.ingestion.timeprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * MotionProcessorResult represents the public execution outcome
 * produced by MotionProcessor after applying an incoming MotionEvent
 * to the Motion runtime.
 *
 * MotionProcessorResult serves as the stable public contract exposed
 * by the TimeProcessor boundary and communicates the resulting
 * MotionFrame evolution outcome without exposing internal runtime
 * implementation details.
 *
 * This model does not perform routing, window evaluation, frame
 * lifecycle management, append behavior, persistence behavior,
 * or operational intelligence processing.
 *
 * Its responsibility is to provide a deterministic representation
 * of MotionProcessor execution suitable for observability, replay,
 * transport, and future runtime integrations.
 *
 * MotionProcessorResult represents the completion of the
 * MotionEvent → MotionFrame processing flow.
 */
public class MotionProcessorResult extends MotionBaseModel {

    private String resultId;
    private String status;
    private String code;
    private String reason;
    private String appendResultJson;
    private String motionFrameJson;
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

    public String getAppendResultJson() {
        return appendResultJson;
    }

    public void setAppendResultJson(String appendResultJson) {
        this.appendResultJson = appendResultJson;
    }

    public String getMotionFrameJson() {
        return motionFrameJson;
    }

    public void setMotionFrameJson(String motionFrameJson) {
        this.motionFrameJson = motionFrameJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "MotionProcessorResult{" +
                "resultId='" + resultId + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", reason='" + reason + '\'' +
                ", appendResultJson='" + appendResultJson + '\'' +
                ", motionFrameJson='" + motionFrameJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}