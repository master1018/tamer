public class PerfCounter {
    private static final Perf perf =
        AccessController.doPrivileged(new Perf.GetPerfAction());
    private final static int V_Constant  = 1;
    private final static int V_Monotonic = 2;
    private final static int V_Variable  = 3;
    private final static int U_None      = 1;
    private final String name;
    private final LongBuffer lb;
    private PerfCounter(String name, int type) {
        this.name = name;
        ByteBuffer bb = perf.createLong(name, U_None, type, 0L);
        bb.order(ByteOrder.nativeOrder());
        this.lb = bb.asLongBuffer();
    }
    static PerfCounter newPerfCounter(String name) {
        return new PerfCounter(name, V_Variable);
    }
    static PerfCounter newConstantPerfCounter(String name) {
        PerfCounter c = new PerfCounter(name, V_Constant);
        return c;
    }
    public synchronized long get() {
        return lb.get(0);
    }
    public synchronized void set(long newValue) {
        lb.put(0, newValue);
    }
    public synchronized void add(long value) {
        long res = get() + value;
        lb.put(0, res);
    }
    public void increment() {
        add(1);
    }
    public void addTime(long interval) {
        add(interval);
    }
    public void addElapsedTimeFrom(long startTime) {
        add(System.nanoTime() - startTime);
    }
    @Override
    public String toString() {
        return name + " = " + get();
    }
    static class CoreCounters {
        static final PerfCounter pdt   = newPerfCounter("sun.classloader.parentDelegationTime");
        static final PerfCounter lc    = newPerfCounter("sun.classloader.findClasses");
        static final PerfCounter lct   = newPerfCounter("sun.classloader.findClassTime");
        static final PerfCounter rcbt  = newPerfCounter("sun.urlClassLoader.readClassBytesTime");
        static final PerfCounter zfc   = newPerfCounter("sun.zip.zipFiles");
        static final PerfCounter zfot  = newPerfCounter("sun.zip.zipFile.openTime");
    }
    static class WindowsClientCounters {
        static final PerfCounter d3dAvailable = newConstantPerfCounter("sun.java2d.d3d.available");
    }
    public static PerfCounter getFindClasses() {
        return CoreCounters.lc;
    }
    public static PerfCounter getFindClassTime() {
        return CoreCounters.lct;
    }
    public static PerfCounter getReadClassBytesTime() {
        return CoreCounters.rcbt;
    }
    public static PerfCounter getParentDelegationTime() {
        return CoreCounters.pdt;
    }
    public static PerfCounter getZipFileCount() {
        return CoreCounters.zfc;
    }
    public static PerfCounter getZipFileOpenTime() {
        return CoreCounters.zfot;
    }
    public static PerfCounter getD3DAvailable() {
        return WindowsClientCounters.d3dAvailable;
    }
}
