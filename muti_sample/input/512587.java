public class VariableSizeFont implements VariableFont
{
    private final Font           font;
    private final VariableDouble size;
    private float cachedSize = -1.0f;
    private Font  cachedFont;
    public VariableSizeFont(Font font, VariableDouble size)
    {
        this.font = font;
        this.size = size;
    }
    public Font getFont(long time)
    {
        float s = (float)size.getDouble(time);
        if (s != cachedSize)
        {
            cachedSize = s;
            cachedFont = font.deriveFont((float)s);
        }
        return cachedFont;
    }
}
