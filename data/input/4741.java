public class PerfIntegerMonitor extends AbstractMonitor
                                implements IntegerMonitor {
    IntBuffer ib;
    public PerfIntegerMonitor(String name, Units u, Variability v,
                              boolean supported, IntBuffer ib) {
        super(name, u, v, supported);
        this.ib = ib;
    }
    public Object getValue() {
        return new Integer(ib.get(0));
    }
    public int intValue() {
        return ib.get(0);
    }
}
