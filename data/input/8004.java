public class GetFreePhysicalMemorySize {
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
        long max_size = mbean.getTotalPhysicalMemorySize();
        if (max_size > 0) {
            max_size_for_pass = max_size;
        }
        long size = mbean.getFreePhysicalMemorySize();
        if (trace) {
            System.out.println("Free physical memory size in bytes: " + size);
        }
        if (size < MIN_SIZE_FOR_PASS || size > max_size_for_pass) {
            throw new RuntimeException("Free physical memory size " +
                                       "illegal value: " + size + " bytes " +
                                       "(MIN = " + MIN_SIZE_FOR_PASS + "; " +
                                       "MAX = " + max_size_for_pass + ")");
        }
        System.out.println("Test passed.");
    }
}
