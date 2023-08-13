public class GetTotalPhysicalMemorySize {
    private static OperatingSystemMXBean mbean =
        (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
    private static long       min_size_for_pass = 1;
    private static final long MAX_SIZE_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long min_size = mbean.getFreePhysicalMemorySize();
        if (min_size > 0) {
            min_size_for_pass = min_size;
        }
        long size = mbean.getTotalPhysicalMemorySize();
        if (trace) {
            System.out.println("Total physical memory size in bytes: " + size);
        }
        if (size < min_size_for_pass || size > MAX_SIZE_FOR_PASS) {
            throw new RuntimeException("Total physical memory size " +
                                       "illegal value: " + size + " bytes " +
                                       "(MIN = " + min_size_for_pass + "; " +
                                       "MAX = " + MAX_SIZE_FOR_PASS + ")");
        }
        System.out.println("Test passed.");
    }
}
