public class GetMaxFileDescriptorCount {
    private static UnixOperatingSystemMXBean mbean =
        (UnixOperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    private static long       min_count_for_pass = 1;
    private static final long MAX_COUNT_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long min_count = mbean.getOpenFileDescriptorCount();
        if (min_count > 0) {
            min_count_for_pass = min_count;
        }
        long count = mbean.getMaxFileDescriptorCount();
        if (trace) {
            System.out.println("Max file descriptor count: " + count);
        }
        if (count < min_count_for_pass || count > MAX_COUNT_FOR_PASS) {
            throw new RuntimeException("Max file descriptor count " +
                                       "illegal value: " + count + " bytes " +
                                       "(MIN = " + min_count_for_pass + "; " +
                                       "MAX = " + MAX_COUNT_FOR_PASS + ")");
        }
        System.out.println("Test passed.");
    }
}
