package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

import java.time.Instant;

public class HistoricalViewWindow extends MotionBaseModel
{
    private long windowSeconds;
    private String from;
    private String to;

    public long getWindowSeconds()
    {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds)
    {
        this.windowSeconds = windowSeconds;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public Instant calculateWindow()
    {
        Instant now = Instant.now();

        this.to = now.toString();
        this.from = now.minusSeconds(this.windowSeconds).toString();

        return now;
    }

    @Override
    public String toString()
    {
        return "HistoricalViewWindow{" +
                "windowSeconds=" + windowSeconds +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
