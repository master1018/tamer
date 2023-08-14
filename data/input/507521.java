public class ColorSprite implements Sprite
{
    private final VariableColor color;
    private final Sprite        sprite;
    public ColorSprite(VariableColor color,
                       Sprite        sprite)
    {
        this.color  = color;
        this.sprite = sprite;
    }
    public void paint(Graphics graphics, long time)
    {
        Color oldColor = graphics.getColor();
        graphics.setColor(color.getColor(time));
        sprite.paint(graphics, time);
        graphics.setColor(oldColor);
    }
}
