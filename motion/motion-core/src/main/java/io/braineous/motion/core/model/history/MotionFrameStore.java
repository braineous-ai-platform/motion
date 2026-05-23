package io.braineous.motion.core.model.history;

import java.util.List;

/**
 * Storage boundary for MotionFrameRecord history.
 *
 * This interface defines the small persistence contract for bounded
 * operational evolution windows.
 *
 * It does not model governance lifecycle, scoring, approval, or query-level
 * interpretation.
 *
 * Motion history storage is intentionally simple:
 * append frames,
 * find frames,
 * reconstruct continuity later.
 */
public interface MotionFrameStore {

    public void addRecord(MotionFrameRecord record);

    public MotionFrameRecord findByFrameId(String frameId);

    public List<MotionFrameRecord> findBySubject(String subjectId, String subjectType);

    public List<MotionFrameRecord> findByContextId(String contextId);

    public List<MotionFrameRecord> getAll();

    public void clear();
}