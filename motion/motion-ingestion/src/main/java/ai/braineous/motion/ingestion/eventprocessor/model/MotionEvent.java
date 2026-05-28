package ai.braineous.motion.ingestion.eventprocessor.model;

/**
 * MotionEvent represents the normalized motion primitive produced by
 * the Motion ingestion pipeline after wrapping and normalization.
 *
 * <p>
 * Unlike RawEvent, which preserves source-specific structure,
 * MotionEvent establishes a stable and domain-neutral representation
 * of observed motion within the system.
 * </p>
 *
 * <p>
 * MotionEvent exists to create a clean transition point between:
 * </p>
 *
 * <ul>
 *     <li>External producer chaos</li>
 *     <li>Deterministic stream processing</li>
 *     <li>Time/window analysis</li>
 *     <li>Graph/context intelligence</li>
 * </ul>
 *
 * <p>
 * MotionEvent typically captures normalized motion semantics such as:
 * </p>
 *
 * <ul>
 *     <li>Actor</li>
 *     <li>Action</li>
 *     <li>Target</li>
 *     <li>Time</li>
 *     <li>Context/location</li>
 *     <li>Attributes</li>
 *     <li>Quality/confidence indicators</li>
 * </ul>
 *
 * <p>
 * MotionEvent intentionally avoids:
 * </p>
 *
 * <ul>
 *     <li>Business conclusions</li>
 *     <li>AI reasoning</li>
 *     <li>Graph enrichment</li>
 *     <li>Policy decisions</li>
 *     <li>Temporal aggregation</li>
 * </ul>
 *
 * <p>
 * Those responsibilities belong to later Motion layers:
 * </p>
 *
 * <pre>
 * Kafka ingestion layer
 *     -> clean motion primitives
 *
 * Flink time layer
 *     -> temporal/stateful interpretation
 *
 * Intelligence layer
 *     -> graph/context meaning
 * </pre>
 *
 * <p>
 * MotionEvent is the canonical normalized event representation
 * flowing through the Motion runtime.
 * </p>
 */
public class MotionEvent {
}