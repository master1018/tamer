public class GetTotalSafepointTime {
    private static HotspotRuntimeMBean mbean =
        (HotspotRuntimeMBean)ManagementFactoryHelper.getHotspotRuntimeMBean();
    private static final long NUM_THREAD_DUMPS = 100;
    private static final long MIN_VALUE_FOR_PASS = 1;
    private static final long MAX_VALUE_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        for (int i = 0; i < NUM_THREAD_DUMPS; i++) {
             Thread.getAllStackTraces();
        }
        long value = mbean.getTotalSafepointTime();
        if (trace) {
            System.out.println("Total safepoint time (ms): " + value);
        }
        if (value < MIN_VALUE_FOR_PASS || value > MAX_VALUE_FOR_PASS) {
            throw new RuntimeException("Total safepoint time " +
                                       "illegal value: " + value + " ms " +
                                       "(MIN = " + MIN_VALUE_FOR_PASS + "; " +
                                       "MAX = " + MAX_VALUE_FOR_PASS + ")");
        }
        for (int i = 0; i < 2 * NUM_THREAD_DUMPS; i++) {
             Thread.getAllStackTraces();
        }
        long value2 = mbean.getTotalSafepointTime();
        if (trace) {
            System.out.println("Total safepoint time2 (ms): " + value2);
        }
        if (value2 <= value) {
            throw new RuntimeException("Total safepoint time " +
                                       "did not increase " +
                                       "(value = " + value + "; " +
                                       "value2 = " + value2 + ")");
        }
        System.out.println("Test passed.");
    }
}
