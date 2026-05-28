package ai.braineous.motion.ingestion.eventprocessor.model;

/**
 * MotionReplaySignal represents the ingestion pipeline's replay guidance
 * for an inbound event or failed ingestion outcome.
 *
 * <p>
 * This object does not perform replay. It only describes whether replay
 * is safe, where replay may begin, and why replay is or is not allowed.
 * </p>
 *
 * <p>
 * MotionReplaySignal exists to prevent blind retries. Some failures are
 * temporary and replayable, while others are poison inputs that should be
 * rejected or quarantined instead of reprocessed repeatedly.
 * </p>
 *
 * <p>
 * Typical replay signal responsibilities include:
 * </p>
 *
 * <ul>
 *     <li>Identifying replay eligibility</li>
 *     <li>Identifying the safe replay stage</li>
 *     <li>Capturing replay reason codes</li>
 *     <li>Separating retryable failures from poison events</li>
 *     <li>Supporting deterministic operational recovery</li>
 * </ul>
 *
 * <p>
 * MotionReplaySignal intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Kafka retry implementation</li>
 *     <li>Replay scheduling</li>
 *     <li>Business correction logic</li>
 *     <li>Semantic re-interpretation</li>
 *     <li>Policy decisions</li>
 * </ul>
 *
 * <p>
 * Replay behavior is executed later by infrastructure-specific adapters
 * and operational tooling. This object only carries replay intent.
 * </p>
 */
public class MotionReplaySignal {
}