import proguard.classfile.ClassConstants;
public abstract class DoubleValue extends Category2Value
{
    public double value()
    {
        return 0.0;
    }
    public abstract DoubleValue negate();
    public abstract IntegerValue convertToInteger();
    public abstract LongValue convertToLong();
    public abstract FloatValue convertToFloat();
    public abstract DoubleValue generalize(DoubleValue other);
    public abstract DoubleValue add(DoubleValue other);
    public abstract DoubleValue subtract(DoubleValue other);
    public abstract DoubleValue subtractFrom(DoubleValue other);
    public abstract DoubleValue multiply(DoubleValue other);
    public abstract DoubleValue divide(DoubleValue other);
    public abstract DoubleValue divideOf(DoubleValue other);
    public abstract DoubleValue remainder(DoubleValue other);
    public abstract DoubleValue remainderOf(DoubleValue other);
    public abstract IntegerValue compare(DoubleValue other);
    public final IntegerValue compareReverse(DoubleValue other)
    {
        return compare(other).negate();
    }
    public DoubleValue generalize(SpecificDoubleValue other)
    {
        return generalize((DoubleValue)other);
    }
    public DoubleValue add(SpecificDoubleValue other)
    {
        return add((DoubleValue)other);
    }
    public DoubleValue subtract(SpecificDoubleValue other)
    {
        return subtract((DoubleValue)other);
    }
    public DoubleValue subtractFrom(SpecificDoubleValue other)
    {
        return subtractFrom((DoubleValue)other);
    }
    public DoubleValue multiply(SpecificDoubleValue other)
    {
        return multiply((DoubleValue)other);
    }
    public DoubleValue divide(SpecificDoubleValue other)
    {
        return divide((DoubleValue)other);
    }
    public DoubleValue divideOf(SpecificDoubleValue other)
    {
        return divideOf((DoubleValue)other);
    }
    public DoubleValue remainder(SpecificDoubleValue other)
    {
        return remainder((DoubleValue)other);
    }
    public DoubleValue remainderOf(SpecificDoubleValue other)
    {
        return remainderOf((DoubleValue)other);
    }
    public IntegerValue compare(SpecificDoubleValue other)
    {
        return compare((DoubleValue)other);
    }
    public final IntegerValue compareReverse(SpecificDoubleValue other)
    {
        return compare(other).negate();
    }
    public DoubleValue generalize(ParticularDoubleValue other)
    {
        return generalize((SpecificDoubleValue)other);
    }
    public DoubleValue add(ParticularDoubleValue other)
    {
        return add((SpecificDoubleValue)other);
    }
    public DoubleValue subtract(ParticularDoubleValue other)
    {
        return subtract((SpecificDoubleValue)other);
    }
    public DoubleValue subtractFrom(ParticularDoubleValue other)
    {
        return subtractFrom((SpecificDoubleValue)other);
    }
    public DoubleValue multiply(ParticularDoubleValue other)
    {
        return multiply((SpecificDoubleValue)other);
    }
    public DoubleValue divide(ParticularDoubleValue other)
    {
        return divide((SpecificDoubleValue)other);
    }
    public DoubleValue divideOf(ParticularDoubleValue other)
    {
        return divideOf((SpecificDoubleValue)other);
    }
    public DoubleValue remainder(ParticularDoubleValue other)
    {
        return remainder((SpecificDoubleValue)other);
    }
    public DoubleValue remainderOf(ParticularDoubleValue other)
    {
        return remainderOf((SpecificDoubleValue)other);
    }
    public IntegerValue compare(ParticularDoubleValue other)
    {
        return compare((SpecificDoubleValue)other);
    }
    public final IntegerValue compareReverse(ParticularDoubleValue other)
    {
        return compare(other).negate();
    }
    public final DoubleValue doubleValue()
    {
        return this;
    }
    public Value refresh()
    {
        return this;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.doubleValue());
    }
    public final int computationalType()
    {
        return TYPE_DOUBLE;
    }
    public final String internalType()
    {
        return String.valueOf(ClassConstants.INTERNAL_TYPE_DOUBLE);
    }
}
