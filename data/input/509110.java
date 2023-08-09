public class ShadowedSprite implements Sprite
{
    private final VariableInt    xOffset;
    private final VariableInt    yOffset;
    private final VariableDouble alpha;
    private final VariableInt    blur;
    private final Sprite         sprite;
    private float cachedAlpha = -1.0f;
    private Color cachedColor;
    public ShadowedSprite(VariableInt    xOffset,
                          VariableInt    yOffset,
                          VariableDouble alpha,
                          VariableInt    blur,
                          Sprite         sprite)
    {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.alpha   = alpha;
        this.blur    = blur;
        this.sprite  = sprite;
    }
    public void paint(Graphics graphics, long time)
    {
        double l = alpha.getDouble(time);
        int    b = blur.getInt(time) + 1;
        float a = 1.0f - (float)Math.pow(1.0 - l, 1.0/(b*b));
        if (a != cachedAlpha)
        {
            cachedAlpha = a;
            cachedColor = new Color(0f, 0f, 0f, a);
        }
        Color actualColor = graphics.getColor();
        graphics.setColor(cachedColor);
        int xo = xOffset.getInt(time) - b/2;
        int yo = yOffset.getInt(time) - b/2;
        for (int x = 0; x < b; x++)
        {
            for (int y = 0; y < b; y++)
            {
                int xt = xo + x;
                int yt = yo + y;
                graphics.translate(xt, yt);
                sprite.paint(graphics, time);
                graphics.translate(-xt, -yt);
            }
        }
        graphics.setColor(actualColor);
        sprite.paint(graphics, time);
    }
}
