public class PerfStringVariableMonitor extends PerfStringMonitor {
    public PerfStringVariableMonitor(String name, boolean supported,
                                     ByteBuffer bb) {
        this(name, supported, bb, bb.limit());
    }
    public PerfStringVariableMonitor(String name, boolean supported,
                                     ByteBuffer bb, int maxLength) {
        super(name, Variability.VARIABLE, supported, bb, maxLength+1);
    }
}
