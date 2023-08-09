public class PerfLongMonitor extends AbstractMonitor implements LongMonitor {
    LongBuffer lb;
    public PerfLongMonitor(String name, Units u, Variability v,
                           boolean supported, LongBuffer lb) {
        super(name, u, v, supported);
        this.lb = lb;
    }
    public Object getValue() {
        return new Long(lb.get(0));
    }
    public long longValue() {
        return lb.get(0);
    }
}
