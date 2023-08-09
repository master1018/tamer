public class FindDeadlocks {
    static ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    public static void main(String[] argv) {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        MonitorDeadlock md = new MonitorDeadlock();
        if (findDeadlocks() != null) {
            throw new RuntimeException("TEST FAILED: Should return null.");
        }
        md.goDeadlock();
        md.waitUntilDeadlock();
        long[] mthreads = findDeadlocks();
        if (mthreads == null) {
            ThreadDump.dumpStacks();
            throw new RuntimeException("TEST FAILED: Deadlock not detected.");
        }
        md.checkResult(mthreads);
        SynchronizerDeadlock sd = new SynchronizerDeadlock();
        sd.goDeadlock();
        sd.waitUntilDeadlock();
        long[] threads = findDeadlocks();
        if (threads == null) {
            ThreadDump.dumpStacks();
            throw new RuntimeException("TEST FAILED: Deadlock not detected.");
        }
        long[] newList = new long[threads.length - mthreads.length];
        int count = 0;
        for (int i = 0; i < threads.length; i++) {
            long id = threads[i];
            boolean isNew = true;
            for (int j = 0; j < mthreads.length; j++) {
                if (mthreads[j] == id) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                newList[count++] = id;
            }
        }
        if (mbean.isSynchronizerUsageSupported()) {
            sd.checkResult(newList);
        } else {
            if (count != 0) {
                throw new RuntimeException("TEST FAILED: NewList should be empty.");
            }
        }
        System.out.println("Found threads that are in deadlock:-");
        ThreadInfo[] infos = mbean.getThreadInfo(threads, Integer.MAX_VALUE);
        for (int i = 0; i < infos.length; i++) {
            ThreadDump.printThreadInfo(infos[i]);
        }
        System.out.println("Test passed");
    }
    static long[] findDeadlocks() {
        long[] threads;
        if (mbean.isSynchronizerUsageSupported()) {
            threads = mbean.findDeadlockedThreads();
        } else {
            threads = mbean.findMonitorDeadlockedThreads();
        }
        return threads;
    }
}
