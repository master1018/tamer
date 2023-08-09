public class ConstantDouble implements VariableDouble
{
    private final double value;
    public ConstantDouble(double value)
    {
        this.value = value;
    }
    public double getDouble(long time)
    {
        return value;
    }
}
