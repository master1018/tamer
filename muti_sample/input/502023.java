public class SineTiming implements Timing
{
    private final long period;
    private final long phase;
    public SineTiming(long period, long phase)
    {
        this.period = period;
        this.phase  = phase;
    }
    public double getTiming(long time)
    {
        return 0.5 + 0.5 * Math.sin(2.0 * Math.PI * (time + phase) / period);
    }
}
