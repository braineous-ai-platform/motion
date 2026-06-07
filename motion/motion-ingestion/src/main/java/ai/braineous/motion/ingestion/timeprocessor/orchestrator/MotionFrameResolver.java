package ai.braineous.motion.ingestion.timeprocessor.orchestrator;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningRecord;
import ai.braineous.motion.ingestion.timeprocessor.store.MotionHappeningStore;
import io.braineous.motion.core.model.MotionFrame;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MotionFrameResolver resolves the active MotionFrame associated with
 * a deterministic MotionFrameRoutingKey.
 *
 * The resolver represents the TimeProcessor boundary between routing
 * identity and active frame continuity. It uses MotionHappeningStore
 * to recover an existing active MotionFrame when one is already present
 * for the routing identity.
 *
 * When no active MotionFrame exists, the resolver creates a new
 * MotionFrame initialized for active runtime evolution.
 *
 * MotionFrameResolver does not append MotionEvents, evaluate temporal
 * windows, close frames, publish events, build processor responses,
 * assemble EvolvingContext, perform operational intelligence, or own
 * persistence implementation details.
 *
 * Its responsibility is intentionally narrow:
 *
 * resolve the MotionFrame that should receive runtime evolution.
 */
@ApplicationScoped
public class MotionFrameResolver {

    @Inject
    MotionHappeningStore happeningStore;

    public MotionFrame resolve(MotionFrameRoutingKey routingKey) {

        if (routingKey == null) {
            return null;
        }

        String routingKeyValue =
                safe(routingKey.getRoutingKey());

        if (routingKeyValue == null) {
            return null;
        }

        MotionHappeningRecord existingRecord =
                happeningStore.findByRoutingKey(routingKeyValue);

        if (existingRecord != null) {

            MotionFrame existingFrame =
                    existingRecord.getMotionFrame();

            if (existingFrame != null) {
                return existingFrame;
            }
        }

        return newFrame(routingKey);
    }

    private MotionFrame newFrame(MotionFrameRoutingKey routingKey) {

        MotionFrame frame =
                new MotionFrame();

        frame.setFrameId(
                routingKey.getRoutingKey() + ":frame");

        frame.setFrameType(
                routingKey.getFrameType());

        frame.setSequence("1");
        frame.setStatus("OPEN");
        frame.setMetadataJson("{\"runtime\":\"motion\"}");

        return frame;
    }

    private String safe(String value) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}