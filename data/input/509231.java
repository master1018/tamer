public class SmoothTiming implements Timing
{
    private final long fromTime;
    private final long toTime;
    public SmoothTiming(long fromTime, long toTime)
    {
        this.fromTime = fromTime;
        this.toTime   = toTime;
    }
    public double getTiming(long time)
    {
        if (time <= fromTime)
        {
            return 0.0;
        }
        if (time >= toTime)
        {
            return 1.0;
        }
        double timing = (double) (time - fromTime) / (double) (toTime - fromTime);
        return timing * timing * (3.0 - 2.0 * timing);
    }
}
