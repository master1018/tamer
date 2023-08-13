public class AttributeClass {
    private String myName;
    private int myType;
    private int nameLen;
    private Object myValue;
    public static final int TAG_UNSUPPORTED_VALUE = 0x10;
    public static final int TAG_INT = 0x21;
    public static final int TAG_BOOL = 0x22;
    public static final int TAG_ENUM = 0x23;
    public static final int TAG_OCTET = 0x30;
    public static final int TAG_DATE = 0x31;
    public static final int TAG_RESOLUTION = 0x32;
    public static final int TAG_RANGE_INTEGER = 0x33;
    public static final int TAG_TEXT_LANGUAGE = 0x35;
    public static final int TAG_NAME_LANGUAGE = 0x36;
    public static final int TAG_TEXT_WO_LANGUAGE = 0x41;
    public static final int TAG_NAME_WO_LANGUAGE = 0x42;
    public static final int TAG_KEYWORD = 0x44;
    public static final int TAG_URI = 0x45;
    public static final int TAG_CHARSET = 0x47;
    public static final int TAG_NATURALLANGUAGE = 0x48;
    public static final int TAG_MIME_MEDIATYPE = 0x49;
    public static final int TAG_MEMBER_ATTRNAME = 0x4A;
    public static final AttributeClass ATTRIBUTES_CHARSET =
        new AttributeClass("attributes-charset",
                           TAG_CHARSET, "utf-8");
    public static final AttributeClass ATTRIBUTES_NATURAL_LANGUAGE =
        new AttributeClass("attributes-natural-language",
                           TAG_NATURALLANGUAGE, "en");
    protected AttributeClass(String name, int type, Object value) {
        myName = name;
        myType = type;
        nameLen = name.length();
        myValue = value;
    }
    public byte getType() {
        return (byte)myType;
    }
    public char[] getLenChars() {
        char[] chars = new char[2];
        chars[0] = 0;
        chars[1] = (char)nameLen;
        return chars;
    }
    public Object getObjectValue() {
        return myValue;
    }
    public int getIntValue() {
        byte[] bufArray = (byte[])myValue;
        if (bufArray != null) {
            byte[] buf = new byte[4];
            for (int i=0; i<4; i++) {
                buf[i] = bufArray[i+1];
            }
            return convertToInt(buf);
        }
        return 0;
    }
    public int[] getArrayOfIntValues() {
        byte[] bufArray = (byte[])myValue;
        if (bufArray != null) {
            ByteArrayInputStream bufStream =
                new ByteArrayInputStream(bufArray);
            int available = bufStream.available();
            bufStream.mark(available);
            bufStream.skip(available-1);
            int length = bufStream.read();
            bufStream.reset();
            int[] valueArray = new int[length];
            for (int i = 0; i < length; i++) {
                int valLength = bufStream.read();
                if (valLength != 4) {
                    return null;
                }
                byte[] bufBytes = new byte[valLength];
                bufStream.read(bufBytes, 0, valLength);
                valueArray[i] = convertToInt(bufBytes);
            }
            return valueArray;
        }
        return null;
    }
    public int[] getIntRangeValue() {
        int[] range = {0, 0};
        byte[] bufArray = (byte[])myValue;
        if (bufArray != null) {
            int nBytes = 4; 
            for (int j=0; j<2; j++) { 
                byte[] intBytes = new byte[nBytes];
                for (int i=0; i< nBytes; i++) {
                    intBytes[i] = bufArray[i+(4*j)+1];
                }
                range[j] = convertToInt(intBytes);
            }
        }
        return range;
    }
    public String getStringValue() {
        String strVal = null;
        byte[] bufArray = (byte[])myValue;
        if (bufArray != null) {
            ByteArrayInputStream bufStream =
                new ByteArrayInputStream(bufArray);
            int valLength = bufStream.read();
            byte[] strBytes = new byte[valLength];
            bufStream.read(strBytes, 0, valLength);
            try {
                strVal = new String(strBytes, "UTF-8");
            } catch (java.io.UnsupportedEncodingException uee) {
            }
        }
        return strVal;
    }
    public String[] getArrayOfStringValues() {
        byte[] bufArray = (byte[])myValue;
        if (bufArray != null) {
            ByteArrayInputStream bufStream =
                new ByteArrayInputStream(bufArray);
            int available = bufStream.available();
            bufStream.mark(available);
            bufStream.skip(available-1);
            int length = bufStream.read();
            bufStream.reset();
            String[] valueArray = new String[length];
            for (int i = 0; i < length; i++) {
                int valLength = bufStream.read();
                byte[] bufBytes = new byte[valLength];
                bufStream.read(bufBytes, 0, valLength);
                try {
                    valueArray[i] = new String(bufBytes, "UTF-8");
                } catch (java.io.UnsupportedEncodingException uee) {
                }
            }
            return valueArray;
        }
        return null;
    }
    public byte getByteValue() {
        byte[] bufArray = (byte[])myValue;
        if ((bufArray != null) && (bufArray.length>=2)) {
            return bufArray[1];
        }
        return 0;
    }
    public String getName() {
        return myName;
    }
    public boolean equals(Object obj) {
        return
            obj != null &&
            obj instanceof AttributeClass &&
            obj.toString().equals (((AttributeClass) obj).toString());
    }
    public String toString() {
        return myName;
    }
    private int unsignedByteToInt(byte b) {
        return (int) (b & 0xff);
    }
    private int convertToInt(byte[] buf) {
        int intVal = 0;
        int pos = 0;
        intVal+= unsignedByteToInt(buf[pos++]) << 24;
        intVal+= unsignedByteToInt(buf[pos++]) << 16;
        intVal+= unsignedByteToInt(buf[pos++]) << 8;
        intVal+= unsignedByteToInt(buf[pos++]) << 0;
        return intVal;
    }
}
