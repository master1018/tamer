public class LockedMonitors {
    public static void main(String[] argv) throws Exception {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        if (!mbean.isObjectMonitorUsageSupported()) {
            System.out.println("Monitoring of object monitor usage is not supported");
            return;
        }
        LockingThread.startLockingThreads();
        ThreadDump.threadDump();
        ThreadInfo[] tinfos;
        long[] ids = LockingThread.getThreadIds();
        tinfos = mbean.dumpAllThreads(true, false);
        LockingThread.checkLockedMonitors(tinfos);
        tinfos = mbean.dumpAllThreads(true, true);
        LockingThread.checkLockedMonitors(tinfos);
        tinfos = mbean.getThreadInfo(ids, true, false);
        if (tinfos.length != ids.length) {
            throw new RuntimeException("Number of ThreadInfo objects = " +
                tinfos.length + " not matched. Expected: " + ids.length);
        }
        LockingThread.checkLockedMonitors(tinfos);
        tinfos = mbean.getThreadInfo(ids, true, true);
        if (tinfos.length != ids.length) {
            throw new RuntimeException("Number of ThreadInfo objects = " +
                tinfos.length + " not matched. Expected: " + ids.length);
        }
        LockingThread.checkLockedMonitors(tinfos);
        System.out.println("Test passed");
    }
}
