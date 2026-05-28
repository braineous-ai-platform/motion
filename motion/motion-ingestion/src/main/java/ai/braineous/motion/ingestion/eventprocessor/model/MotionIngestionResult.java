package ai.braineous.motion.ingestion.eventprocessor.model;

/**
 * MotionIngestionResult represents the deterministic outcome produced
 * by the Motion ingestion pipeline after validation and routing decisions.
 *
 * <p>
 * This object communicates whether an inbound event was:
 * </p>
 *
 * <ul>
 *     <li>Accepted</li>
 *     <li>Rejected</li>
 *     <li>Quarantined</li>
 *     <li>Marked replayable</li>
 * </ul>
 *
 * <p>
 * MotionIngestionResult exists primarily for:
 * </p>
 *
 * <ul>
 *     <li>Pipeline observability</li>
 *     <li>Operational debugging</li>
 *     <li>Replay coordination</li>
 *     <li>Failure traceability</li>
 *     <li>Deterministic ingestion feedback</li>
 * </ul>
 *
 * <p>
 * The ingestion result intentionally avoids business interpretation.
 * It communicates ingestion state only.
 * </p>
 *
 * <p>
 * Typical ingestion outcome metadata may include:
 * </p>
 *
 * <ul>
 *     <li>Reason codes</li>
 *     <li>Correlation identifiers</li>
 *     <li>Motion event identifiers</li>
 *     <li>Replay eligibility</li>
 *     <li>Pipeline stage outcomes</li>
 * </ul>
 *
 * <p>
 * MotionIngestionResult is designed to remain stable across:
 * </p>
 *
 * <ul>
 *     <li>Kafka infrastructure changes</li>
 *     <li>Flink processing evolution</li>
 *     <li>CGO/intelligence evolution</li>
 * </ul>
 *
 * <p>
 * It represents ingestion truth only.
 * </p>
 */
public class MotionIngestionResult {
}