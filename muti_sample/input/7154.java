public class LockedSynchronizers {
    public static void main(String[] argv) throws Exception {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        if (!mbean.isSynchronizerUsageSupported()) {
            System.out.println("Monitoring of synchronizer usage not supported");
            return;
        }
        SynchronizerLockingThread.startLockingThreads();
        ThreadDump.threadDump();
        ThreadInfo[] tinfos = mbean.dumpAllThreads(true, true);
        SynchronizerLockingThread.checkLocks(tinfos);
        long[] ids = SynchronizerLockingThread.getThreadIds();
        tinfos = mbean.getThreadInfo(ids, true, true);
        if (tinfos.length != ids.length) {
            throw new RuntimeException("Number of ThreadInfo objects = " +
                tinfos.length + " not matched. Expected: " + ids.length);
        }
        SynchronizerLockingThread.checkLocks(tinfos);
        System.out.println("Test passed");
    }
}
