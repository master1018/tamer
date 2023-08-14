public class PerfByteArrayCounter extends AbstractCounter
       implements ByteArrayCounter {
    ByteBuffer bb;
    PerfByteArrayCounter(String name, Units u, Variability v,
                         int flags, int vectorLength,
                         ByteBuffer bb) {
        super(name, u, v, flags, vectorLength);
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
    public String toString() {
        String result = getName() + ": " + new String(byteArrayValue()) +
                        " " + getUnits();
        if (isInternal()) {
            return result + " [INTERNAL]";
        } else {
            return result;
        }
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
       return new ByteArrayCounterSnapshot(getName(),
                                           getUnits(),
                                           getVariability(),
                                           getFlags(),
                                           getVectorLength(),
                                           byteArrayValue());
    }
    private static final long serialVersionUID = 2545474036937279921L;
}
