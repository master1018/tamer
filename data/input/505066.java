final class NegatedDoubleValue extends SpecificDoubleValue
{
    private final DoubleValue doubleValue;
    public NegatedDoubleValue(DoubleValue doubleValue)
    {
        this.doubleValue = doubleValue;
    }
    public DoubleValue negate()
    {
        return doubleValue;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.doubleValue.equals(((NegatedDoubleValue)object).doubleValue);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               doubleValue.hashCode();
    }
    public String toString()
    {
        return "-"+doubleValue;
    }
}