public class LinearInt implements VariableInt
{
    private final int    fromValue;
    private final int    toValue;
    private final Timing timing;
    public LinearInt(int fromValue, int toValue, Timing timing)
    {
        this.fromValue = fromValue;
        this.toValue   = toValue;
        this.timing    = timing;
    }
    public int getInt(long time)
    {
        return (int) (fromValue + timing.getTiming(time) * (toValue - fromValue));
    }
}
