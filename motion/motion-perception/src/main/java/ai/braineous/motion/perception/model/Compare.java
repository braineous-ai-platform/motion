package ai.braineous.motion.perception.model;

import io.braineous.motion.core.model.MotionBaseModel;

public class Compare extends MotionBaseModel
{
    private Perception left;
    private HistoricalView right;

    public Perception getLeft()
    {
        return left;
    }

    public void setLeft(Perception left)
    {
        this.left = left;
    }

    public HistoricalView getRight()
    {
        return right;
    }

    public void setRight(HistoricalView right)
    {
        this.right = right;
    }

    @Override
    public String toString()
    {
        return "Compare{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
