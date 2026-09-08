package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import io.braineous.motion.core.model.EvolvingContext;
import io.braineous.motion.core.model.MotionContextWindow;
import io.braineous.motion.core.model.MotionFrame;
import io.braineous.motion.core.model.MotionTimeWindow;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * EvolvingContextResolver owns deterministic placement of a MotionFrame into
 * the EvolvingContext identified by canonical routing continuity and the
 * frame's absolute 24-hour context boundary. Its inputs are a canonical
 * MotionFrameRoutingKey, a MotionFrame, and optional current EvolvingContext;
 * its output is the matching context with deterministic, duplicate-free frame
 * membership.
 *
 * The resolver enforces globally anchored UTC periods of exactly 86,400
 * seconds and rejects malformed or cross-boundary mutation. It does not alter
 * MotionTimeWindow semantics and does not own wall-clock lifecycle execution,
 * persistence, recovery, replay, CGO, Insights, or Flink execution state.
 */
@ApplicationScoped
public class EvolvingContextResolver {

    private static final long CONTEXT_DURATION_MILLIS = 86_400_000L;

    public EvolvingContext place(
            MotionFrameRoutingKey routingKey,
            MotionFrame frame,
            EvolvingContext currentContext) {

        if (routingKey == null) {
            throw new IllegalArgumentException("routingKey cannot be null");
        }

        if (frame == null) {
            throw new IllegalArgumentException("frame cannot be null");
        }

        String routingKeyValue = requireValue(
                routingKey.getRoutingKey(),
                "routingKey cannot be null/blank");

        requireValue(
                frame.getFrameId(),
                "frameId cannot be null/blank");

        Instant placementInstant = placementInstant(frame);
        boundaryEndInstant(frame);
        MotionContextWindow contextWindow = contextWindow(placementInstant);
        String contextId = routingKeyValue
                + ":context:"
                + contextWindow.getContextStart();

        validateCurrentContext(
                currentContext,
                contextId,
                contextWindow);

        List<MotionFrame> frames = copyFrames(
                currentContext,
                contextWindow);

        if (!containsFrame(frames, frame)) {
            frames.add(frame);
        }

        sortFrames(frames);

        EvolvingContext resolvedContext = currentContext;

        if (resolvedContext == null) {
            resolvedContext = new EvolvingContext();
            resolvedContext.setContextId(contextId);
            resolvedContext.setSubjectId(routingKey.getSubjectId());
            resolvedContext.setSubjectType(routingKey.getSubjectType());
            resolvedContext.setContextWindow(contextWindow);
        }

        resolvedContext.setMotionFrames(frames);
        return resolvedContext;
    }

    private Instant placementInstant(MotionFrame frame) {

        if (frame == null) {
            throw new IllegalArgumentException("frame cannot be null");
        }

        MotionTimeWindow timeWindow = frame.getTimeWindow();

        if (timeWindow == null) {
            throw new IllegalArgumentException("frame timeWindow cannot be null");
        }

        String windowStart = requireValue(
                timeWindow.getWindowStart(),
                "frame windowStart cannot be null/blank");

        try {
            return Instant.parse(windowStart);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "frame windowStart must be a valid Instant",
                    exception);
        }
    }

    private MotionContextWindow contextWindow(Instant placementInstant) {

        long epochMillis = placementInstant.toEpochMilli();
        long contextStartMillis = Math.floorDiv(
                epochMillis,
                CONTEXT_DURATION_MILLIS) * CONTEXT_DURATION_MILLIS;
        long contextEndMillis = Math.addExact(
                contextStartMillis,
                CONTEXT_DURATION_MILLIS);

        MotionContextWindow contextWindow = new MotionContextWindow();
        contextWindow.setContextStart(
                Instant.ofEpochMilli(contextStartMillis).toString());
        contextWindow.setContextEnd(
                Instant.ofEpochMilli(contextEndMillis).toString());
        return contextWindow;
    }

    private Instant boundaryEndInstant(MotionFrame frame) {

        MotionTimeWindow timeWindow = frame.getTimeWindow();
        String windowEnd = requireValue(
                timeWindow.getWindowEnd(),
                "frame windowEnd cannot be null/blank");

        try {
            return Instant.parse(windowEnd);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "frame windowEnd must be a valid Instant",
                    exception);
        }
    }

    private void validateCurrentContext(
            EvolvingContext currentContext,
            String expectedContextId,
            MotionContextWindow expectedWindow) {

        if (currentContext == null) {
            return;
        }

        if (!expectedContextId.equals(currentContext.getContextId())) {
            throw new IllegalArgumentException(
                    "frame belongs to a different EvolvingContext");
        }

        MotionContextWindow currentWindow = currentContext.getContextWindow();

        if (currentWindow == null) {
            throw new IllegalArgumentException(
                    "current contextWindow cannot be null");
        }

        if (!expectedWindow.getContextStart().equals(currentWindow.getContextStart())) {
            throw new IllegalArgumentException(
                    "frame belongs to a different context boundary");
        }

        if (!expectedWindow.getContextEnd().equals(currentWindow.getContextEnd())) {
            throw new IllegalArgumentException(
                    "frame belongs to a different context boundary");
        }
    }

    private List<MotionFrame> copyFrames(
            EvolvingContext currentContext,
            MotionContextWindow expectedWindow) {

        List<MotionFrame> frames = new ArrayList<MotionFrame>();

        if (currentContext == null || currentContext.getMotionFrames() == null) {
            return frames;
        }

        for (MotionFrame existingFrame : currentContext.getMotionFrames()) {
            if (existingFrame == null) {
                throw new IllegalArgumentException(
                        "current context cannot contain a null frame");
            }

            requireValue(
                    existingFrame.getFrameId(),
                    "current context frameId cannot be null/blank");
            Instant existingPlacement = placementInstant(existingFrame);
            boundaryEndInstant(existingFrame);
            MotionContextWindow existingWindow = contextWindow(existingPlacement);

            if (!expectedWindow.getContextStart().equals(existingWindow.getContextStart())) {
                throw new IllegalArgumentException(
                        "current context contains a cross-boundary frame");
            }

            if (!expectedWindow.getContextEnd().equals(existingWindow.getContextEnd())) {
                throw new IllegalArgumentException(
                        "current context contains a cross-boundary frame");
            }

            frames.add(existingFrame);
        }

        return frames;
    }

    private boolean containsFrame(
            List<MotionFrame> frames,
            MotionFrame frame) {

        Instant frameStart = placementInstant(frame);
        Instant frameEnd = boundaryEndInstant(frame);

        for (MotionFrame existingFrame : frames) {
            Instant existingStart = placementInstant(existingFrame);
            Instant existingEnd = boundaryEndInstant(existingFrame);

            if (frameStart.equals(existingStart)
                    && frameEnd.equals(existingEnd)) {
                return true;
            }
        }

        return false;
    }

    private void sortFrames(List<MotionFrame> frames) {
        Collections.sort(
                frames,
                new Comparator<MotionFrame>() {
                    @Override
                    public int compare(
                            MotionFrame left,
                            MotionFrame right) {

                        Instant leftStart = placementInstant(left);
                        Instant rightStart = placementInstant(right);
                        int timeComparison = leftStart.compareTo(rightStart);

                        if (timeComparison != 0) {
                            return timeComparison;
                        }

                        return left.getFrameId().compareTo(right.getFrameId());
                    }
                });
    }

    private String requireValue(
            String value,
            String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return trimmed;
    }
}
