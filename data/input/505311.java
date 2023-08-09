final class CompositeFloatValue extends SpecificFloatValue
{
    public static final byte ADD       = '+';
    public static final byte SUBTRACT  = '-';
    public static final byte MULTIPLY  = '*';
    public static final byte DIVIDE    = '/';
    public static final byte REMAINDER = '%';
    private final FloatValue floatValue1;
    private final byte        operation;
    private final FloatValue floatValue2;
    public CompositeFloatValue(FloatValue floatValue1,
                                byte        operation,
                                FloatValue floatValue2)
    {
        this.floatValue1 = floatValue1;
        this.operation    = operation;
        this.floatValue2 = floatValue2;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.floatValue1.equals(((CompositeFloatValue)object).floatValue1) &&
               this.operation       == ((CompositeFloatValue)object).operation    &&
               this.floatValue2.equals(((CompositeFloatValue)object).floatValue2);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               floatValue1.hashCode() ^
               floatValue2.hashCode();
    }
    public String toString()
    {
        return "("+floatValue1+((char)operation)+floatValue2+")";
    }
}