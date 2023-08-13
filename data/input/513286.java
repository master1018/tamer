public class TextSprite implements Sprite
{
    private final VariableString[] text;
    private final VariableInt      spacing;
    private final VariableInt      x;
    private final VariableInt      y;
    public TextSprite(VariableString text,
                      VariableInt    x,
                      VariableInt    y)
    {
        this(new VariableString[] { text }, new ConstantInt(0), x, y);
    }
    public TextSprite(VariableString[] text,
                      VariableInt      spacing,
                      VariableInt      x,
                      VariableInt      y)
    {
        this.text    = text;
        this.spacing = spacing;
        this.x       = x;
        this.y       = y;
    }
    public void paint(Graphics graphics, long time)
    {
        int xt = x.getInt(time);
        int yt = y.getInt(time);
        int spacingt = spacing.getInt(time);
        for (int index = 0; index < text.length; index++)
        {
            graphics.drawString(text[index].getString(time), xt, yt + index * spacingt);
        }
    }
}
