final class ConvertedByteValue extends SpecificIntegerValue
{
    private final IntegerValue value;
    public ConvertedByteValue(IntegerValue value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedByteValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(byte)("+value+")";
    }
}