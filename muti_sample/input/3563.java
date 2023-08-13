public class PerfStringConstantMonitor extends PerfStringMonitor {
    String data;
    public PerfStringConstantMonitor(String name, boolean supported,
                                     ByteBuffer bb) {
        super(name, Variability.CONSTANT, supported, bb);
        this.data = super.stringValue();
    }
    public Object getValue() {
        return data;        
    }
    public String stringValue() {
        return data;        
    }
}
