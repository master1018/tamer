class HotspotRuntime
    implements HotspotRuntimeMBean {
    private VMManagement jvm;
    HotspotRuntime(VMManagement vm) {
        jvm = vm;
    }
    public long getSafepointCount() {
        return jvm.getSafepointCount();
    }
    public long getTotalSafepointTime() {
        return jvm.getTotalSafepointTime();
    }
    public long getSafepointSyncTime() {
        return jvm.getSafepointSyncTime();
    }
    private static final String JAVA_RT          = "java.rt.";
    private static final String COM_SUN_RT       = "com.sun.rt.";
    private static final String SUN_RT           = "sun.rt.";
    private static final String JAVA_PROPERTY    = "java.property.";
    private static final String COM_SUN_PROPERTY = "com.sun.property.";
    private static final String SUN_PROPERTY     = "sun.property.";
    private static final String RT_COUNTER_NAME_PATTERN =
        JAVA_RT + "|" + COM_SUN_RT + "|" + SUN_RT + "|" +
        JAVA_PROPERTY + "|" + COM_SUN_PROPERTY + "|" + SUN_PROPERTY;
    public java.util.List<Counter> getInternalRuntimeCounters() {
        return jvm.getInternalCounters(RT_COUNTER_NAME_PATTERN);
    }
}
