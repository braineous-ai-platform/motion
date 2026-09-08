package io.braineous.motion.core.model;

/**
 * MotionContextWindow is Motion's domain representation of the absolute
 * 24-hour boundary owned by an EvolvingContext. Its input is an absolute
 * placement instant and its output values identify one globally anchored,
 * contiguous, non-overlapping, half-open context period.
 *
 * MotionContextWindow does not represent a MotionFrame boundary and does not
 * own timestamp parsing, placement calculation, lifecycle execution,
 * persistence, recovery, or Flink behavior.
 */
public class MotionContextWindow extends MotionBaseModel {

    private String contextStart;
    private String contextEnd;

    public MotionContextWindow() {
    }

    public String getContextStart() {
        return contextStart;
    }

    public void setContextStart(String contextStart) {
        this.contextStart = contextStart;
    }

    public String getContextEnd() {
        return contextEnd;
    }

    public void setContextEnd(String contextEnd) {
        this.contextEnd = contextEnd;
    }

    @Override
    public String toString() {
        return "MotionContextWindow{" +
                "contextStart='" + contextStart + '\'' +
                ", contextEnd='" + contextEnd + '\'' +
                '}';
    }
}
