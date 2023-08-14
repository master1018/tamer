public class CompositeSprite implements Sprite
{
    private final Sprite[] sprites;
    public CompositeSprite(Sprite[] sprites)
    {
        this.sprites = sprites;
    }
    public void paint(Graphics graphics, long time)
    {
        for (int index = 0; index < sprites.length; index++)
        {
            sprites[index].paint(graphics, time);
        }
    }
}
