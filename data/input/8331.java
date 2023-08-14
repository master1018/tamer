class HotspotThread
    implements HotspotThreadMBean {
    private VMManagement jvm;
    HotspotThread(VMManagement vm) {
        jvm = vm;
    }
    public native int getInternalThreadCount();
    public Map<String, Long> getInternalThreadCpuTimes() {
        int count = getInternalThreadCount();
        if (count == 0) {
            return java.util.Collections.emptyMap();
        }
        String[] names = new String[count];
        long[] times = new long[count];
        int numThreads = getInternalThreadTimes0(names, times);
        Map<String, Long> result = new HashMap<String, Long>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            result.put(names[i], new Long(times[i]));
        }
        return result;
    }
    public native int getInternalThreadTimes0(String[] names, long[] times);
    private static final String JAVA_THREADS    = "java.threads.";
    private static final String COM_SUN_THREADS = "com.sun.threads.";
    private static final String SUN_THREADS     = "sun.threads.";
    private static final String THREADS_COUNTER_NAME_PATTERN =
        JAVA_THREADS + "|" + COM_SUN_THREADS + "|" + SUN_THREADS;
    public java.util.List<Counter> getInternalThreadingCounters() {
        return jvm.getInternalCounters(THREADS_COUNTER_NAME_PATTERN);
    }
}
