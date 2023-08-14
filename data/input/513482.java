public class ConstantString implements VariableString
{
    private final String value;
    public ConstantString(String value)
    {
        this.value = value;
    }
    public String getString(long time)
    {
        return value;
    }
}
