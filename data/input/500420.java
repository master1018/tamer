final class NegatedFloatValue extends SpecificFloatValue
{
    private final FloatValue floatValue;
    public NegatedFloatValue(FloatValue floatValue)
    {
        this.floatValue = floatValue;
    }
    public FloatValue negate()
    {
        return floatValue;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.floatValue.equals(((NegatedFloatValue)object).floatValue);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               floatValue.hashCode();
    }
    public String toString()
    {
        return "-"+floatValue;
    }
}