public class GetOpenFileDescriptorCount {
    private static UnixOperatingSystemMXBean mbean =
        (UnixOperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    private static final long MIN_COUNT_FOR_PASS = 1;
    private static long       max_count_for_pass = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long max_count = mbean.getMaxFileDescriptorCount();
        if (max_count > 0) {
            max_count_for_pass = max_count;
        }
        long count = mbean.getOpenFileDescriptorCount();
        if (trace) {
            System.out.println("Open file descriptor count: " + count);
        }
        if (count < MIN_COUNT_FOR_PASS || count > max_count_for_pass) {
            throw new RuntimeException("Open file descriptor count " +
                                       "illegal value: " + count + " bytes " +
                                       "(MIN = " + MIN_COUNT_FOR_PASS + "; " +
                                       "MAX = " + max_count_for_pass + ")");
        }
        System.out.println("Test passed.");
    }
}
