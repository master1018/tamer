public class BufferedSprite implements Sprite
{
    private final int         bufferX;
    private final int         bufferY;
    private final Image       bufferImage;
    private final Color       backgroundColor;
    private final Sprite      sprite;
    private final VariableInt x;
    private final VariableInt y;
    private long cachedTime = -1;
    public BufferedSprite(int         bufferX,
                          int         bufferY,
                          int         width,
                          int         height,
                          Sprite      sprite,
                          VariableInt x,
                          VariableInt y)
    {
        this(bufferX,
             bufferY,
             new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR),
             null,
             sprite,
             x,
             y);
    }
    public BufferedSprite(int         bufferX,
                          int         bufferY,
                          Image       bufferImage,
                          Color       backgroundColor,
                          Sprite      sprite,
                          VariableInt x,
                          VariableInt y)
    {
        this.bufferX         = bufferX;
        this.bufferY         = bufferY;
        this.bufferImage     = bufferImage;
        this.backgroundColor = backgroundColor;
        this.sprite          = sprite;
        this.x               = x;
        this.y               = y;
    }
    public void paint(Graphics graphics, long time)
    {
        if (time != cachedTime)
        {
            Graphics bufferGraphics = bufferImage.getGraphics();
            if (backgroundColor != null)
            {
                Graphics2D bufferGraphics2D = (Graphics2D)bufferGraphics;
                bufferGraphics2D.setComposite(AlphaComposite.Clear);
                bufferGraphics.fillRect(0, 0, bufferImage.getWidth(null), bufferImage.getHeight(null));
                bufferGraphics2D.setComposite(AlphaComposite.Src);
            }
            else
            {
                bufferGraphics.setColor(backgroundColor);
                bufferGraphics.fillRect(0, 0, bufferImage.getWidth(null), bufferImage.getHeight(null));
            }
            bufferGraphics.translate(-bufferX, -bufferY);
            bufferGraphics.setColor(graphics.getColor());
            bufferGraphics.setFont(graphics.getFont());
            sprite.paint(bufferGraphics, time);
            bufferGraphics.dispose();
            cachedTime = time;
        }
        graphics.drawImage(bufferImage,
                           bufferX + x.getInt(time),
                           bufferY + y.getInt(time),
                           null);
    }
}
