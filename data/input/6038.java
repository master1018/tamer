public class ThreadMonitor {
    private MBeanServerConnection server;
    private ThreadMXBean tmbean;
    private ObjectName objname;
    private String findDeadlocksMethodName = "findDeadlockedThreads";
    private boolean canDumpLocks = true;
    public ThreadMonitor(MBeanServerConnection server) throws IOException {
       this.server = server;
       this.tmbean = newPlatformMXBeanProxy(server,
                                            THREAD_MXBEAN_NAME,
                                            ThreadMXBean.class);
       try {
           objname = new ObjectName(THREAD_MXBEAN_NAME);
        } catch (MalformedObjectNameException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
       }
       parseMBeanInfo();
    }
    public ThreadMonitor() {
        this.tmbean = getThreadMXBean();
    }
    public void threadDump() {
        if (canDumpLocks) {
            if (tmbean.isObjectMonitorUsageSupported() &&
                tmbean.isSynchronizerUsageSupported()) {
                dumpThreadInfoWithLocks();
            }
        } else {
            dumpThreadInfo();
        }
    }
    private void dumpThreadInfo() {
       System.out.println("Full Java thread dump");
       long[] tids = tmbean.getAllThreadIds();
       ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
       for (ThreadInfo ti : tinfos) {
           printThreadInfo(ti);
       }
    }
    private void dumpThreadInfoWithLocks() {
       System.out.println("Full Java thread dump with locks info");
       ThreadInfo[] tinfos = tmbean.dumpAllThreads(true, true);
       for (ThreadInfo ti : tinfos) {
           printThreadInfo(ti);
           LockInfo[] syncs = ti.getLockedSynchronizers();
           printLockInfo(syncs);
       }
       System.out.println();
    }
    private static String INDENT = "    ";
    private void printThreadInfo(ThreadInfo ti) {
       printThread(ti);
       StackTraceElement[] stacktrace = ti.getStackTrace();
       MonitorInfo[] monitors = ti.getLockedMonitors();
       for (int i = 0; i < stacktrace.length; i++) {
           StackTraceElement ste = stacktrace[i];
           System.out.println(INDENT + "at " + ste.toString());
           for (MonitorInfo mi : monitors) {
               if (mi.getLockedStackDepth() == i) {
                   System.out.println(INDENT + "  - locked " + mi);
               }
           }
       }
       System.out.println();
    }
    private void printThread(ThreadInfo ti) {
       StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" +
                                            " Id=" + ti.getThreadId() +
                                            " in " + ti.getThreadState());
       if (ti.getLockName() != null) {
           sb.append(" on lock=" + ti.getLockName());
       }
       if (ti.isSuspended()) {
           sb.append(" (suspended)");
       }
       if (ti.isInNative()) {
           sb.append(" (running in native)");
       }
       System.out.println(sb.toString());
       if (ti.getLockOwnerName() != null) {
            System.out.println(INDENT + " owned by " + ti.getLockOwnerName() +
                               " Id=" + ti.getLockOwnerId());
       }
    }
    private void printMonitorInfo(ThreadInfo ti) {
       MonitorInfo[] monitors = ti.getLockedMonitors();
       System.out.println(INDENT + "Locked monitors: count = " + monitors.length);
       for (MonitorInfo mi : monitors) {
           System.out.println(INDENT + "  - " + mi + " locked at ");
           System.out.println(INDENT + "      " + mi.getLockedStackDepth() +
                              " " + mi.getLockedStackFrame());
       }
    }
    private void printLockInfo(LockInfo[] locks) {
       System.out.println(INDENT + "Locked synchronizers: count = " + locks.length);
       for (LockInfo li : locks) {
           System.out.println(INDENT + "  - " + li);
       }
       System.out.println();
    }
    public boolean findDeadlock() {
       long[] tids;
       if (findDeadlocksMethodName.equals("findDeadlockedThreads") &&
               tmbean.isSynchronizerUsageSupported()) {
           tids = tmbean.findDeadlockedThreads();
           if (tids == null) {
               return false;
           }
           System.out.println("Deadlock found :-");
           ThreadInfo[] infos = tmbean.getThreadInfo(tids, true, true);
           for (ThreadInfo ti : infos) {
               printThreadInfo(ti);
               printMonitorInfo(ti);
               printLockInfo(ti.getLockedSynchronizers());
               System.out.println();
           }
       } else {
           tids = tmbean.findMonitorDeadlockedThreads();
           if (tids == null) {
               return false;
           }
           ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
           for (ThreadInfo ti : infos) {
               printThreadInfo(ti);
           }
       }
       return true;
    }
    private void parseMBeanInfo() throws IOException {
        try {
            MBeanOperationInfo[] mopis = server.getMBeanInfo(objname).getOperations();
            boolean found = false;
            for (MBeanOperationInfo op : mopis) {
                if (op.getName().equals(findDeadlocksMethodName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                findDeadlocksMethodName = "findMonitorDeadlockedThreads";
                canDumpLocks = false;
            }
        } catch (IntrospectionException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (InstanceNotFoundException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (ReflectionException e) {
            InternalError ie = new InternalError(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }
}
