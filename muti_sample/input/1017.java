class HotspotMemory
    implements HotspotMemoryMBean {
    private VMManagement jvm;
    HotspotMemory(VMManagement vm) {
        jvm = vm;
    }
    private static final String JAVA_GC    = "java.gc.";
    private static final String COM_SUN_GC = "com.sun.gc.";
    private static final String SUN_GC     = "sun.gc.";
    private static final String GC_COUNTER_NAME_PATTERN =
        JAVA_GC + "|" + COM_SUN_GC + "|" + SUN_GC;
    public java.util.List<Counter> getInternalMemoryCounters() {
        return jvm.getInternalCounters(GC_COUNTER_NAME_PATTERN);
    }
}
