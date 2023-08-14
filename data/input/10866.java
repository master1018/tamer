public class DisableTest {
    private static ThreadMXBean tm = ManagementFactory.getThreadMXBean();
    public static void main(String args[]) throws Exception {
        try {
            testThreadContentionMonitoring();
            testThreadCpuMonitoring();
        } finally {
            if (tm.isThreadContentionMonitoringSupported()) {
                tm.setThreadContentionMonitoringEnabled(false);
            }
            if (tm.isThreadCpuTimeSupported()) {
                tm.setThreadCpuTimeEnabled(false);
            }
        }
        System.out.println("Test passed.");
    }
    private static void testThreadContentionMonitoring()
        throws Exception {
        if (!tm.isThreadContentionMonitoringSupported()) {
            System.out.println("JVM does not supports thread contention monitoring");
            return;
        }
        tm.setThreadContentionMonitoringEnabled(false);
        tm.setThreadContentionMonitoringEnabled(false);
        if (tm.isThreadContentionMonitoringEnabled()) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected thread contention monitoring to be disabled");
        }
        tm.setThreadContentionMonitoringEnabled(true);
        if (!tm.isThreadContentionMonitoringEnabled()) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected thread contention monitoring to be enabled");
        }
    }
    private static void testThreadCpuMonitoring()
        throws Exception {
        if (!tm.isThreadCpuTimeSupported()) {
            System.out.println("JVM does not support thread CPU time monitoring");
            return;
        }
        if (tm.isThreadCpuTimeEnabled()) {
            tm.setThreadCpuTimeEnabled(false);
        }
        if (tm.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected thread CPU time monitoring to be disabled");
        }
        tm.setThreadCpuTimeEnabled(false);
        tm.setThreadCpuTimeEnabled(false);
        if (tm.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected thread CPU time monitoring to be disabled");
        }
        tm.setThreadCpuTimeEnabled(true);
        if (!tm.isThreadCpuTimeEnabled()) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected thread CPU time monitoring to be disabled");
        }
    }
}
