public class ClassFile {
    public final static int JAVA_MAGIC = 0xCAFEBABE;
    public final static int CONSTANT_Utf8 = 1;
    public final static int CONSTANT_Unicode = 2;
    public final static int CONSTANT_Integer = 3;
    public final static int CONSTANT_Float = 4;
    public final static int CONSTANT_Long = 5;
    public final static int CONSTANT_Double = 6;
    public final static int CONSTANT_Class = 7;
    public final static int CONSTANT_String = 8;
    public final static int CONSTANT_Fieldref = 9;
    public final static int CONSTANT_Methodref = 10;
    public final static int CONSTANT_InterfaceMethodref = 11;
    public final static int CONSTANT_NameandType = 12;
    public final static int CONSTANT_MethodHandle = 15;
    public final static int CONSTANT_MethodType = 16;
    public final static int CONSTANT_InvokeDynamic = 18;
    public final static int MAX_PARAMETERS = 0xff;
    public final static int MAX_DIMENSIONS = 0xff;
    public final static int MAX_CODE = 0xffff;
    public final static int MAX_LOCALS = 0xffff;
    public final static int MAX_STACK = 0xffff;
    public enum Version {
        V45_3(45, 3), 
        V49(49, 0),   
        V50(50, 0),   
        V51(51, 0);   
        Version(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }
        public final int major, minor;
    }
    public static byte[] internalize(byte[] buf, int offset, int len) {
        byte[] translated = new byte[len];
        for (int j = 0; j < len; j++) {
            byte b = buf[offset + j];
            if (b == '/') translated[j] = (byte) '.';
            else translated[j] = b;
        }
        return translated;
    }
    public static byte[] internalize(Name name) {
        return internalize(name.getByteArray(), name.getByteOffset(), name.getByteLength());
    }
    public static byte[] externalize(byte[] buf, int offset, int len) {
        byte[] translated = new byte[len];
        for (int j = 0; j < len; j++) {
            byte b = buf[offset + j];
            if (b == '.') translated[j] = (byte) '/';
            else translated[j] = b;
        }
        return translated;
    }
    public static byte[] externalize(Name name) {
        return externalize(name.getByteArray(), name.getByteOffset(), name.getByteLength());
    }
    public static class NameAndType {
        Name name;
        Type type;
        NameAndType(Name name, Type type) {
            this.name = name;
            this.type = type;
        }
        public boolean equals(Object other) {
            return
                other instanceof NameAndType &&
                name == ((NameAndType) other).name &&
                type.equals(((NameAndType) other).type);
        }
        public int hashCode() {
            return name.hashCode() * type.hashCode();
        }
    }
}
