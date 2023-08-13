final class NegatedLongValue extends SpecificLongValue
{
    private final LongValue longValue;
    public NegatedLongValue(LongValue longValue)
    {
        this.longValue = longValue;
    }
    public LongValue negate()
    {
        return longValue;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.longValue.equals(((NegatedLongValue)object).longValue);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               longValue.hashCode();
    }
    public String toString()
    {
        return "-"+longValue;
    }
}