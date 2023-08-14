public class FontSprite implements Sprite
{
    private final VariableFont font;
    private final Sprite       sprite;
    public FontSprite(VariableFont font,
                      Sprite       sprite)
    {
        this.font   = font;
        this.sprite = sprite;
    }
    public void paint(Graphics graphics, long time)
    {
        Font oldFont = graphics.getFont();
        graphics.setFont(font.getFont(time));
        sprite.paint(graphics, time);
        graphics.setFont(oldFont);
    }
}
