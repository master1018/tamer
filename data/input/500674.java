final class NegatedIntegerValue extends SpecificIntegerValue
{
    private final IntegerValue integerValue;
    public NegatedIntegerValue(IntegerValue integerValue)
    {
        this.integerValue = integerValue;
    }
    public IntegerValue negate()
    {
        return integerValue;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.integerValue.equals(((NegatedIntegerValue)object).integerValue);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               integerValue.hashCode();
    }
    public String toString()
    {
        return "-"+integerValue;
    }
}