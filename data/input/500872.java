final class ConvertedIntegerValue extends SpecificIntegerValue
{
    private final Value value;
    public ConvertedIntegerValue(Value value)
    {
        this.value = value;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.value.equals(((ConvertedIntegerValue)object).value);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               value.hashCode();
    }
    public String toString()
    {
        return "(int)("+value+")";
    }
}