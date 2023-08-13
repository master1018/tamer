public abstract class Value
{
    public static final int NEVER  = -1;
    public static final int MAYBE  = 0;
    public static final int ALWAYS = 1;
    public static final int TYPE_INTEGER            = 1;
    public static final int TYPE_LONG               = 2;
    public static final int TYPE_FLOAT              = 3;
    public static final int TYPE_DOUBLE             = 4;
    public static final int TYPE_REFERENCE          = 5;
    public static final int TYPE_INSTRUCTION_OFFSET = 6;
    public static final int TYPE_TOP                = 7;
    public Category1Value category1Value()
    {
        throw new IllegalArgumentException("Value is not a Category 1 value [" + this.getClass().getName() + "]");
    }
    public Category2Value category2Value()
    {
        throw new IllegalArgumentException("Value is not a Category 2 value [" + this.getClass().getName() + "]");
    }
    public IntegerValue integerValue()
    {
        throw new IllegalArgumentException("Value is not an integer value [" + this.getClass().getName() + "]");
    }
    public LongValue longValue()
    {
        throw new IllegalArgumentException("Value is not a long value [" + this.getClass().getName() + "]");
    }
    public FloatValue floatValue()
    {
        throw new IllegalArgumentException("Value is not a float value [" + this.getClass().getName() + "]");
    }
    public DoubleValue doubleValue()
    {
        throw new IllegalArgumentException("Value is not a double value [" + this.getClass().getName() + "]");
    }
    public ReferenceValue referenceValue()
    {
        throw new IllegalArgumentException("Value is not a reference value [" + this.getClass().getName() + "]");
    }
    public InstructionOffsetValue instructionOffsetValue()
    {
        throw new IllegalArgumentException("Value is not an instruction offset value [" + this.getClass().getName() + "]");
    }
    public boolean isSpecific()
    {
        return false;
    }
    public boolean isParticular()
    {
        return false;
    }
    public abstract Value generalize(Value other);
    public abstract boolean isCategory2();
    public abstract int computationalType();
    public abstract String internalType();
}
