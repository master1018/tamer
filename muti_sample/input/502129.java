public class Thread implements Runnable {
    private static final int NANOS_PER_MILLI = 1000000;
    private static class ParkState {
        private static final int UNPARKED = 1;
        private static final int PREEMPTIVELY_UNPARKED = 2;
        private static final int PARKED = 3;
    }
    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }
    public final static int MAX_PRIORITY = 10;
    public final static int MIN_PRIORITY = 1;
    public final static int NORM_PRIORITY = 5;
    volatile VMThread vmThread;
    volatile ThreadGroup group;
    volatile boolean daemon;
    volatile String name;
    volatile int priority;
    volatile long stackSize;
    Runnable target;
    private static int count = 0;
    private long id;
    ThreadLocal.Values localValues;
    ThreadLocal.Values inheritableValues;
    private Runnable interruptAction;
    private ClassLoader contextClassLoader;
    private UncaughtExceptionHandler uncaughtHandler;
    private static UncaughtExceptionHandler defaultUncaughtHandler;
    boolean hasBeenStarted = false;
    private int parkState = ParkState.UNPARKED;
    public Thread() {
        create(null, null, null, 0);
    }
    public Thread(Runnable runnable) {
        create(null, runnable, null, 0);
    }
    public Thread(Runnable runnable, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(null, runnable, threadName, 0);
    }
    public Thread(String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(null, null, threadName, 0);
    }
    public Thread(ThreadGroup group, Runnable runnable) {
        create(group, runnable, null, 0);
    }
    public Thread(ThreadGroup group, Runnable runnable, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(group, runnable, threadName, 0);
    }
    public Thread(ThreadGroup group, String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(group, null, threadName, 0);
    }
    public Thread(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        create(group, runnable, threadName, stackSize);
    }
    Thread(ThreadGroup group, String name, int priority, boolean daemon) {
        synchronized (Thread.class) {
            id = ++Thread.count;
        }
        if (name == null) {
            this.name = "Thread-" + id;
        } else
            this.name = name;
        if (group == null) {
            throw new InternalError("group not specified");
        }
        this.group = group;
        this.target = null;
        this.stackSize = 0;
        this.priority = priority;
        this.daemon = daemon;
        this.group.addThread(this);
    }
    private void create(ThreadGroup group, Runnable runnable, String threadName, long stackSize) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            if (group == null) {
                group = smgr.getThreadGroup();
            }
            if (getClass() != Thread.class) {
                Class[] signature = new Class[] { ClassLoader.class };
                try {
                    getClass().getDeclaredMethod("getContextClassLoader", signature);
                    smgr.checkPermission(new RuntimePermission("enableContextClassLoaderOverride"));
                } catch (NoSuchMethodException ex) {
                }
                try {
                    getClass().getDeclaredMethod("setContextClassLoader", signature);
                    smgr.checkPermission(new RuntimePermission("enableContextClassLoaderOverride"));
                } catch (NoSuchMethodException ex) {
                }
            }
        }
        Thread currentThread = Thread.currentThread();
        if (group == null) {
            group = currentThread.getThreadGroup();
        }
        group.checkAccess();
        if (group.isDestroyed()) {
            throw new IllegalThreadStateException("Group already destroyed");
        }
        this.group = group;
        synchronized (Thread.class) {
            id = ++Thread.count;
        }
        if (threadName == null) {
            this.name = "Thread-" + id;
        } else {
            this.name = threadName;
        }
        this.target = runnable;
        this.stackSize = stackSize;
        this.priority = currentThread.getPriority();
        this.contextClassLoader = currentThread.contextClassLoader;
        if (currentThread.inheritableValues != null) {
            inheritableValues
                    = new ThreadLocal.Values(currentThread.inheritableValues);
        }
        SecurityUtils.putContext(this, AccessController.getContext());
        this.group.addThread(this);
    }
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }
    public final void checkAccess() {
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkAccess(this);
        }
    }
    @Deprecated
    public int countStackFrames() {
        return getStackTrace().length;
    }
    public static Thread currentThread() {
        return VMThread.currentThread();
    }
    @Deprecated
    public void destroy() {
        throw new NoSuchMethodError("Thread.destroy()"); 
    }
    public static void dumpStack() {
        new Throwable("stack dump").printStackTrace();
    }
    public static int enumerate(Thread[] threads) {
        Thread thread = Thread.currentThread();
        thread.checkAccess();
        return thread.getThreadGroup().enumerate(threads);
    }
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("getStackTrace"));
            securityManager.checkPermission(new RuntimePermission("modifyThreadGroup"));
        }
        Map<Thread, StackTraceElement[]> map = new HashMap<Thread, StackTraceElement[]>();
        int count = ThreadGroup.mSystem.activeCount();
        Thread[] threads = new Thread[count + count / 2];
        count = ThreadGroup.mSystem.enumerate(threads);
        for (int i = 0; i < count; i++) {
            map.put(threads[i], threads[i].getStackTrace());
        }
        return map;
    }
    public ClassLoader getContextClassLoader() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader calling = VMStack.getCallingClassLoader();
            if (calling != null && !calling.isAncestorOf(contextClassLoader)) {
                sm.checkPermission(new RuntimePermission("getClassLoader"));
            }
        }
        return contextClassLoader;
    }
    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultUncaughtHandler;
    }
    public long getId() {
        return id;
    }
    public final String getName() {
        return name;
    }
    public final int getPriority() {
        return priority;
    }
    public StackTraceElement[] getStackTrace() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("getStackTrace"));
        }
        StackTraceElement ste[] = VMStack.getThreadStackTrace(this);
        return ste != null ? ste : new StackTraceElement[0];
    }
    public State getState() {
        VMThread vmt = this.vmThread;
        VMThread thread = vmThread;
        if (thread != null) {
            int state = thread.getStatus();
            if(state != -1) {
                return VMThread.STATE_MAP[state];
            }
        }
        return hasBeenStarted ? Thread.State.TERMINATED : Thread.State.NEW;
    }
    public final ThreadGroup getThreadGroup() {
        if (getState() == Thread.State.TERMINATED) {
            return null;
        } else {
            return group;
        }
    }
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        if (uncaughtHandler != null)
            return uncaughtHandler;
        else
            return group;           
    }
    public void interrupt() {
        checkAccess();
        if (interruptAction != null) {
            interruptAction.run();
        }
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.interrupt();
        }
    }
    public static boolean interrupted() {
        return VMThread.interrupted();
    }
    public final boolean isAlive() {
        return (vmThread != null);
    }
    public final boolean isDaemon() {
        return daemon;
    }
    public boolean isInterrupted() {
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            return vmt.isInterrupted();
        }
        return false;
    }
    public final void join() throws InterruptedException {
        VMThread t = vmThread;
        if (t == null) {
            return;
        }
        synchronized (t) {
            while (isAlive()) {
                t.wait();
            }
        }
    }
    public final void join(long millis) throws InterruptedException {
        join(millis, 0);
    }
    public final void join(long millis, int nanos) throws InterruptedException {
        if (millis < 0 || nanos < 0 || nanos >= NANOS_PER_MILLI) {
            throw new IllegalArgumentException();
        }
        boolean overflow = millis >= (Long.MAX_VALUE - nanos) / NANOS_PER_MILLI;
        boolean forever = (millis | nanos) == 0;
        if (forever | overflow) {
            join();
            return;
        }
        VMThread t = vmThread;
        if (t == null) {
            return;
        }
        synchronized (t) {
            if (!isAlive()) {
                return;
            }
            long nanosToWait = millis * NANOS_PER_MILLI + nanos;
            long start = System.nanoTime();
            while (true) {
                t.wait(millis, nanos);
                if (!isAlive()) {
                    break;
                }
                long nanosElapsed = System.nanoTime() - start;
                long nanosRemaining = nanosToWait - nanosElapsed;
                if (nanosRemaining <= 0) {
                    break;
                }
                millis = nanosRemaining / NANOS_PER_MILLI;
                nanos = (int) (nanosRemaining - millis * NANOS_PER_MILLI);
            }
        }
    }
    @Deprecated
    public final void resume() {
        checkAccess();
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.resume();
        }
    }
    public void run() {
        if (target != null) {
            target.run();
        }
    }
    public void setContextClassLoader(ClassLoader cl) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        contextClassLoader = cl;
    }
    public final void setDaemon(boolean isDaemon) {
        checkAccess();
        if (hasBeenStarted) {
            throw new IllegalThreadStateException("Thread already started."); 
        }
        if (vmThread == null) {
            daemon = isDaemon;
        }
    }
    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new RuntimePermission ("setDefaultUncaughtExceptionHandler"));
        }
        Thread.defaultUncaughtHandler = handler;
    }
    @SuppressWarnings("unused")
    private void setInterruptAction(Runnable action) {
        this.interruptAction = action;
    }
    public final void setName(String threadName) {
        if (threadName == null) {
            throw new NullPointerException();
        }
        checkAccess();
        name = threadName;
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.nameChanged(threadName);
        }
    }
    public final void setPriority(int priority) {
        checkAccess();
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("Prioritiy out of range"); 
        }
        if (priority > group.getMaxPriority()) {
            priority = group.getMaxPriority();
        }
        this.priority = priority;
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.setPriority(priority);
        }
    }
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
        checkAccess();
        uncaughtHandler = handler;
    }
    public static void sleep(long time) throws InterruptedException {
        Thread.sleep(time, 0);
    }
    public static void sleep(long millis, int nanos) throws InterruptedException {
        VMThread.sleep(millis, nanos);
    }
    public synchronized void start() {
        if (hasBeenStarted) {
            throw new IllegalThreadStateException("Thread already started."); 
        }
        hasBeenStarted = true;
        VMThread.create(this, stackSize);
    }
    @Deprecated
    public final void stop() {
        stop(new ThreadDeath());
    }
    @Deprecated
    public final synchronized void stop(Throwable throwable) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkAccess(this);
            if (Thread.currentThread() != this) {
                securityManager.checkPermission(new RuntimePermission("stopThread"));
            }
        }
        if (throwable == null) {
            throw new NullPointerException();
        }
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.stop(throwable);
        }
    }
    @Deprecated
    public final void suspend() {
        checkAccess();
        VMThread vmt = this.vmThread;
        if (vmt != null) {
            vmt.suspend();
        }
    }
    @Override
    public String toString() {
        return "Thread[" + name + "," + priority + "," + group.getName() + "]";
    }
    public static void yield() {
        VMThread.yield();
    }
    public static boolean holdsLock(Object object) {
        return currentThread().vmThread.holdsLock(object);
    }
    public static interface UncaughtExceptionHandler {
        void uncaughtException(Thread thread, Throwable ex);
    }
     void unpark() {
        VMThread vmt = vmThread;
        if (vmt == null) {
            parkState = ParkState.PREEMPTIVELY_UNPARKED;
            return;
        }
        synchronized (vmt) {
            switch (parkState) {
                case ParkState.PREEMPTIVELY_UNPARKED: {
                    break;
                }
                case ParkState.UNPARKED: {
                    parkState = ParkState.PREEMPTIVELY_UNPARKED;
                    break;
                }
                default : {
                    parkState = ParkState.UNPARKED;
                    vmt.notifyAll();
                    break;
                }
            }
        }
    }
     void parkFor(long nanos) {
        VMThread vmt = vmThread;
        if (vmt == null) {
            throw new AssertionError();
        }
        synchronized (vmt) {
            switch (parkState) {
                case ParkState.PREEMPTIVELY_UNPARKED: {
                    parkState = ParkState.UNPARKED;
                    break;
                }
                case ParkState.UNPARKED: {
                    long millis = nanos / NANOS_PER_MILLI;
                    nanos %= NANOS_PER_MILLI;
                    parkState = ParkState.PARKED;
                    try {
                        vmt.wait(millis, (int) nanos);
                    } catch (InterruptedException ex) {
                        interrupt();
                    } finally {
                        if (parkState == ParkState.PARKED) {
                            parkState = ParkState.UNPARKED;
                        }
                    }
                    break;
                }
                default : {
                    throw new AssertionError(
                            "shouldn't happen: attempt to repark");
                }
            }
        }
    }
     void parkUntil(long time) {
        VMThread vmt = vmThread;
        if (vmt == null) {
            throw new AssertionError();
        }
        synchronized (vmt) {
            long delayMillis = time - System.currentTimeMillis();
            if (delayMillis <= 0) {
                parkState = ParkState.UNPARKED;
            } else {
                parkFor(delayMillis * NANOS_PER_MILLI);
            }
        }
    }
}
