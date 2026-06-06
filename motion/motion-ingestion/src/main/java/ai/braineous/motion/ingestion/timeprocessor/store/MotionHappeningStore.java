package ai.braineous.motion.ingestion.timeprocessor.store;

import java.util.List;

/**
 * Storage boundary for active MotionFrame continuity.
 *
 * This interface defines the small persistence contract used by
 * MotionProcessor to resolve, recover, and continue active
 * operational evolution.
 *
 * Unlike MotionFrameStore, which preserves completed historical
 * MotionFrames, MotionHappeningStore manages MotionFrames that
 * are currently active and may continue accepting MotionEvents.
 *
 * MotionHappeningStore does not perform routing behavior,
 * window evaluation, append orchestration, frame lifecycle
 * decisions, frame closure, EvolvingContext assembly,
 * operational intelligence processing, or governance behavior.
 *
 * Its responsibility is intentionally simple:
 *
 * persist active frames,
 * resolve active frames,
 * recover active continuity,
 * allow operational evolution to continue.
 */
public interface MotionHappeningStore {

    public void addRecord(MotionHappeningRecord record);

    public MotionHappeningRecord findByRoutingKey(String routingKey);

    public MotionHappeningRecord findByFrameId(String frameId);

    public List<MotionHappeningRecord> findBySubject(
            String subjectId,
            String subjectType);

    public List<MotionHappeningRecord> getAll();

    public void clear();
}