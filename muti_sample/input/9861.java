public class PerfLongArrayCounter extends AbstractCounter
       implements LongArrayCounter {
    LongBuffer lb;
    PerfLongArrayCounter(String name, Units u, Variability v,
                         int flags, int vectorLength,
                         LongBuffer lb) {
        super(name, u, v, flags, vectorLength);
        this.lb = lb;
    }
    public Object getValue() {
        return longArrayValue();
    }
    public long[] longArrayValue() {
        lb.position(0);
        long[] l = new long[lb.limit()];
        lb.get(l);
        return l;
    }
    public long longAt(int index) {
        lb.position(index);
        return lb.get();
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new LongArrayCounterSnapshot(getName(),
                                            getUnits(),
                                            getVariability(),
                                            getFlags(),
                                            getVectorLength(),
                                            longArrayValue());
    }
    private static final long serialVersionUID = -2733617913045487126L;
}
