public class ConstantInt implements VariableInt
{
    private final int value;
    public ConstantInt(int value)
    {
        this.value = value;
    }
    public int getInt(long time)
    {
        return value;
    }
}
