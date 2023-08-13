public class ConstantFont implements VariableFont
{
    private final Font value;
    public ConstantFont(Font value)
    {
        this.value = value;
    }
    public Font getFont(long time)
    {
        return value;
    }
}
