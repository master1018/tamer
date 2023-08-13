public class EnableTest {
    private static ThreadMXBean tm = ManagementFactory.getThreadMXBean();
    private static void checkThreadContentionMonitoring(boolean expectedValue)
        throws Exception {
        boolean value = tm.isThreadContentionMonitoringEnabled();
        if (value != expectedValue) {
             throw new RuntimeException("TEST FAILED: " +
                 "isThreadContentionMonitoringEnabled() returns " + value +
                 " but expected to be " + expectedValue);
        }
    }
    private static void testThreadContentionMonitoring()
        throws Exception {
        if (!tm.isThreadContentionMonitoringSupported()) return;
        checkThreadContentionMonitoring(false);
        tm.setThreadContentionMonitoringEnabled(true);
        checkThreadContentionMonitoring(true);
        tm.setThreadContentionMonitoringEnabled(true);
        checkThreadContentionMonitoring(true);
        tm.setThreadContentionMonitoringEnabled(true);
        tm.setThreadContentionMonitoringEnabled(false);
        checkThreadContentionMonitoring(false);
        tm.setThreadContentionMonitoringEnabled(false);
        checkThreadContentionMonitoring(false);
        tm.setThreadContentionMonitoringEnabled(true);
        checkThreadContentionMonitoring(true);
    }
    private static void checkThreadCpuTime(boolean expectedValue)
        throws Exception {
        boolean value = tm.isThreadCpuTimeEnabled();
        if (value != expectedValue) {
             throw new RuntimeException("TEST FAILED: " +
                 "isThreadCpuTimeEnabled() returns " + value +
                 " but expected to be " + expectedValue);
        }
    }
    private static void testThreadCpuTime()
        throws Exception {
        if (!tm.isThreadCpuTimeSupported()) return;
        if (!tm.isThreadCpuTimeEnabled()) {
            tm.setThreadCpuTimeEnabled(true);
            checkThreadCpuTime(true);
        }
        tm.setThreadCpuTimeEnabled(false);
        checkThreadCpuTime(false);
        tm.setThreadCpuTimeEnabled(true);
        checkThreadCpuTime(true);
        tm.setThreadCpuTimeEnabled(true);
        checkThreadCpuTime(true);
        tm.setThreadCpuTimeEnabled(true);
        tm.setThreadCpuTimeEnabled(false);
        checkThreadCpuTime(false);
        tm.setThreadCpuTimeEnabled(false);
        checkThreadCpuTime(false);
        tm.setThreadCpuTimeEnabled(true);
        checkThreadCpuTime(true);
    }
    public static void main(String args[]) throws Exception {
        try {
            testThreadContentionMonitoring();
            testThreadCpuTime();
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
}
