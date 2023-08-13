public class UnknownFloatValue extends FloatValue
{
    public FloatValue negate()
    {
        return this;
    }
    public IntegerValue convertToInteger()
    {
        return ValueFactory.INTEGER_VALUE;
    }
    public LongValue convertToLong()
    {
        return ValueFactory.LONG_VALUE;
    }
    public DoubleValue convertToDouble()
    {
        return ValueFactory.DOUBLE_VALUE;
    }
    public FloatValue generalize(FloatValue other)
    {
        return this;
    }
    public FloatValue add(FloatValue other)
    {
        return this;
    }
    public FloatValue subtract(FloatValue other)
    {
        return this;
    }
    public FloatValue subtractFrom(FloatValue other)
    {
        return this;
    }
    public FloatValue multiply(FloatValue other)
    {
        return this;
    }
    public FloatValue divide(FloatValue other)
    {
        return this;
    }
    public FloatValue divideOf(FloatValue other)
    {
        return this;
    }
    public FloatValue remainder(FloatValue other)
    {
        return this;
    }
    public FloatValue remainderOf(FloatValue other)
    {
        return this;
    }
    public IntegerValue compare(FloatValue other)
    {
        return ValueFactory.INTEGER_VALUE;
    }
    public boolean equals(Object object)
    {
        return object != null &&
               this.getClass() == object.getClass();
    }
    public int hashCode()
    {
        return this.getClass().hashCode();
    }
    public String toString()
    {
        return "d";
    }
}