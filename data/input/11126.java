public class OperatingSystemImpl implements OperatingSystemMXBean {
    private final VMManagement jvm;
    protected OperatingSystemImpl(VMManagement vm) {
        this.jvm = vm;
    }
    public String getName() {
        return jvm.getOsName();
    }
    public String getArch() {
        return jvm.getOsArch();
    }
    public String getVersion() {
        return jvm.getOsVersion();
    }
    public int getAvailableProcessors() {
        return jvm.getAvailableProcessors();
    }
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private double[] loadavg = new double[1];
    public double getSystemLoadAverage() {
        if (unsafe.getLoadAverage(loadavg, 1) == 1) {
             return loadavg[0];
        } else {
             return -1.0;
        }
    }
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
    }
}
