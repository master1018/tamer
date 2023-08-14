public class GetProcessCpuTime {
    private static OperatingSystemMXBean mbean =
        (com.sun.management.OperatingSystemMXBean)
        ManagementFactory.getOperatingSystemMXBean();
    private static final long MIN_TIME_FOR_PASS = 1;
    private static final long MAX_TIME_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        double sum=0;
        for (int i=0; i <=2000000; i++) {
          sum += i;
          sum /= i;
        }
        long ns = mbean.getProcessCpuTime();
        if (ns == -1) {
            System.out.println("getProcessCpuTime() is not supported");
            return;
        }
        if (trace) {
            System.out.println("Process CPU time in ns: " + ns);
        }
        if (ns < MIN_TIME_FOR_PASS || ns > MAX_TIME_FOR_PASS) {
            throw new RuntimeException("Process CPU time " +
                                       "illegal value: " + ns + " ns " +
                                       "(MIN = " + MIN_TIME_FOR_PASS + "; " +
                                       "MAX = " + MAX_TIME_FOR_PASS + ")");
        }
        System.out.println("Test passed.");
    }
}
