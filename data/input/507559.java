public class ConstantTiming implements Timing
{
    private final double timing;
    public ConstantTiming()
    {
        this(0.0);
    }
    public ConstantTiming(double timing)
    {
        this.timing = timing;
    }
    public double getTiming(long time)
    {
        return timing;
    }
}
