public class GetCommittedVirtualMemorySize {
    private static OperatingSystemMXBean mbean =
        (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
    private static final long MIN_SIZE_FOR_PASS = 1;
    private static long       max_size_for_pass = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long max_size = mbean.getTotalSwapSpaceSize() +
                        mbean.getTotalPhysicalMemorySize();
        if (max_size > 0) {
            max_size_for_pass = max_size;
        }
        long size = mbean.getCommittedVirtualMemorySize();
        if (size == -1) {
            System.out.println("getCommittedVirtualMemorySize() is not supported");
            return;
        }
        if (trace) {
            System.out.println("Committed virtual memory size in bytes: " +
                               size);
        }
        if (size < MIN_SIZE_FOR_PASS || size > max_size_for_pass) {
            throw new RuntimeException("Committed virtual memory size " +
                                       "illegal value: " + size + " bytes " +
                                       "(MIN = " + MIN_SIZE_FOR_PASS + "; " +
                                       "MAX = " + max_size_for_pass + ")");
        }
        System.out.println("Test passed.");
    }
}
