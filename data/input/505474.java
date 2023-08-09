final class ParticularFloatValue extends SpecificFloatValue
{
    private final float value;
    public ParticularFloatValue(float value)
    {
        this.value = value;
    }
    public float value()
    {
        return value;
    }
    public FloatValue negate()
    {
        return new ParticularFloatValue(-value);
    }
    public IntegerValue convertToInteger()
    {
        return new ParticularIntegerValue((int)value);
    }
    public LongValue convertToLong()
    {
        return new ParticularLongValue((long)value);
    }
    public DoubleValue convertToDouble()
    {
        return new ParticularDoubleValue((float)value);
    }
    public FloatValue generalize(FloatValue other)
    {
        return other.generalize(this);
    }
    public FloatValue add(FloatValue other)
    {
        return value == 0.0 ? other : other.add(this);
    }
    public FloatValue subtract(FloatValue other)
    {
        return value == 0.0 ? other.negate() : other.subtractFrom(this);
    }
    public FloatValue subtractFrom(FloatValue other)
    {
        return value == 0.0 ? other : other.subtract(this);
    }
    public FloatValue multiply(FloatValue other)
    {
        return other.multiply(this);
    }
    public FloatValue divide(FloatValue other)
    {
        return other.divideOf(this);
    }
    public FloatValue divideOf(FloatValue other)
    {
        return other.divide(this);
    }
    public FloatValue remainder(FloatValue other)
    {
        return other.remainderOf(this);
    }
    public FloatValue remainderOf(FloatValue other)
    {
        return other.remainder(this);
    }
    public IntegerValue compare(FloatValue other)
    {
        return other.compareReverse(this);
    }
    public FloatValue generalize(ParticularFloatValue other)
    {
        return this.value == other.value ? this : ValueFactory.FLOAT_VALUE;
    }
    public FloatValue add(ParticularFloatValue other)
    {
        return new ParticularFloatValue(this.value + other.value);
    }
    public FloatValue subtract(ParticularFloatValue other)
    {
        return new ParticularFloatValue(this.value - other.value);
    }
    public FloatValue subtractFrom(ParticularFloatValue other)
    {
        return new ParticularFloatValue(other.value - this.value);
    }
    public FloatValue multiply(ParticularFloatValue other)
    {
        return new ParticularFloatValue(this.value * other.value);
    }
    public FloatValue divide(ParticularFloatValue other)
    {
        return new ParticularFloatValue(this.value / other.value);
    }
    public FloatValue divideOf(ParticularFloatValue other)
    {
        return new ParticularFloatValue(other.value / this.value);
    }
    public FloatValue remainder(ParticularFloatValue other)
    {
        return new ParticularFloatValue(this.value % other.value);
    }
    public FloatValue remainderOf(ParticularFloatValue other)
    {
        return new ParticularFloatValue(other.value % this.value);
    }
    public IntegerValue compare(ParticularFloatValue other)
    {
        return this.value <  other.value ? SpecificValueFactory.INTEGER_VALUE_M1 :
               this.value == other.value ? SpecificValueFactory.INTEGER_VALUE_0  :
                                           SpecificValueFactory.INTEGER_VALUE_1;
    }
    public boolean isParticular()
    {
        return true;
    }
    public boolean equals(Object object)
    {
        return super.equals(object) &&
               this.value == ((ParticularFloatValue)object).value;
    }
    public int hashCode()
    {
        return super.hashCode() ^
               Float.floatToIntBits(value);
    }
    public String toString()
    {
        return value+"f";
    }
}