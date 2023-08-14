public class FindMonitorDeadlock {
    public static void main(String[] argv) {
        MonitorDeadlock md = new MonitorDeadlock();
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] threads = mbean.findMonitorDeadlockedThreads();
        if (threads != null) {
            throw new RuntimeException("TEST FAILED: Should return null.");
        }
        md.goDeadlock();
        md.waitUntilDeadlock();
        threads = mbean.findMonitorDeadlockedThreads();
        if (threads == null) {
            ThreadDump.dumpStacks();
            throw new RuntimeException("TEST FAILED: Deadlock not detected.");
        }
        System.out.println("Found threads that are in deadlock:-");
        ThreadInfo[] infos = mbean.getThreadInfo(threads, Integer.MAX_VALUE);
        for (int i = 0; i < infos.length; i++) {
            ThreadDump.printThreadInfo(infos[i]);
        }
        md.checkResult(threads);
        System.out.println("Test passed");
    }
}
