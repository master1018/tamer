public class LinearTiming implements Timing
{
    private final long fromTime;
    private final long toTime;
    public LinearTiming(long fromTime, long toTime)
    {
        this.fromTime = fromTime;
        this.toTime   = toTime;
    }
    public double getTiming(long time)
    {
        return time <= fromTime ? 0.0 :
               time >= toTime   ? 1.0 :
                                  (double)(time - fromTime) / (double)(toTime - fromTime);
    }
}
