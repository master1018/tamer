public class UnknownLongValue extends LongValue
{
    public LongValue negate()
    {
        return this;
    }
    public IntegerValue convertToInteger()
    {
        return ValueFactory.INTEGER_VALUE;
    }
    public FloatValue convertToFloat()
    {
        return ValueFactory.FLOAT_VALUE;
    }
    public DoubleValue convertToDouble()
    {
        return ValueFactory.DOUBLE_VALUE;
    }
    public LongValue generalize(LongValue other)
    {
        return this;
    }
    public LongValue add(LongValue other)
    {
        return this;
    }
    public LongValue subtract(LongValue other)
    {
        return this;
    }
    public LongValue subtractFrom(LongValue other)
    {
        return this;
    }
    public LongValue multiply(LongValue other)
    throws ArithmeticException
    {
        return this;
    }
    public LongValue divide(LongValue other)
    throws ArithmeticException
    {
        return this;
    }
    public LongValue divideOf(LongValue other)
    throws ArithmeticException
    {
        return this;
    }
    public LongValue remainder(LongValue other)
    throws ArithmeticException
    {
        return this;
    }
    public LongValue remainderOf(LongValue other)
    throws ArithmeticException
    {
        return this;
    }
    public LongValue shiftLeft(IntegerValue other)
    {
        return this;
    }
    public LongValue shiftRight(IntegerValue other)
    {
        return this;
    }
    public LongValue unsignedShiftRight(IntegerValue other)
    {
        return this;
    }
    public LongValue and(LongValue other)
    {
        return this;
    }
    public LongValue or(LongValue other)
    {
        return this;
    }
    public LongValue xor(LongValue other)
    {
        return this;
    }
    public IntegerValue compare(LongValue other)
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
        return "l";
    }
}