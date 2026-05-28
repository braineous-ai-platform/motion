package ai.braineous.motion.ingestion.eventprocessor.model;

/**
 * RawEvent represents the original inbound event entering the Motion ingestion pipeline.
 *
 * <p>
 * This object intentionally preserves the incoming event in its original shape
 * before normalization, validation, partition resolution, or routing occurs.
 * The source payload may be incomplete, unordered, duplicated, noisy,
 * infrastructure-specific, or domain-specific.
 * </p>
 *
 * <p>
 * RawEvent exists as the boundary object between external producer systems
 * and the deterministic Motion ingestion pipeline.
 * </p>
 *
 * <p>
 * Responsibilities intentionally NOT handled by RawEvent:
 * </p>
 *
 * <ul>
 *     <li>Semantic interpretation</li>
 *     <li>Domain intelligence</li>
 *     <li>Graph/context enrichment</li>
 *     <li>Time/window processing</li>
 *     <li>Replay decisions</li>
 *     <li>Kafka/Flink infrastructure behavior</li>
 * </ul>
 *
 * <p>
 * The ingestion pipeline later transforms:
 * </p>
 *
 * <pre>
 * RawEvent
 *     -> MotionEnvelope
 *     -> MotionEvent
 * </pre>
 *
 * <p>
 * RawEvent is intentionally infrastructure-agnostic and domain-neutral.
 * </p>
 */
public class RawEvent {
}
