public class UpTime {
    final static long DELAY = 5; 
    final static long TIMEOUT = 30; 
    private static RuntimeMXBean metrics
        = ManagementFactory.getRuntimeMXBean();
    public static void main(String argv[]) throws Exception {
        long jvmStartTime = metrics.getStartTime();
        long systemStartOuter = System.currentTimeMillis();
        long metricsStart = metrics.getUptime();
        long systemStartInner = System.currentTimeMillis();
        long testUptime = metricsStart - (systemStartOuter - jvmStartTime);
        if (testUptime > TIMEOUT * 60 * 1000)
            throw new RuntimeException("Uptime of the JVM is more than 30 "
                                     + "minutes ("
                                     + (metricsStart / 60 / 1000)
                                     + " minutes).");
        Object o = new Object();
        while (System.currentTimeMillis() < systemStartInner + DELAY * 1000) {
            synchronized (o) {
                try {
                    o.wait(DELAY * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        long systemEndInner = System.currentTimeMillis();
        long metricsEnd = metrics.getUptime();
        long systemEndOuter = System.currentTimeMillis();
        long systemDifferenceInner = systemEndInner - systemStartInner;
        long systemDifferenceOuter = systemEndOuter - systemStartOuter;
        long metricsDifference = metricsEnd - metricsStart;
        if (metricsDifference < systemDifferenceInner)
            throw new RuntimeException("Flow of the time in "
                                     + "RuntimeMXBean.getUptime() ("
                                     + metricsDifference + ") is slower than "
                                     + " in system (" + systemDifferenceInner
                                     + ")");
        if (metricsDifference > systemDifferenceOuter)
            throw new RuntimeException("Flow of the time in "
                                     + "RuntimeMXBean.getUptime() ("
                                     + metricsDifference + ") is faster than "
                                     + "in system (" + systemDifferenceOuter
                                     + ")");
        System.out.println("Test passed.");
    }
}
