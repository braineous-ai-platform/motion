package ai.braineous.motion.ingestion.sinkprocessor.model;

import io.braineous.motion.core.model.MotionBaseModel;

import java.util.List;
import java.util.Map;

/**
 * OperationalView is a deterministic materialized projection of Motion
 * temporal reality shaped for interrogation rather than runtime recovery.
 */
public class OperationalView extends MotionBaseModel {

    private String viewId;
    private String routingKey;
    private String contextId;
    private String windowStart;
    private String windowEnd;
    private Map<String, Object> state;
    private List<String> sourceFrameIds;
    private List<String> sourceEventIds;
    private String materializedAt;

    public OperationalView() {
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
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

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }

    public List<String> getSourceFrameIds() {
        return sourceFrameIds;
    }

    public void setSourceFrameIds(List<String> sourceFrameIds) {
        this.sourceFrameIds = sourceFrameIds;
    }

    public List<String> getSourceEventIds() {
        return sourceEventIds;
    }

    public void setSourceEventIds(List<String> sourceEventIds) {
        this.sourceEventIds = sourceEventIds;
    }

    public String getMaterializedAt() {
        return materializedAt;
    }

    public void setMaterializedAt(String materializedAt) {
        this.materializedAt = materializedAt;
    }

    @Override
    public String toString() {
        return "OperationalView{" +
                "viewId='" + viewId + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", contextId='" + contextId + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", state=" + state +
                ", sourceFrameIds=" + sourceFrameIds +
                ", sourceEventIds=" + sourceEventIds +
                ", materializedAt=" + materializedAt +
                '}';
    }
}
