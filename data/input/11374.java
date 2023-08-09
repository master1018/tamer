public class GetInternalThreads {
    private static HotspotThreadMBean mbean =
        ManagementFactoryHelper.getHotspotThreadMBean();
    private static final long MIN_VALUE_FOR_PASS = 4;
    private static final long MAX_VALUE_FOR_PASS = Long.MAX_VALUE;
    public static void main(String[] args) {
        long value = mbean.getInternalThreadCount();
        if (value < MIN_VALUE_FOR_PASS || value > MAX_VALUE_FOR_PASS) {
            throw new RuntimeException("Internal thread count " +
                                       "illegal value: " + value + " " +
                                       "(MIN = " + MIN_VALUE_FOR_PASS + "; " +
                                       "MAX = " + MAX_VALUE_FOR_PASS + ")");
        }
        System.out.println("Internal Thread Count = " + value);
        ThreadMXBean thread =
            ManagementFactory.getThreadMXBean();
        if (!thread.isThreadCpuTimeSupported()) {
            System.out.println("Thread Cpu Time is not supported.");
            return;
        }
        Map times = mbean.getInternalThreadCpuTimes();
        Iterator iter = times.entrySet().iterator();
        for (; iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            System.out.println(entry.getKey() +
                " CPU time = " + entry.getValue() + "ns");
            if (((Long) entry.getValue()).longValue() < 0) {
                throw new RuntimeException("Thread CPU time" +
                    "illegal value: " + entry.getValue());
            }
        }
        System.out.println("Test passed.");
    }
}
