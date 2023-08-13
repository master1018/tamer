import proguard.classfile.ClassConstants;
public abstract class FloatValue extends Category1Value
{
    public float value()
    {
        return 0f;
    }
    public abstract FloatValue negate();
    public abstract IntegerValue convertToInteger();
    public abstract LongValue convertToLong();
    public abstract DoubleValue convertToDouble();
    public abstract FloatValue generalize(FloatValue other);
    public abstract FloatValue add(FloatValue other);
    public abstract FloatValue subtract(FloatValue other);
    public abstract FloatValue subtractFrom(FloatValue other);
    public abstract FloatValue multiply(FloatValue other);
    public abstract FloatValue divide(FloatValue other);
    public abstract FloatValue divideOf(FloatValue other);
    public abstract FloatValue remainder(FloatValue other);
    public abstract FloatValue remainderOf(FloatValue other);
    public abstract IntegerValue compare(FloatValue other);
    public final IntegerValue compareReverse(FloatValue other)
    {
        return compare(other).negate();
    }
    public FloatValue generalize(SpecificFloatValue other)
    {
        return generalize((FloatValue)other);
    }
    public FloatValue add(SpecificFloatValue other)
    {
        return add((FloatValue)other);
    }
    public FloatValue subtract(SpecificFloatValue other)
    {
        return subtract((FloatValue)other);
    }
    public FloatValue subtractFrom(SpecificFloatValue other)
    {
        return subtractFrom((FloatValue)other);
    }
    public FloatValue multiply(SpecificFloatValue other)
    {
        return multiply((FloatValue)other);
    }
    public FloatValue divide(SpecificFloatValue other)
    {
        return divide((FloatValue)other);
    }
    public FloatValue divideOf(SpecificFloatValue other)
    {
        return divideOf((FloatValue)other);
    }
    public FloatValue remainder(SpecificFloatValue other)
    {
        return remainder((FloatValue)other);
    }
    public FloatValue remainderOf(SpecificFloatValue other)
    {
        return remainderOf((FloatValue)other);
    }
    public IntegerValue compare(SpecificFloatValue other)
    {
        return compare((FloatValue)other);
    }
    public final IntegerValue compareReverse(SpecificFloatValue other)
    {
        return compare(other).negate();
    }
    public FloatValue generalize(ParticularFloatValue other)
    {
        return generalize((SpecificFloatValue)other);
    }
    public FloatValue add(ParticularFloatValue other)
    {
        return add((SpecificFloatValue)other);
    }
    public FloatValue subtract(ParticularFloatValue other)
    {
        return subtract((SpecificFloatValue)other);
    }
    public FloatValue subtractFrom(ParticularFloatValue other)
    {
        return subtractFrom((SpecificFloatValue)other);
    }
    public FloatValue multiply(ParticularFloatValue other)
    {
        return multiply((SpecificFloatValue)other);
    }
    public FloatValue divide(ParticularFloatValue other)
    {
        return divide((SpecificFloatValue)other);
    }
    public FloatValue divideOf(ParticularFloatValue other)
    {
        return divideOf((SpecificFloatValue)other);
    }
    public FloatValue remainder(ParticularFloatValue other)
    {
        return remainder((SpecificFloatValue)other);
    }
    public FloatValue remainderOf(ParticularFloatValue other)
    {
        return remainderOf((SpecificFloatValue)other);
    }
    public IntegerValue compare(ParticularFloatValue other)
    {
        return compare((SpecificFloatValue)other);
    }
    public final IntegerValue compareReverse(ParticularFloatValue other)
    {
        return compare(other).negate();
    }
    public final FloatValue floatValue()
    {
        return this;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.floatValue());
    }
    public final int computationalType()
    {
        return TYPE_FLOAT;
    }
    public final String internalType()
    {
        return String.valueOf(ClassConstants.INTERNAL_TYPE_FLOAT);
    }
}
