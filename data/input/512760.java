public class UnknownDoubleValue extends DoubleValue
{
    public DoubleValue negate()
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
    public FloatValue convertToFloat()
    {
        return ValueFactory.FLOAT_VALUE;
    }
    public DoubleValue generalize(DoubleValue other)
    {
        return this;
    }
    public DoubleValue add(DoubleValue other)
    {
        return this;
    }
    public DoubleValue subtract(DoubleValue other)
    {
        return this;
    }
    public DoubleValue subtractFrom(DoubleValue other)
    {
        return this;
    }
    public DoubleValue multiply(DoubleValue other)
    {
        return this;
    }
    public DoubleValue divide(DoubleValue other)
    {
        return this;
    }
    public DoubleValue divideOf(DoubleValue other)
    {
        return this;
    }
    public DoubleValue remainder(DoubleValue other)
    {
        return this;
    }
    public DoubleValue remainderOf(DoubleValue other)
    {
        return this;
    }
    public IntegerValue compare(DoubleValue other)
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