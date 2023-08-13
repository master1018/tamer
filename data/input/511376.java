final class ConvertedLongValue extends SpecificLongValue
{
    private final Value value;
    public ConvertedLongValue(Value value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedLongValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(long)("+value+")";
    }
}