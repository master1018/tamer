public class TimeSwitchSprite implements Sprite
{
    private final long   onTime;
    private final long offTime;
    private final Sprite sprite;
    public TimeSwitchSprite(long onTime, Sprite sprite)
    {
        this(onTime, 0L, sprite);
    }
    public TimeSwitchSprite(long onTime, long offTime, Sprite sprite)
    {
        this.onTime  = onTime;
        this.offTime = offTime;
        this.sprite  = sprite;
    }
    public void paint(Graphics graphics, long time)
    {
        if (time >= onTime && (offTime <= 0 || time <= offTime))
        {
            sprite.paint(graphics, time - onTime);
        }
    }
}
