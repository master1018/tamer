public class JavaValueArray extends JavaLazyReadObject
                 implements ArrayTypeCodes {
    private static String arrayTypeName(byte sig) {
        switch (sig) {
            case 'B':
                return "byte[]";
            case 'Z':
                return "boolean[]";
            case 'C':
                return "char[]";
            case 'S':
                return "short[]";
            case 'I':
                return "int[]";
            case 'F':
                return "float[]";
            case 'J':
                return "long[]";
            case 'D':
                return "double[]";
            default:
                throw new RuntimeException("invalid array element sig: " + sig);
        }
    }
    private static int elementSize(byte type) {
        switch (type) {
            case T_BYTE:
            case T_BOOLEAN:
                return 1;
            case T_CHAR:
            case T_SHORT:
                return 2;
            case T_INT:
            case T_FLOAT:
                return 4;
            case T_LONG:
            case T_DOUBLE:
                return 8;
            default:
                throw new RuntimeException("invalid array element type: " + type);
        }
    }
    protected final int readValueLength() throws IOException {
        JavaClass cl = getClazz();
        ReadBuffer buf = cl.getReadBuffer();
        int idSize = cl.getIdentifierSize();
        long offset = getOffset() + idSize + 4;
        int len = buf.getInt(offset);
        byte type = buf.getByte(offset + 4);
        return len * elementSize(type);
    }
    protected final byte[] readValue() throws IOException {
        JavaClass cl = getClazz();
        ReadBuffer buf = cl.getReadBuffer();
        int idSize = cl.getIdentifierSize();
        long offset = getOffset() + idSize + 4;
        int length = buf.getInt(offset);
        byte type = buf.getByte(offset + 4);
        if (length == 0) {
            return Snapshot.EMPTY_BYTE_ARRAY;
        } else {
            length *= elementSize(type);
            byte[] res = new byte[length];
            buf.get(offset + 5, res);
            return res;
        }
    }
    private JavaClass clazz;
    private int data;
    private static final int SIGNATURE_MASK = 0x0FF;
    private static final int LENGTH_DIVIDER_MASK = 0x0FF00;
    private static final int LENGTH_DIVIDER_SHIFT = 8;
    public JavaValueArray(byte elementSignature, long offset) {
        super(offset);
        this.data = (elementSignature & SIGNATURE_MASK);
    }
    public JavaClass getClazz() {
        return clazz;
    }
    public void visitReferencedObjects(JavaHeapObjectVisitor v) {
        super.visitReferencedObjects(v);
    }
    public void resolve(Snapshot snapshot) {
        if (clazz instanceof JavaClass) {
            return;
        }
        byte elementSig = getElementType();
        clazz = snapshot.findClass(arrayTypeName(elementSig));
        if (clazz == null) {
            clazz = snapshot.getArrayClass("" + ((char) elementSig));
        }
        getClazz().addInstance(this);
        super.resolve(snapshot);
    }
    public int getLength() {
        int divider = (data & LENGTH_DIVIDER_MASK) >>> LENGTH_DIVIDER_SHIFT;
        if (divider == 0) {
            byte elementSignature = getElementType();
            switch (elementSignature) {
            case 'B':
            case 'Z':
                divider = 1;
                break;
            case 'C':
            case 'S':
                divider = 2;
                break;
            case 'I':
            case 'F':
                divider = 4;
                break;
            case 'J':
            case 'D':
                divider = 8;
                break;
            default:
                throw new RuntimeException("unknown primitive type: " +
                                elementSignature);
            }
            data |= (divider << LENGTH_DIVIDER_SHIFT);
        }
        return (getValueLength() / divider);
    }
    public Object getElements() {
        final int len = getLength();
        final byte et = getElementType();
        byte[] data = getValue();
        int index = 0;
        switch (et) {
            case 'Z': {
                boolean[] res = new boolean[len];
                for (int i = 0; i < len; i++) {
                    res[i] = booleanAt(index, data);
                    index++;
                }
                return res;
            }
            case 'B': {
                byte[] res = new byte[len];
                for (int i = 0; i < len; i++) {
                    res[i] = byteAt(index, data);
                    index++;
                }
                return res;
            }
            case 'C': {
                char[] res = new char[len];
                for (int i = 0; i < len; i++) {
                    res[i] = charAt(index, data);
                    index += 2;
                }
                return res;
            }
            case 'S': {
                short[] res = new short[len];
                for (int i = 0; i < len; i++) {
                    res[i] = shortAt(index, data);
                    index += 2;
                }
                return res;
            }
            case 'I': {
                int[] res = new int[len];
                for (int i = 0; i < len; i++) {
                    res[i] = intAt(index, data);
                    index += 4;
                }
                return res;
            }
            case 'J': {
                long[] res = new long[len];
                for (int i = 0; i < len; i++) {
                    res[i] = longAt(index, data);
                    index += 8;
                }
                return res;
            }
            case 'F': {
                float[] res = new float[len];
                for (int i = 0; i < len; i++) {
                    res[i] = floatAt(index, data);
                    index += 4;
                }
                return res;
            }
            case 'D': {
                double[] res = new double[len];
                for (int i = 0; i < len; i++) {
                    res[i] = doubleAt(index, data);
                    index += 8;
                }
                return res;
            }
            default: {
                throw new RuntimeException("unknown primitive type?");
            }
        }
    }
    public byte getElementType() {
        return (byte) (data & SIGNATURE_MASK);
    }
    private void checkIndex(int index) {
        if (index < 0 || index >= getLength()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }
    private void requireType(char type) {
        if (getElementType() != type) {
            throw new RuntimeException("not of type : " + type);
        }
    }
    public boolean getBooleanAt(int index) {
        checkIndex(index);
        requireType('Z');
        return booleanAt(index, getValue());
    }
    public byte getByteAt(int index) {
        checkIndex(index);
        requireType('B');
        return byteAt(index, getValue());
    }
    public char getCharAt(int index) {
        checkIndex(index);
        requireType('C');
        return charAt(index << 1, getValue());
    }
    public short getShortAt(int index) {
        checkIndex(index);
        requireType('S');
        return shortAt(index << 1, getValue());
    }
    public int getIntAt(int index) {
        checkIndex(index);
        requireType('I');
        return intAt(index << 2, getValue());
    }
    public long getLongAt(int index) {
        checkIndex(index);
        requireType('J');
        return longAt(index << 3, getValue());
    }
    public float getFloatAt(int index) {
        checkIndex(index);
        requireType('F');
        return floatAt(index << 2, getValue());
    }
    public double getDoubleAt(int index) {
        checkIndex(index);
        requireType('D');
        return doubleAt(index << 3, getValue());
    }
    public String valueString() {
        return valueString(true);
    }
    public String valueString(boolean bigLimit) {
        StringBuffer result;
        byte[] value = getValue();
        int max = value.length;
        byte elementSignature = getElementType();
        if (elementSignature == 'C')  {
            result = new StringBuffer();
            for (int i = 0; i < value.length; ) {
                char val = charAt(i, value);
                result.append(val);
                i += 2;
            }
        } else {
            int limit = 8;
            if (bigLimit) {
                limit = 1000;
            }
            result = new StringBuffer("{");
            int num = 0;
            for (int i = 0; i < value.length; ) {
                if (num > 0) {
                    result.append(", ");
                }
                if (num >= limit) {
                    result.append("... ");
                    break;
                }
                num++;
                switch (elementSignature) {
                    case 'Z': {
                        boolean val = booleanAt(i, value);
                        if (val) {
                            result.append("true");
                        } else {
                            result.append("false");
                        }
                        i++;
                        break;
                    }
                    case 'B': {
                        int val = 0xFF & byteAt(i, value);
                        result.append("0x" + Integer.toString(val, 16));
                        i++;
                        break;
                    }
                    case 'S': {
                        short val = shortAt(i, value);
                        i += 2;
                        result.append("" + val);
                        break;
                    }
                    case 'I': {
                        int val = intAt(i, value);
                        i += 4;
                        result.append("" + val);
                        break;
                    }
                    case 'J': {         
                        long val = longAt(i, value);
                        result.append("" + val);
                        i += 8;
                        break;
                    }
                    case 'F': {
                        float val = floatAt(i, value);
                        result.append("" + val);
                        i += 4;
                        break;
                    }
                    case 'D': {         
                        double val = doubleAt(i, value);
                        result.append("" + val);
                        i += 8;
                        break;
                    }
                    default: {
                        throw new RuntimeException("unknown primitive type?");
                    }
                }
            }
            result.append("}");
        }
        return result.toString();
    }
}
