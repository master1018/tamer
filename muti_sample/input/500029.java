public class LinearDouble implements VariableDouble
{
    private final double fromValue;
    private final double toValue;
    private final Timing timing;
    public LinearDouble(double fromValue, double toValue, Timing timing)
    {
        this.fromValue = fromValue;
        this.toValue   = toValue;
        this.timing    = timing;
    }
    public double getDouble(long time)
    {
        return fromValue + timing.getTiming(time) * (toValue - fromValue);
    }
}
