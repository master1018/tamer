final class CompositeIntegerValue extends SpecificIntegerValue
{
    public static final byte ADD                  = '+';
    public static final byte SUBTRACT             = '-';
    public static final byte MULTIPLY             = '*';
    public static final byte DIVIDE               = '/';
    public static final byte REMAINDER            = '%';
    public static final byte SHIFT_LEFT           = '<';
    public static final byte SHIFT_RIGHT          = '>';
    public static final byte UNSIGNED_SHIFT_RIGHT = '}';
    public static final byte AND                  = '&';
    public static final byte OR                   = '|';
    public static final byte XOR                  = '^';
    private final IntegerValue integerValue1;
    private final byte         operation;
    private final IntegerValue integerValue2;
    public CompositeIntegerValue(IntegerValue integerValue1,
                                 byte         operation,
                                 IntegerValue integerValue2)
    {
        this.integerValue1 = integerValue1;
        this.operation     = operation;
        this.integerValue2 = integerValue2;
    }
    public boolean equals(Object object)
    {
        return this == object ||
               super.equals(object) &&
               this.integerValue1.equals(((CompositeIntegerValue)object).integerValue1) &&
               this.operation         == ((CompositeIntegerValue)object).operation      &&
               this.integerValue2.equals(((CompositeIntegerValue)object).integerValue2);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               integerValue1.hashCode() ^
               integerValue2.hashCode();
    }
    public String toString()
    {
        return "("+integerValue1+((char)operation)+integerValue2+")";
    }
}