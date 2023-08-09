public class UnknownIntegerValue extends IntegerValue
{
    public IntegerValue negate()
    {
        return this;
    }
    public IntegerValue convertToByte()
    {
        return this;
    }
    public IntegerValue convertToCharacter()
    {
        return this;
    }
    public IntegerValue convertToShort()
    {
        return this;
    }
    public LongValue convertToLong()
    {
        return ValueFactory.LONG_VALUE;
    }
    public FloatValue convertToFloat()
    {
        return ValueFactory.FLOAT_VALUE;
    }
    public DoubleValue convertToDouble()
    {
        return ValueFactory.DOUBLE_VALUE;
    }
    public IntegerValue generalize(IntegerValue other)
    {
        return this;
    }
    public IntegerValue add(IntegerValue other)
    {
        return this;
    }
    public IntegerValue subtract(IntegerValue other)
    {
        return this;
    }
    public IntegerValue subtractFrom(IntegerValue other)
    {
        return this;
    }
    public IntegerValue multiply(IntegerValue other)
    throws ArithmeticException
    {
        return this;
    }
    public IntegerValue divide(IntegerValue other)
    throws ArithmeticException
    {
        return this;
    }
    public IntegerValue divideOf(IntegerValue other)
    throws ArithmeticException
    {
        return this;
    }
    public IntegerValue remainder(IntegerValue other)
    throws ArithmeticException
    {
        return this;
    }
    public IntegerValue remainderOf(IntegerValue other)
    throws ArithmeticException
    {
        return this;
    }
    public IntegerValue shiftLeft(IntegerValue other)
    {
        return this;
    }
    public IntegerValue shiftLeftOf(IntegerValue other)
    {
        return this;
    }
    public IntegerValue shiftRight(IntegerValue other)
    {
        return this;
    }
    public IntegerValue shiftRightOf(IntegerValue other)
    {
        return this;
    }
    public IntegerValue unsignedShiftRight(IntegerValue other)
    {
        return this;
    }
    public IntegerValue unsignedShiftRightOf(IntegerValue other)
    {
        return this;
    }
    public LongValue shiftLeftOf(LongValue other)
    {
        return ValueFactory.LONG_VALUE;
    }
    public LongValue shiftRightOf(LongValue other)
    {
        return ValueFactory.LONG_VALUE;
    }
    public LongValue unsignedShiftRightOf(LongValue other)
    {
        return ValueFactory.LONG_VALUE;
    }
    public IntegerValue and(IntegerValue other)
    {
        return this;
    }
    public IntegerValue or(IntegerValue other)
    {
        return this;
    }
    public IntegerValue xor(IntegerValue other)
    {
        return this;
    }
    public int equal(IntegerValue other)
    {
        return MAYBE;
    }
    public int lessThan(IntegerValue other)
    {
        return MAYBE;
    }
    public int lessThanOrEqual(IntegerValue other)
    {
        return MAYBE;
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
        return "i";
    }
}