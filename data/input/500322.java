final class ConvertedFloatValue extends SpecificFloatValue
{
    private final Value value;
    public ConvertedFloatValue(Value value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedFloatValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(float)("+value+")";
    }
}