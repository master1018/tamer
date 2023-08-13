final class CompositeDoubleValue extends SpecificDoubleValue
{
    public static final byte ADD       = '+';
    public static final byte SUBTRACT  = '-';
    public static final byte MULTIPLY  = '*';
    public static final byte DIVIDE    = '/';
    public static final byte REMAINDER = '%';
    private final DoubleValue doubleValue1;
    private final byte        operation;
    private final DoubleValue doubleValue2;
    public CompositeDoubleValue(DoubleValue doubleValue1,
                                byte        operation,
                                DoubleValue doubleValue2)
    {
        this.doubleValue1 = doubleValue1;
        this.operation    = operation;
        this.doubleValue2 = doubleValue2;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.doubleValue1.equals(((CompositeDoubleValue)object).doubleValue1) &&
               this.operation        == ((CompositeDoubleValue)object).operation     &&
               this.doubleValue2.equals(((CompositeDoubleValue)object).doubleValue2);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               doubleValue1.hashCode() ^
               doubleValue2.hashCode();
    }
    public String toString()
    {
        return "("+doubleValue1+((char)operation)+doubleValue2+")";
    }
}