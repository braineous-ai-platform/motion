package io.braineous.motion.core.model;

/**
 * MotionTimeWindow is Motion's domain representation of the temporal
 * boundary associated with a MotionFrame. It carries the frame's start
 * and end boundary values and provides the temporal-boundary primitive
 * that MotionFrame owns and infrastructure may later execute against.
 *
 * Temporal boundary identity belongs to Motion domain semantics rather
 * than Flink or another execution substrate. MotionTimeWindow does not
 * calculate boundaries, parse timestamps, define duration, or define
 * event-time semantics. It does not own lateness, ordering, replay,
 * lifecycle, persistence, or Flink execution.
 */
public class MotionTimeWindow extends MotionBaseModel {

    private String windowStart;
    private String windowEnd;

    public MotionTimeWindow() {
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

    @Override
    public String toString() {
        return "MotionTimeWindow{" +
                "windowStart='" + windowStart + '\'' +
                ", windowEnd='" + windowEnd + '\'' +
                '}';
    }
}
