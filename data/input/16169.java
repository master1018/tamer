public class GetTotalSwapSpaceSize {
    private static OperatingSystemMXBean mbean =
        (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
    private static long       min_size_for_pass = 0;
    private static final long MAX_SIZE_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 1 && args[1].equals("trace")) {
            trace = true;
        }
        long expected_swap_size = 0;
        if (args.length < 1 || args.length > 2) {
           throw new IllegalArgumentException("Unexpected number of args " + args.length);
        }
        long min_size = mbean.getFreeSwapSpaceSize();
        if (min_size > 0) {
            min_size_for_pass = min_size;
        }
        long size = mbean.getTotalSwapSpaceSize();
        if (trace) {
            System.out.println("Total swap space size in bytes: " + size);
        }
        if (!args[0].matches("sanity-only")) {
            expected_swap_size = Long.parseLong(args[0]);
            if (size != expected_swap_size) {
                throw new RuntimeException("Expected total swap size      : " +
                                           expected_swap_size +
                                           " but getTotalSwapSpaceSize returned: " +
                                           size);
            }
        }
        if (size < min_size_for_pass || size > MAX_SIZE_FOR_PASS) {
            throw new RuntimeException("Total swap space size " +
                                       "illegal value: " + size + " bytes " +
                                       "(MIN = " + min_size_for_pass + "; " +
                                       "MAX = " + MAX_SIZE_FOR_PASS + ")");
        }
        System.out.println("Test passed.");
    }
}
