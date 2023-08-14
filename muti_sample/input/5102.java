public class PerfStringMonitor extends PerfByteArrayMonitor
       implements StringMonitor {
    private static Charset defaultCharset = Charset.defaultCharset();
    public PerfStringMonitor(String name, Variability v, boolean supported,
                             ByteBuffer bb) {
        this(name, v, supported, bb, bb.limit());
    }
    public PerfStringMonitor(String name, Variability v, boolean supported,
                             ByteBuffer bb, int maxLength) {
        super(name, Units.STRING, v, supported, bb, maxLength);
    }
    public Object getValue() {
        return stringValue();
    }
    public String stringValue() {
        String str = "";
        byte[] b = byteArrayValue();
        if ((b == null) || (b.length <= 1) || (b[0] == (byte)0)) {
            return str;
        }
        int i;
        for (i = 0; i < b.length && b[i] != (byte)0; i++);
        return new String(b, 0, i, defaultCharset);
    }
}
