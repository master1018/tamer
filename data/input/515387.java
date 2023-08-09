public class ConstantColor implements VariableColor
{
    private final Color value;
    public ConstantColor(Color value)
    {
        this.value = value;
    }
    public Color getColor(long time)
    {
        return value;
    }
}
