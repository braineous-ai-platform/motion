package ai.braineous.motion.ingestion.sinkprocessor;

import ai.braineous.motion.ingestion.sinkprocessor.model.OperationalView;
import io.braineous.motion.core.model.MotionEvent;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationalViewMaterializer {

    public OperationalView materialize(MotionFrame motionFrame) {

        if (motionFrame == null) {
            return null;
        }

        OperationalView operationalView =
                new OperationalView();

        operationalView.setViewId(
                motionFrame.getFrameId());

        MotionTimeWindow timeWindow =
                motionFrame.getTimeWindow();

        if (timeWindow != null) {
            operationalView.setWindowStart(
                    timeWindow.getWindowStart());

            operationalView.setWindowEnd(
                    timeWindow.getWindowEnd());
        }

        Map<String, Object> state =
                new LinkedHashMap<String, Object>();

        state.put("frameType", motionFrame.getFrameType());
        state.put("sequence", motionFrame.getSequence());
        state.put("status", motionFrame.getStatus());
        state.put("metadataJson", motionFrame.getMetadataJson());

        operationalView.setState(state);

        List<String> sourceFrameIds =
                new ArrayList<String>();

        if (motionFrame.getFrameId() != null) {
            sourceFrameIds.add(motionFrame.getFrameId());
        }

        operationalView.setSourceFrameIds(sourceFrameIds);

        List<String> sourceEventIds =
                new ArrayList<String>();

        List<MotionEvent> motionEvents =
                motionFrame.getMotionEvents();

        if (motionEvents != null) {
            for (MotionEvent motionEvent : motionEvents) {
                if (motionEvent != null
                        && motionEvent.getEventId() != null) {
                    sourceEventIds.add(
                            motionEvent.getEventId());
                }
            }
        }

        operationalView.setSourceEventIds(sourceEventIds);
        operationalView.setMaterializedAt(
                Instant.now().toString());

        return operationalView;
    }
}
