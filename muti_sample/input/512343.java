final class ParticularLongValue extends SpecificLongValue
{
    private final long value;
    public ParticularLongValue(long value)
    {
        this.value = value;
    }
    public long value()
    {
        return value;
    }
    public LongValue negate()
    {
        return new ParticularLongValue(-value);
    }
    public IntegerValue convertToInteger()
    {
        return new ParticularIntegerValue((int)value);
    }
    public FloatValue convertToFloat()
    {
        return new ParticularFloatValue((float)value);
    }
    public DoubleValue convertToDouble()
    {
        return new ParticularDoubleValue((double)value);
    }
    public LongValue generalize(LongValue other)
    {
        return other.generalize(this);
    }
    public LongValue add(LongValue other)
    {
        return other.add(this);
    }
    public LongValue subtract(LongValue other)
    {
        return other.subtractFrom(this);
    }
    public LongValue subtractFrom(LongValue other)
    {
        return other.subtract(this);
    }
    public LongValue multiply(LongValue other)
    {
        return other.multiply(this);
    }
    public LongValue divide(LongValue other)
    throws ArithmeticException
    {
        return other.divideOf(this);
    }
    public LongValue divideOf(LongValue other)
    throws ArithmeticException
    {
        return other.divide(this);
    }
    public LongValue remainder(LongValue other)
    throws ArithmeticException
    {
        return other.remainderOf(this);
    }
    public LongValue remainderOf(LongValue other)
    throws ArithmeticException
    {
        return other.remainder(this);
    }
    public LongValue shiftLeft(IntegerValue other)
    {
        return other.shiftLeftOf(this);
    }
    public LongValue shiftRight(IntegerValue other)
    {
        return other.shiftRightOf(this);
    }
    public LongValue unsignedShiftRight(IntegerValue other)
    {
        return other.unsignedShiftRightOf(this);
    }
    public LongValue and(LongValue other)
    {
        return other.and(this);
    }
    public LongValue or(LongValue other)
    {
        return other.or(this);
    }
    public LongValue xor(LongValue other)
    {
        return other.xor(this);
    }
    public IntegerValue compare(LongValue other)
    {
        return other.compareReverse(this);
    }
    public LongValue generalize(ParticularLongValue other)
    {
        return generalize((SpecificLongValue)other);
    }
    public LongValue add(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value + other.value);
    }
    public LongValue subtract(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value - other.value);
    }
    public LongValue subtractFrom(ParticularLongValue other)
    {
        return new ParticularLongValue(other.value - this.value);
    }
    public LongValue multiply(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value * other.value);
    }
    public LongValue divide(ParticularLongValue other)
    throws ArithmeticException
    {
        return new ParticularLongValue(this.value / other.value);
    }
    public LongValue divideOf(ParticularLongValue other)
    throws ArithmeticException
    {
        return new ParticularLongValue(other.value / this.value);
    }
    public LongValue remainder(ParticularLongValue other)
    throws ArithmeticException
    {
        return new ParticularLongValue(this.value % other.value);
    }
    public LongValue remainderOf(ParticularLongValue other)
    throws ArithmeticException
    {
        return new ParticularLongValue(other.value % this.value);
    }
    public LongValue shiftLeft(ParticularIntegerValue other)
    {
        return new ParticularLongValue(this.value << other.value());
    }
    public LongValue shiftRight(ParticularIntegerValue other)
    {
        return new ParticularLongValue(this.value >> other.value());
    }
    public LongValue unsignedShiftRight(ParticularIntegerValue other)
    {
        return new ParticularLongValue(this.value >>> other.value());
    }
    public LongValue and(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value & other.value);
    }
    public LongValue or(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value | other.value);
    }
    public LongValue xor(ParticularLongValue other)
    {
        return new ParticularLongValue(this.value ^ other.value);
    }
    public boolean isParticular()
    {
        return true;
    }
    public boolean equals(Object object)
    {
        return super.equals(object) &&
               this.value == ((ParticularLongValue)object).value;
    }
    public int hashCode()
    {
        return this.getClass().hashCode() ^
               (int)value;
    }
    public String toString()
    {
        return value+"L";
    }
}