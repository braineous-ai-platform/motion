package io.braineous.motion.core.model;

import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MotionTimeWindowTest {

    @Test
    public void test_1() {
        Console.log("timeWindow", "create MotionTimeWindow");

        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-05-22T10:00:00Z");
        timeWindow.setWindowEnd("2026-05-22T10:05:00Z");

        Console.log("timeWindow", timeWindow.toString());

        assertEquals("2026-05-22T10:00:00Z", timeWindow.getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", timeWindow.getWindowEnd());
    }

    @Test
    public void test_2() {
        MotionTimeWindow timeWindow = new MotionTimeWindow();
        timeWindow.setWindowStart("2026-05-22T10:00:00Z");
        timeWindow.setWindowEnd("2026-05-22T10:05:00Z");

        Console.log("timeWindow", "serialize MotionTimeWindow");
        String json = timeWindow.toJson();
        MotionTimeWindow restored = MotionTimeWindow.fromJson(json, MotionTimeWindow.class);
        Console.log("timeWindowJson", json);

        assertNotNull(restored);
        assertEquals("2026-05-22T10:00:00Z", restored.getWindowStart());
        assertEquals("2026-05-22T10:05:00Z", restored.getWindowEnd());
    }

    @Test
    public void test_3() {
        MotionTimeWindow timeWindow = new MotionTimeWindow();

        Console.log("timeWindow", "serialize null boundaries");
        String json = timeWindow.toJson();
        MotionTimeWindow restored = MotionTimeWindow.fromJson(json, MotionTimeWindow.class);
        Console.log("timeWindowJson", json);

        assertNotNull(restored);
        assertNull(restored.getWindowStart());
        assertNull(restored.getWindowEnd());
    }
}
