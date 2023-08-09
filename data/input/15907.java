public class CharValueImpl extends PrimitiveValueImpl
                           implements CharValue {
    private char value;
    CharValueImpl(VirtualMachine aVm,char aValue) {
        super(aVm);
        value = aValue;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof CharValue)) {
            return (value == ((CharValue)obj).value()) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }
    public int hashCode() {
        return intValue();
    }
    public int compareTo(CharValue charVal) {
        return value() - charVal.value();
    }
    public Type type() {
        return vm.theCharType();
    }
    public char value() {
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
    public String toString() {
        return "" + value;
    }
    byte checkedByteValue() throws InvalidTypeException {
        if (value > Byte.MAX_VALUE) {
            throw new InvalidTypeException("Can't convert " + value + " to byte");
        } else {
            return super.checkedByteValue();
        }
    }
    short checkedShortValue() throws InvalidTypeException {
        if (value > Short.MAX_VALUE) {
            throw new InvalidTypeException("Can't convert " + value + " to short");
        } else {
            return super.checkedShortValue();
        }
    }
}
