abstract class SpecificDoubleValue extends DoubleValue
{
    public DoubleValue negate()
    {
        return new NegatedDoubleValue(this);
    }
    public IntegerValue convertToInteger()
    {
        return new ConvertedIntegerValue(this);
    }
    public LongValue convertToLong()
    {
        return new ConvertedLongValue(this);
    }
    public FloatValue convertToFloat()
    {
        return new ConvertedFloatValue(this);
    }
    public DoubleValue generalize(DoubleValue other)
    {
        return other.generalize(this);
    }
    public DoubleValue add(DoubleValue other)
    {
        return other.add(this);
    }
    public DoubleValue subtract(DoubleValue other)
    {
        return other.subtractFrom(this);
    }
    public DoubleValue subtractFrom(DoubleValue other)
    {
        return other.subtract(this);
    }
    public DoubleValue multiply(DoubleValue other)
    {
        return other.multiply(this);
    }
    public DoubleValue divide(DoubleValue other)
    {
        return other.divideOf(this);
    }
    public DoubleValue divideOf(DoubleValue other)
    {
        return other.divide(this);
    }
    public DoubleValue remainder(DoubleValue other)
    {
        return other.remainderOf(this);
    }
    public DoubleValue remainderOf(DoubleValue other)
    {
        return other.remainder(this);
    }
    public IntegerValue compare(DoubleValue other)
    {
        return other.compareReverse(this);
    }
    public DoubleValue generalize(SpecificDoubleValue other)
    {
        return this.equals(other) ? this : ValueFactory.DOUBLE_VALUE;
    }
    public DoubleValue add(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(this, CompositeDoubleValue.ADD, other);
    }
    public DoubleValue subtract(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(this, CompositeDoubleValue.SUBTRACT, other);
    }
    public DoubleValue subtractFrom(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(other, CompositeDoubleValue.SUBTRACT, this);
    }
    public DoubleValue multiply(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(this, CompositeDoubleValue.MULTIPLY, other);
    }
    public DoubleValue divide(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(this, CompositeDoubleValue.DIVIDE, other);
    }
    public DoubleValue divideOf(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(other, CompositeDoubleValue.DIVIDE, this);
    }
    public DoubleValue remainder(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(this, CompositeDoubleValue.REMAINDER, other);
    }
    public DoubleValue remainderOf(SpecificDoubleValue other)
    {
        return new CompositeDoubleValue(other, CompositeDoubleValue.REMAINDER, this);
    }
    public IntegerValue compare(SpecificDoubleValue other)
    {
        return this.equals(other) ?
            SpecificValueFactory.INTEGER_VALUE_0 :
            new ComparisonValue(this, other);
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
