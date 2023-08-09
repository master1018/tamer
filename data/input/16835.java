public class PerfByteArrayMonitor extends AbstractMonitor
       implements ByteArrayMonitor {
    ByteBuffer bb;
    public PerfByteArrayMonitor(String name, Units u, Variability v,
                                boolean supported, ByteBuffer bb,
                                int vectorLength) {
        super(name, u, v, supported, vectorLength);
        this.bb = bb;
    }
    public Object getValue() {
        return byteArrayValue();
    }
    public byte[] byteArrayValue() {
        bb.position(0);
        byte[] b = new byte[bb.limit()];
        bb.get(b);
        return b;
    }
    public byte byteAt(int index) {
        bb.position(index);
        return bb.get();
    }
    public int getMaximumLength() {
        return bb.limit();
    }
}
