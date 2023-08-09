public class XcbRequestCounter {
    private final static long MAX_UINT = 4294967295L;
    long value;
    public XcbRequestCounter(long value) {
        this.value = value;
    }
    public void setValue(long value) {
        this.value = value;
    }
    public long getValue() {
        return value;
    }
    public void add(long v) {
        value += v;
        if (value > MAX_UINT) {
            value = 0; 
        }
    }
}
