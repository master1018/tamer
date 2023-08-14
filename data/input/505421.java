final class ConvertedDoubleValue extends SpecificDoubleValue
{
    private final Value value;
    public ConvertedDoubleValue(Value value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedDoubleValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(double)("+value+")";
    }
}