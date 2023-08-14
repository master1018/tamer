import proguard.classfile.ClassConstants;
public abstract class IntegerValue extends Category1Value
{
    public int value()
    {
        return 0;
    }
    public abstract IntegerValue negate();
    public abstract IntegerValue convertToByte();
    public abstract IntegerValue convertToCharacter();
    public abstract IntegerValue convertToShort();
    public abstract LongValue convertToLong();
    public abstract FloatValue convertToFloat();
    public abstract DoubleValue convertToDouble();
    public abstract IntegerValue generalize(IntegerValue other);
    public abstract IntegerValue add(IntegerValue other);
    public abstract IntegerValue subtract(IntegerValue other);
    public abstract IntegerValue subtractFrom(IntegerValue other);
    public abstract IntegerValue multiply(IntegerValue other)
    throws ArithmeticException;
    public abstract IntegerValue divide(IntegerValue other)
    throws ArithmeticException;
    public abstract IntegerValue divideOf(IntegerValue other)
    throws ArithmeticException;
    public abstract IntegerValue remainder(IntegerValue other)
    throws ArithmeticException;
    public abstract IntegerValue remainderOf(IntegerValue other)
    throws ArithmeticException;
    public abstract IntegerValue shiftLeft(IntegerValue other);
    public abstract IntegerValue shiftRight(IntegerValue other);
    public abstract IntegerValue unsignedShiftRight(IntegerValue other);
    public abstract IntegerValue shiftLeftOf(IntegerValue other);
    public abstract IntegerValue shiftRightOf(IntegerValue other);
    public abstract IntegerValue unsignedShiftRightOf(IntegerValue other);
    public abstract LongValue shiftLeftOf(LongValue other);
    public abstract LongValue shiftRightOf(LongValue other);
    public abstract LongValue unsignedShiftRightOf(LongValue other);
    public abstract IntegerValue and(IntegerValue other);
    public abstract IntegerValue or(IntegerValue other);
    public abstract IntegerValue xor(IntegerValue other);
    public abstract int equal(IntegerValue other);
    public abstract int lessThan(IntegerValue other);
    public abstract int lessThanOrEqual(IntegerValue other);
    public final int notEqual(IntegerValue other)
    {
        return -equal(other);
    }
    public final int greaterThan(IntegerValue other)
    {
        return -lessThanOrEqual(other);
    }
    public final int greaterThanOrEqual(IntegerValue other)
    {
        return -lessThan(other);
    }
    public IntegerValue generalize(UnknownIntegerValue other)
    {
        return generalize((IntegerValue)other);
    }
    public IntegerValue add(UnknownIntegerValue other)
    {
        return add((IntegerValue)other);
    }
    public IntegerValue subtract(UnknownIntegerValue other)
    {
        return subtract((IntegerValue)other);
    }
    public IntegerValue subtractFrom(UnknownIntegerValue other)
    {
        return subtractFrom((IntegerValue)other);
    }
    public IntegerValue multiply(UnknownIntegerValue other)
    {
        return multiply((IntegerValue)other);
    }
    public IntegerValue divide(UnknownIntegerValue other)
    {
        return divide((IntegerValue)other);
    }
    public IntegerValue divideOf(UnknownIntegerValue other)
    {
        return divideOf((IntegerValue)other);
    }
    public IntegerValue remainder(UnknownIntegerValue other)
    {
        return remainder((IntegerValue)other);
    }
    public IntegerValue remainderOf(UnknownIntegerValue other)
    {
        return remainderOf((IntegerValue)other);
    }
    public IntegerValue shiftLeft(UnknownIntegerValue other)
    {
        return shiftLeft((IntegerValue)other);
    }
    public IntegerValue shiftRight(UnknownIntegerValue other)
    {
        return shiftRight((IntegerValue)other);
    }
    public IntegerValue unsignedShiftRight(UnknownIntegerValue other)
    {
        return unsignedShiftRight((IntegerValue)other);
    }
    public IntegerValue shiftLeftOf(UnknownIntegerValue other)
    {
        return shiftLeftOf((IntegerValue)other);
    }
    public IntegerValue shiftRightOf(UnknownIntegerValue other)
    {
        return shiftRightOf((IntegerValue)other);
    }
    public IntegerValue unsignedShiftRightOf(UnknownIntegerValue other)
    {
        return unsignedShiftRightOf((IntegerValue)other);
    }
    public LongValue shiftLeftOf(UnknownLongValue other)
    {
        return shiftLeftOf((LongValue)other);
    }
    public LongValue shiftRightOf(UnknownLongValue other)
    {
        return shiftRightOf((LongValue)other);
    }
    public LongValue unsignedShiftRightOf(UnknownLongValue other)
    {
        return unsignedShiftRightOf((LongValue)other);
    }
    public IntegerValue and(UnknownIntegerValue other)
    {
        return and((IntegerValue)other);
    }
    public IntegerValue or(UnknownIntegerValue other)
    {
        return or((IntegerValue)other);
    }
    public IntegerValue xor(UnknownIntegerValue other)
    {
        return xor((IntegerValue)other);
    }
    public int equal(UnknownIntegerValue other)
    {
        return equal((IntegerValue)other);
    }
    public int lessThan(UnknownIntegerValue other)
    {
        return lessThan((IntegerValue)other);
    }
    public int lessThanOrEqual(UnknownIntegerValue other)
    {
        return lessThanOrEqual((IntegerValue)other);
    }
    public final int notEqual(UnknownIntegerValue other)
    {
        return -equal(other);
    }
    public final int greaterThan(UnknownIntegerValue other)
    {
        return -lessThanOrEqual(other);
    }
    public final int greaterThanOrEqual(UnknownIntegerValue other)
    {
        return -lessThan(other);
    }
    public IntegerValue generalize(SpecificIntegerValue other)
    {
        return generalize((IntegerValue)other);
    }
    public IntegerValue add(SpecificIntegerValue other)
    {
        return add((IntegerValue)other);
    }
    public IntegerValue subtract(SpecificIntegerValue other)
    {
        return subtract((IntegerValue)other);
    }
    public IntegerValue subtractFrom(SpecificIntegerValue other)
    {
        return subtractFrom((IntegerValue)other);
    }
    public IntegerValue multiply(SpecificIntegerValue other)
    {
        return multiply((IntegerValue)other);
    }
    public IntegerValue divide(SpecificIntegerValue other)
    {
        return divide((IntegerValue)other);
    }
    public IntegerValue divideOf(SpecificIntegerValue other)
    {
        return divideOf((IntegerValue)other);
    }
    public IntegerValue remainder(SpecificIntegerValue other)
    {
        return remainder((IntegerValue)other);
    }
    public IntegerValue remainderOf(SpecificIntegerValue other)
    {
        return remainderOf((IntegerValue)other);
    }
    public IntegerValue shiftLeft(SpecificIntegerValue other)
    {
        return shiftLeft((IntegerValue)other);
    }
    public IntegerValue shiftRight(SpecificIntegerValue other)
    {
        return shiftRight((IntegerValue)other);
    }
    public IntegerValue unsignedShiftRight(SpecificIntegerValue other)
    {
        return unsignedShiftRight((IntegerValue)other);
    }
    public IntegerValue shiftLeftOf(SpecificIntegerValue other)
    {
        return shiftLeftOf((IntegerValue)other);
    }
    public IntegerValue shiftRightOf(SpecificIntegerValue other)
    {
        return shiftRightOf((IntegerValue)other);
    }
    public IntegerValue unsignedShiftRightOf(SpecificIntegerValue other)
    {
        return unsignedShiftRightOf((IntegerValue)other);
    }
    public LongValue shiftLeftOf(SpecificLongValue other)
    {
        return shiftLeftOf((LongValue)other);
    }
    public LongValue shiftRightOf(SpecificLongValue other)
    {
        return shiftRightOf((LongValue)other);
    }
    public LongValue unsignedShiftRightOf(SpecificLongValue other)
    {
        return unsignedShiftRightOf((LongValue)other);
    }
    public IntegerValue and(SpecificIntegerValue other)
    {
        return and((IntegerValue)other);
    }
    public IntegerValue or(SpecificIntegerValue other)
    {
        return or((IntegerValue)other);
    }
    public IntegerValue xor(SpecificIntegerValue other)
    {
        return xor((IntegerValue)other);
    }
    public int equal(SpecificIntegerValue other)
    {
        return equal((IntegerValue)other);
    }
    public int lessThan(SpecificIntegerValue other)
    {
        return lessThan((IntegerValue)other);
    }
    public int lessThanOrEqual(SpecificIntegerValue other)
    {
        return lessThanOrEqual((IntegerValue)other);
    }
    public final int notEqual(SpecificIntegerValue other)
    {
        return -equal(other);
    }
    public final int greaterThan(SpecificIntegerValue other)
    {
        return -lessThanOrEqual(other);
    }
    public final int greaterThanOrEqual(SpecificIntegerValue other)
    {
        return -lessThan(other);
    }
    public IntegerValue generalize(ParticularIntegerValue other)
    {
        return generalize((SpecificIntegerValue)other);
    }
    public IntegerValue add(ParticularIntegerValue other)
    {
        return add((SpecificIntegerValue)other);
    }
    public IntegerValue subtract(ParticularIntegerValue other)
    {
        return subtract((SpecificIntegerValue)other);
    }
    public IntegerValue subtractFrom(ParticularIntegerValue other)
    {
        return subtractFrom((SpecificIntegerValue)other);
    }
    public IntegerValue multiply(ParticularIntegerValue other)
    {
        return multiply((SpecificIntegerValue)other);
    }
    public IntegerValue divide(ParticularIntegerValue other)
    {
        return divide((SpecificIntegerValue)other);
    }
    public IntegerValue divideOf(ParticularIntegerValue other)
    {
        return divideOf((SpecificIntegerValue)other);
    }
    public IntegerValue remainder(ParticularIntegerValue other)
    {
        return remainder((SpecificIntegerValue)other);
    }
    public IntegerValue remainderOf(ParticularIntegerValue other)
    {
        return remainderOf((SpecificIntegerValue)other);
    }
    public IntegerValue shiftLeft(ParticularIntegerValue other)
    {
        return shiftLeft((SpecificIntegerValue)other);
    }
    public IntegerValue shiftRight(ParticularIntegerValue other)
    {
        return shiftRight((SpecificIntegerValue)other);
    }
    public IntegerValue unsignedShiftRight(ParticularIntegerValue other)
    {
        return unsignedShiftRight((SpecificIntegerValue)other);
    }
    public IntegerValue shiftLeftOf(ParticularIntegerValue other)
    {
        return shiftLeftOf((SpecificIntegerValue)other);
    }
    public IntegerValue shiftRightOf(ParticularIntegerValue other)
    {
        return shiftRightOf((SpecificIntegerValue)other);
    }
    public IntegerValue unsignedShiftRightOf(ParticularIntegerValue other)
    {
        return unsignedShiftRightOf((SpecificIntegerValue)other);
    }
    public LongValue shiftLeftOf(ParticularLongValue other)
    {
        return shiftLeftOf((SpecificLongValue)other);
    }
    public LongValue shiftRightOf(ParticularLongValue other)
    {
        return shiftRightOf((SpecificLongValue)other);
    }
    public LongValue unsignedShiftRightOf(ParticularLongValue other)
    {
        return unsignedShiftRightOf((SpecificLongValue)other);
    }
    public IntegerValue and(ParticularIntegerValue other)
    {
        return and((SpecificIntegerValue)other);
    }
    public IntegerValue or(ParticularIntegerValue other)
    {
        return or((SpecificIntegerValue)other);
    }
    public IntegerValue xor(ParticularIntegerValue other)
    {
        return xor((SpecificIntegerValue)other);
    }
    public int equal(ParticularIntegerValue other)
    {
        return equal((SpecificIntegerValue)other);
    }
    public int lessThan(ParticularIntegerValue other)
    {
        return lessThan((SpecificIntegerValue)other);
    }
    public int lessThanOrEqual(ParticularIntegerValue other)
    {
        return lessThanOrEqual((SpecificIntegerValue)other);
    }
    public final int notEqual(ParticularIntegerValue other)
    {
        return -equal(other);
    }
    public final int greaterThan(ParticularIntegerValue other)
    {
        return -lessThanOrEqual(other);
    }
    public final int greaterThanOrEqual(ParticularIntegerValue other)
    {
        return -lessThan(other);
    }
    public final IntegerValue integerValue()
    {
        return this;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.integerValue());
    }
    public final int computationalType()
    {
        return TYPE_INTEGER;
    }
    public final String internalType()
    {
        return String.valueOf(ClassConstants.INTERNAL_TYPE_INT);
    }
}
