public class LinearColor implements VariableColor
{
    private final Color  fromValue;
    private final Color  toValue;
    private final Timing timing;
    private double cachedTiming = -1.0;
    private Color  cachedColor;
    public LinearColor(Color fromValue, Color toValue, Timing timing)
    {
        this.fromValue = fromValue;
        this.toValue   = toValue;
        this.timing    = timing;
    }
    public Color getColor(long time)
    {
        double t = timing.getTiming(time);
        if (t != cachedTiming)
        {
            cachedTiming = t;
            cachedColor =
                t == 0.0 ? fromValue :
                t == 1.0 ? toValue   :
                           new Color((int)(fromValue.getRed()   + t * (toValue.getRed()   - fromValue.getRed())),
                                     (int)(fromValue.getGreen() + t * (toValue.getGreen() - fromValue.getGreen())),
                                     (int)(fromValue.getBlue()  + t * (toValue.getBlue()  - fromValue.getBlue())));
        }
        return cachedColor;
    }
}
