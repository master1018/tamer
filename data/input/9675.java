class UnixOperatingSystem
    extends    sun.management.OperatingSystemImpl
    implements UnixOperatingSystemMXBean {
    UnixOperatingSystem(VMManagement vm) {
        super(vm);
    }
    public native long getCommittedVirtualMemorySize();
    public native long getTotalSwapSpaceSize();
    public native long getFreeSwapSpaceSize();
    public native long getProcessCpuTime();
    public native long getFreePhysicalMemorySize();
    public native long getTotalPhysicalMemorySize();
    public native long getOpenFileDescriptorCount();
    public native long getMaxFileDescriptorCount();
    public native double getSystemCpuLoad();
    public native double getProcessCpuLoad();
    static {
        initialize();
    }
    private static native void initialize();
}
