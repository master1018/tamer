public final class RuntimeUtil {
    private static final Log runtimeLog =
        Log.getLog("sun.rmi.runtime", null, false);
    private static final int schedulerThreads =         
        AccessController.doPrivileged(
            new GetIntegerAction("sun.rmi.runtime.schedulerThreads", 1));
    private static final Permission GET_INSTANCE_PERMISSION =
        new RuntimePermission("sun.rmi.runtime.RuntimeUtil.getInstance");
    private static final RuntimeUtil instance = new RuntimeUtil();
    private final ScheduledThreadPoolExecutor scheduler;
    private RuntimeUtil() {
        scheduler = new ScheduledThreadPoolExecutor(
            schedulerThreads,
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);
                public Thread newThread(Runnable runnable) {
                    try {
                        return AccessController.doPrivileged(
                            new NewThreadAction(runnable,
                                "Scheduler(" + count.getAndIncrement() + ")",
                                true));
                    } catch (Throwable t) {
                        runtimeLog.log(Level.WARNING,
                                       "scheduler thread factory throws", t);
                        return null;
                    }
                }
            });
    }
    public static class GetInstanceAction
        implements PrivilegedAction<RuntimeUtil>
    {
        public GetInstanceAction() {
        }
        public RuntimeUtil run() {
            return getInstance();
        }
    }
    private static RuntimeUtil getInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GET_INSTANCE_PERMISSION);
        }
        return instance;
    }
    public ScheduledThreadPoolExecutor getScheduler() {
        return scheduler;
    }
}
