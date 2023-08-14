abstract class SpecificIntegerValue extends IntegerValue
{
    public IntegerValue negate()
    {
        return new NegatedIntegerValue(this);
    }
    public IntegerValue convertToByte()
    {
        return new ConvertedByteValue(this);
    }
    public IntegerValue convertToCharacter()
    {
        return new ConvertedCharacterValue(this);
    }
    public IntegerValue convertToShort()
    {
        return new ConvertedShortValue(this);
    }
    public LongValue convertToLong()
    {
        return new ConvertedLongValue(this);
    }
    public FloatValue convertToFloat()
    {
        return new ConvertedFloatValue(this);
    }
    public DoubleValue convertToDouble()
    {
        return new ConvertedDoubleValue(this);
    }
    public IntegerValue generalize(IntegerValue other)
    {
        return other.generalize(this);
    }
    public IntegerValue add(IntegerValue other)
    {
        return other.add(this);
    }
    public IntegerValue subtract(IntegerValue other)
    {
        return other.subtractFrom(this);
    }
    public IntegerValue subtractFrom(IntegerValue other)
    {
        return other.subtract(this);
    }
    public IntegerValue multiply(IntegerValue other)
    {
        return other.multiply(this);
    }
    public IntegerValue divide(IntegerValue other)
    throws ArithmeticException
    {
        return other.divideOf(this);
    }
    public IntegerValue divideOf(IntegerValue other)
    throws ArithmeticException
    {
        return other.divide(this);
    }
    public IntegerValue remainder(IntegerValue other)
    throws ArithmeticException
    {
        return other.remainderOf(this);
    }
    public IntegerValue remainderOf(IntegerValue other)
    throws ArithmeticException
    {
        return other.remainder(this);
    }
    public IntegerValue shiftLeft(IntegerValue other)
    {
        return other.shiftLeftOf(this);
    }
    public IntegerValue shiftLeftOf(IntegerValue other)
    {
        return other.shiftLeft(this);
    }
    public IntegerValue shiftRight(IntegerValue other)
    {
        return other.shiftRightOf(this);
    }
    public IntegerValue shiftRightOf(IntegerValue other)
    {
        return other.shiftRight(this);
    }
    public IntegerValue unsignedShiftRight(IntegerValue other)
    {
        return other.unsignedShiftRightOf(this);
    }
    public IntegerValue unsignedShiftRightOf(IntegerValue other)
    {
        return other.unsignedShiftRight(this);
    }
    public LongValue shiftLeftOf(LongValue other)
    {
        return other.shiftLeft(this);
    }
    public LongValue shiftRightOf(LongValue other)
    {
        return other.shiftRight(this);
    }
    public LongValue unsignedShiftRightOf(LongValue other)
    {
        return other.unsignedShiftRight(this);
    }
    public IntegerValue and(IntegerValue other)
    {
        return other.and(this);
    }
    public IntegerValue or(IntegerValue other)
    {
        return other.or(this);
    }
    public IntegerValue xor(IntegerValue other)
    {
        return other.xor(this);
    }
    public int equal(IntegerValue other)
    {
        return other.equal(this);
    }
    public int lessThan(IntegerValue other)
    {
        return other.greaterThan(this);
    }
    public int lessThanOrEqual(IntegerValue other)
    {
        return other.greaterThanOrEqual(this);
    }
    public IntegerValue generalize(SpecificIntegerValue other)
    {
        return this.equals(other) ? this : ValueFactory.INTEGER_VALUE;
    }
    public IntegerValue add(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.ADD, other);
    }
    public IntegerValue subtract(SpecificIntegerValue other)
    {
        return this.equals(other) ?
            SpecificValueFactory.INTEGER_VALUE_0 :
            new CompositeIntegerValue(this, CompositeIntegerValue.SUBTRACT, other);
    }
    public IntegerValue subtractFrom(SpecificIntegerValue other)
    {
        return this.equals(other) ?
            SpecificValueFactory.INTEGER_VALUE_0 :
            new CompositeIntegerValue(other, CompositeIntegerValue.SUBTRACT, this);
    }
    public IntegerValue multiply(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.MULTIPLY, other);
    }
    public IntegerValue divide(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.DIVIDE, other);
    }
    public IntegerValue divideOf(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return new CompositeIntegerValue(other, CompositeIntegerValue.DIVIDE, this);
    }
    public IntegerValue remainder(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.REMAINDER, other);
    }
    public IntegerValue remainderOf(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return new CompositeIntegerValue(other, CompositeIntegerValue.REMAINDER, this);
    }
    public IntegerValue shiftLeft(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.SHIFT_LEFT, other);
    }
    public IntegerValue shiftRight(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.SHIFT_RIGHT, other);
    }
    public IntegerValue unsignedShiftRight(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(this, CompositeIntegerValue.UNSIGNED_SHIFT_RIGHT, other);
    }
    public IntegerValue shiftLeftOf(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(other, CompositeIntegerValue.SHIFT_LEFT, this);
    }
    public IntegerValue shiftRightOf(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(other, CompositeIntegerValue.SHIFT_RIGHT, this);
    }
    public IntegerValue unsignedShiftRightOf(SpecificIntegerValue other)
    {
        return new CompositeIntegerValue(other, CompositeIntegerValue.UNSIGNED_SHIFT_RIGHT, this);
    }
    public LongValue shiftLeftOf(SpecificLongValue other)
    {
        return new CompositeLongValue(other, CompositeLongValue.SHIFT_LEFT, this);
    }
    public LongValue shiftRightOf(SpecificLongValue other)
    {
        return new CompositeLongValue(other, CompositeLongValue.SHIFT_RIGHT, this);
    }
    public LongValue unsignedShiftRightOf(SpecificLongValue other)
    {
        return new CompositeLongValue(other, CompositeLongValue.UNSIGNED_SHIFT_RIGHT, this);
    }
    public IntegerValue and(SpecificIntegerValue other)
    {
        return this.equals(other) ?
            this :
            new CompositeIntegerValue(other, CompositeIntegerValue.AND, this);
    }
    public IntegerValue or(SpecificIntegerValue other)
    {
        return this.equals(other) ?
            this :
            new CompositeIntegerValue(other, CompositeIntegerValue.OR, this);
    }
    public IntegerValue xor(SpecificIntegerValue other)
    {
        return this.equals(other) ?
            SpecificValueFactory.INTEGER_VALUE_0 :
            new CompositeIntegerValue(other, CompositeIntegerValue.XOR, this);
    }
    public int equal(SpecificIntegerValue other)
    {
        return this.equals(other) ? ALWAYS : MAYBE;
    }
    public int lessThan(SpecificIntegerValue other)
    {
        return this.equals(other) ? NEVER : MAYBE;
    }
    public int lessThanOrEqual(SpecificIntegerValue other)
    {
        return this.equals(other) ? ALWAYS : MAYBE;
    }
    public boolean isSpecific()
    {
        return true;
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
}
