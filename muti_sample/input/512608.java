public class RectangleSprite implements Sprite
{
    private final boolean       filled;
    private final VariableColor color;
    private final VariableInt   x;
    private final VariableInt   y;
    private final VariableInt   width;
    private final VariableInt   height;
    private final VariableInt   arcWidth;
    private final VariableInt   arcHeight;
    public RectangleSprite(boolean       filled,
                           VariableColor color,
                           VariableInt   x,
                           VariableInt   y,
                           VariableInt   width,
                           VariableInt   height)
    {
        this(filled, color, x, y, width, height, new ConstantInt(0), new ConstantInt(0));
    }
    public RectangleSprite(boolean       filled,
                           VariableColor color,
                           VariableInt   x,
                           VariableInt   y,
                           VariableInt   width,
                           VariableInt   height,
                           VariableInt   arcWidth,
                           VariableInt   arcHeight)
    {
        this.filled    = filled;
        this.color     = color;
        this.x         = x;
        this.y         = y;
        this.width     = width;
        this.height    = height;
        this.arcWidth  = arcWidth;
        this.arcHeight = arcHeight;
    }
    public void paint(Graphics graphics, long time)
    {
        graphics.setColor(color.getColor(time));
        int xt = x.getInt(time);
        int yt = y.getInt(time);
        int w  = width.getInt(time);
        int h  = height.getInt(time);
        int aw = arcWidth.getInt(time);
        int ah = arcHeight.getInt(time);
        if (filled)
        {
            graphics.fillRoundRect(xt, yt, w, h, aw, ah);
        }
        else
        {
            graphics.drawRoundRect(xt, yt, w, h, aw, ah);
        }
    }
}
