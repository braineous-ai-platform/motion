package ai.braineous.motion.ingestion.timeprocessor.infra.flink;

import ai.braineous.motion.ingestion.timeprocessor.model.MotionFrameRoutingKey;
import ai.braineous.motion.ingestion.timeprocessor.orchestrator.MotionFrameRoutingKeyResolver;
import io.braineous.motion.core.model.MotionEvent;
import org.apache.flink.api.java.functions.KeySelector;

/**
 * Adapts Motion's canonical frame routing identity to Flink's keyed
 * execution model.
 *
 * <p>Routing semantics remain owned by
 * {@link MotionFrameRoutingKeyResolver}. This component resolves the
 * {@link MotionFrameRoutingKey} for a canonical {@link MotionEvent}
 * and exposes its routing-key value as the Flink partition key.</p>
 *
 * <p>This component does not define routing semantics, windowing,
 * temporal state, persistence, or downstream resolution.</p>
 */
public class MotionFramePartitionKeySelector
        implements KeySelector<MotionEvent, String> {

    private static final long serialVersionUID = 1L;

    private final MotionFrameRoutingKeyResolver routingKeyResolver;

    public MotionFramePartitionKeySelector(
            MotionFrameRoutingKeyResolver routingKeyResolver) {

        this.routingKeyResolver = routingKeyResolver;
    }

    @Override
    public String getKey(MotionEvent motionEvent) throws Exception {

        MotionFrameRoutingKey routingKey =
                this.routingKeyResolver.resolve(motionEvent);

        if (routingKey == null) {
            return null;
        }

        return routingKey.getRoutingKey();
    }
}
