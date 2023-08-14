public class SawToothTiming implements Timing
{
    private final long period;
    private final long phase;
    public SawToothTiming(long period, long phase)
    {
        this.period = period;
        this.phase  = phase;
    }
    public double getTiming(long time)
    {
        return (double)((time + phase) % period) / (double)period;
    }
}
