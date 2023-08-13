public class ClipSprite implements Sprite
{
    private final VariableColor insideClipColor;
    private final VariableColor outsideClipColor;
    private final Sprite        clipSprite;
    private final Sprite        sprite;
    public ClipSprite(VariableColor insideClipColor,
                      VariableColor outsideClipColor,
                      Sprite        clipSprite,
                      Sprite        sprite)
    {
        this.insideClipColor  = insideClipColor;
        this.outsideClipColor = outsideClipColor;
        this.clipSprite       = clipSprite;
        this.sprite           = sprite;
    }
    public void paint(Graphics graphics, long time)
    {
        Color outsideColor = outsideClipColor.getColor(time);
        Rectangle clip = graphics.getClipBounds();
        graphics.setPaintMode();
        graphics.setColor(outsideColor);
        graphics.fillRect(0, 0, clip.width, clip.height);
        OverrideGraphics2D g = new OverrideGraphics2D((Graphics2D)graphics);
        Color insideColor = insideClipColor.getColor(time);
        g.setOverrideXORMode(insideColor);
        sprite.paint(g, time);
        g.setOverrideXORMode(null);
        g.setOverrideColor(insideColor);
        clipSprite.paint(g, time);
        g.setOverrideColor(null);
        g.setOverrideXORMode(insideColor);
        sprite.paint(g, time);
        g.setOverrideXORMode(null);
    }
}
