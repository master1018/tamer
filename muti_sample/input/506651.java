import proguard.classfile.ClassConstants;
public abstract class LongValue extends Category2Value
{
    public long value()
    {
        return 0;
    }
    public abstract LongValue negate();
    public abstract IntegerValue convertToInteger();
    public abstract FloatValue convertToFloat();
    public abstract DoubleValue convertToDouble();
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
    throws ArithmeticException
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
    public final IntegerValue compareReverse(LongValue other)
    {
        return compare(other).negate();
    }
    public LongValue generalize(SpecificLongValue other)
    {
        return this;
    }
    public LongValue add(SpecificLongValue other)
    {
        return this;
    }
    public LongValue subtract(SpecificLongValue other)
    {
        return this;
    }
    public LongValue subtractFrom(SpecificLongValue other)
    {
        return this;
    }
    public LongValue multiply(SpecificLongValue other)
    {
        return this;
    }
    public LongValue divide(SpecificLongValue other)
    {
        return this;
    }
    public LongValue divideOf(SpecificLongValue other)
    {
        return this;
    }
    public LongValue remainder(SpecificLongValue other)
    {
        return this;
    }
    public LongValue remainderOf(SpecificLongValue other)
    {
        return this;
    }
    public LongValue shiftLeft(SpecificLongValue other)
    {
        return this;
    }
    public LongValue shiftRight(SpecificLongValue other)
    {
        return this;
    }
    public LongValue unsignedShiftRight(SpecificLongValue other)
    {
        return this;
    }
    public LongValue and(SpecificLongValue other)
    {
        return this;
    }
    public LongValue or(SpecificLongValue other)
    {
        return this;
    }
    public LongValue xor(SpecificLongValue other)
    {
        return this;
    }
    public IntegerValue compare(SpecificLongValue other)
    {
        return new ComparisonValue(this, other);
    }
    public final IntegerValue compareReverse(SpecificLongValue other)
    {
        return compare(other).negate();
    }
    public LongValue generalize(ParticularLongValue other)
    {
        return generalize((SpecificLongValue)other);
    }
    public LongValue add(ParticularLongValue other)
    {
        return add((SpecificLongValue)other);
    }
    public LongValue subtract(ParticularLongValue other)
    {
        return subtract((SpecificLongValue)other);
    }
    public LongValue subtractFrom(ParticularLongValue other)
    {
        return subtractFrom((SpecificLongValue)other);
    }
    public LongValue multiply(ParticularLongValue other)
    {
        return multiply((SpecificLongValue)other);
    }
    public LongValue divide(ParticularLongValue other)
    {
        return divide((SpecificLongValue)other);
    }
    public LongValue divideOf(ParticularLongValue other)
    {
        return divideOf((SpecificLongValue)other);
    }
    public LongValue remainder(ParticularLongValue other)
    {
        return remainder((SpecificLongValue)other);
    }
    public LongValue remainderOf(ParticularLongValue other)
    {
        return remainderOf((SpecificLongValue)other);
    }
    public LongValue shiftLeft(ParticularIntegerValue other)
    {
        return shiftLeft((SpecificIntegerValue)other);
    }
    public LongValue shiftRight(ParticularIntegerValue other)
    {
        return shiftRight((SpecificIntegerValue)other);
    }
    public LongValue unsignedShiftRight(ParticularIntegerValue other)
    {
        return unsignedShiftRight((SpecificIntegerValue)other);
    }
    public LongValue and(ParticularLongValue other)
    {
        return and((SpecificLongValue)other);
    }
    public LongValue or(ParticularLongValue other)
    {
        return or((SpecificLongValue)other);
    }
    public LongValue xor(ParticularLongValue other)
    {
        return xor((SpecificLongValue)other);
    }
    public IntegerValue compare(ParticularLongValue other)
    {
        return compare((SpecificLongValue)other);
    }
    public final IntegerValue compareReverse(ParticularLongValue other)
    {
        return compare(other).negate();
    }
    public final LongValue longValue()
    {
        return this;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.longValue());
    }
    public final int computationalType()
    {
        return TYPE_LONG;
    }
    public final String internalType()
    {
        return String.valueOf(ClassConstants.INTERNAL_TYPE_INT);
    }
}
