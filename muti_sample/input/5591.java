public class PerfLongCounter extends AbstractCounter
       implements LongCounter {
    LongBuffer lb;
    PerfLongCounter(String name, Units u, Variability v, int flags,
                    LongBuffer lb) {
        super(name, u, v, flags);
        this.lb = lb;
    }
    public Object getValue() {
        return new Long(lb.get(0));
    }
    public long longValue() {
        return lb.get(0);
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new LongCounterSnapshot(getName(),
                                       getUnits(),
                                       getVariability(),
                                       getFlags(),
                                       longValue());
    }
    private static final long serialVersionUID = 857711729279242948L;
}
