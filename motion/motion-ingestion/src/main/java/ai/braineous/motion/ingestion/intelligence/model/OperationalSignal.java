package ai.braineous.motion.ingestion.intelligence.model;

import io.braineous.motion.core.model.MotionBaseModel;

/**
 * OperationalSignal represents interpreted operational evolution
 * produced by the Motion intelligence layer from accumulated motion
 * over time.
 *
 * OperationalSignal is emitted after Motion has accumulated operational
 * movement through MotionEvents, MotionFrames, and EvolvingContext.
 *
 * Unlike MotionEvent, which represents observed motion, and
 * EvolvingContext, which represents accumulated motion,
 * OperationalSignal represents meaningful motion detected by the
 * runtime.
 *
 * This model intentionally remains domain-neutral, infrastructure-
 * agnostic, and CGO-agnostic. It does not model facts, relationships,
 * graphs, policies, decisions, predictions, or LLM execution.
 *
 * Its responsibility is to communicate that operational evolution has
 * produced a signal that may be consumed later by downstream
 * intelligence, governance, decisioning, replay, or observability
 * layers.
 *
 * OperationalSignal represents the first intelligence-layer model in
 * Motion:
 *
 * MotionEvent
 *     -> MotionFrame
 *     -> EvolvingContext
 *     -> OperationalSignal
 */
public class OperationalSignal extends MotionBaseModel {

    private String signalId;
    private String signalType;
    private String signalLevel;

    private String status;
    private String code;
    private String reason;
    private String message;

    private String contextId;
    private String contextType;
    private String subjectId;
    private String subjectType;

    private String windowStart;
    private String windowEnd;
    private String detectedAt;

    private String evidenceJson;
    private String metadataJson;

    public OperationalSignal() {
    }

    public String getSignalId() {
        return signalId;
    }

    public void setSignalId(String signalId) {
        this.signalId = signalId;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public String getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(String signalLevel) {
        this.signalLevel = signalLevel;
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

    public String setReason(String reason) {
        this.reason = reason;
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(String detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getEvidenceJson() {
        return evidenceJson;
    }

    public void setEvidenceJson(String evidenceJson) {
        this.evidenceJson = evidenceJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    @Override
    public String toString() {
        return "OperationalSignal{" +
                "signalId='" + signalId + '\'' +
                ", signalType='" + signalType + '\'' +
                ", signalLevel='" + signalLevel + '\'' +
                ", status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                ", contextId='" + contextId + '\'' +
                ", contextType='" + contextType + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType='" + subjectType + '\'' +
                ", windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                ", detectedAt='" + detectedAt + '\'' +
                ", evidenceJson='" + evidenceJson + '\'' +
                ", metadataJson='" + metadataJson + '\'' +
                '}';
    }
}