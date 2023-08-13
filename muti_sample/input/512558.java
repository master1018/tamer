final class ConvertedShortValue extends SpecificIntegerValue
{
    private final IntegerValue value;
    public ConvertedShortValue(IntegerValue value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedShortValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(short)("+value+")";
    }
}