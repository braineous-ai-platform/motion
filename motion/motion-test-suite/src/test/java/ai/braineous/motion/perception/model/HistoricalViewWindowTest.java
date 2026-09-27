package ai.braineous.motion.perception.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class HistoricalViewWindowTest {

    @Test
    public void test_1() {
        HistoricalViewWindow historicalViewWindow = new HistoricalViewWindow();
        historicalViewWindow.setWindowSeconds(3600L);

        Instant anchor = historicalViewWindow.calculateWindow();

        Assertions.assertEquals(anchor.toString(), historicalViewWindow.getTo());
        Assertions.assertEquals(
                anchor.minusSeconds(3600L).toString(),
                historicalViewWindow.getFrom());
    }

    @Test
    public void test_2() {
        HistoricalViewWindow historicalViewWindow = new HistoricalViewWindow();
        historicalViewWindow.setWindowSeconds(60L);

        Instant anchor = historicalViewWindow.calculateWindow();
        Instant parsedFrom = Instant.parse(historicalViewWindow.getFrom());
        Instant parsedTo = Instant.parse(historicalViewWindow.getTo());

        Assertions.assertEquals(
                60L,
                Math.abs(Duration.between(parsedFrom, parsedTo).getSeconds()));
        Assertions.assertEquals(anchor, parsedTo);
    }

    @Test
    public void test_3() {
        HistoricalViewWindow historicalViewWindow = new HistoricalViewWindow();

        Assertions.assertEquals(0L, historicalViewWindow.getWindowSeconds());
        Assertions.assertNull(historicalViewWindow.getFrom());
        Assertions.assertNull(historicalViewWindow.getTo());

        historicalViewWindow.setWindowSeconds(300L);

        Assertions.assertEquals(300L, historicalViewWindow.getWindowSeconds());
    }
}
