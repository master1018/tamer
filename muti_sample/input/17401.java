public class LongValueImpl extends PrimitiveValueImpl
                           implements LongValue {
    private long value;
    LongValueImpl(VirtualMachine aVm,long aValue) {
        super(aVm);
        value = aValue;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof LongValue)) {
            return (value == ((LongValue)obj).value()) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return intValue();
    }
    public int compareTo(LongValue longVal) {
        long other = longVal.value();
        if (value() < other) {
            return -1;
        } else if (value() == other) {
            return 0;
        } else {
            return 1;
        }
    }
    public Type type() {
        return vm.theLongType();
    }
    public long value() {
        return value;
    }
    public boolean booleanValue() {
        return(value == 0)?false:true;
    }
    public byte byteValue() {
        return(byte)value;
    }
    public char charValue() {
        return(char)value;
    }
    public short shortValue() {
        return(short)value;
    }
    public int intValue() {
        return(int)value;
    }
    public long longValue() {
        return(long)value;
    }
    public float floatValue() {
        return(float)value;
    }
    public double doubleValue() {
        return(double)value;
    }
    byte checkedByteValue() throws InvalidTypeException {
        if ((value > Byte.MAX_VALUE) || (value < Byte.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to byte");
        } else {
            return super.checkedByteValue();
        }
    }
    char checkedCharValue() throws InvalidTypeException {
        if ((value > Character.MAX_VALUE) || (value < Character.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to char");
        } else {
            return super.checkedCharValue();
        }
    }
    short checkedShortValue() throws InvalidTypeException {
        if ((value > Short.MAX_VALUE) || (value < Short.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to short");
        } else {
            return super.checkedShortValue();
        }
    }
    int checkedIntValue() throws InvalidTypeException {
        if ((value > Integer.MAX_VALUE) || (value < Integer.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to int");
        } else {
            return super.checkedIntValue();
        }
    }
    public String toString() {
        return "" + value;
    }
}
