public final class NewThreadAction implements PrivilegedAction<Thread> {
    static final ThreadGroup systemThreadGroup =
        AccessController.doPrivileged(new PrivilegedAction<ThreadGroup>() {
            public ThreadGroup run() {
                ThreadGroup group = Thread.currentThread().getThreadGroup();
                ThreadGroup parent;
                while ((parent = group.getParent()) != null) {
                    group = parent;
                }
                return group;
            }
        });
    static final ThreadGroup userThreadGroup =
        AccessController.doPrivileged(new PrivilegedAction<ThreadGroup>() {
            public ThreadGroup run() {
                return new ThreadGroup(systemThreadGroup, "RMI Runtime");
            }
        });
    private final ThreadGroup group;
    private final Runnable runnable;
    private final String name;
    private final boolean daemon;
    NewThreadAction(ThreadGroup group, Runnable runnable,
                    String name, boolean daemon)
    {
        this.group = group;
        this.runnable = runnable;
        this.name = name;
        this.daemon = daemon;
    }
    public NewThreadAction(Runnable runnable, String name, boolean daemon) {
        this(systemThreadGroup, runnable, name, daemon);
    }
    public NewThreadAction(Runnable runnable, String name, boolean daemon,
                           boolean user)
    {
        this(user ? userThreadGroup : systemThreadGroup,
             runnable, name, daemon);
    }
    public Thread run() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
        }
        Thread t = new Thread(group, runnable, "RMI " + name);
        t.setContextClassLoader(ClassLoader.getSystemClassLoader());
        t.setDaemon(daemon);
        return t;
    }
}
