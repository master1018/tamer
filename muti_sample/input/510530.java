public class ThreadGroup implements Thread.UncaughtExceptionHandler {
    private String name;
    private int maxPriority = Thread.MAX_PRIORITY;
    ThreadGroup parent;
    int numThreads;
    private Thread[] childrenThreads = new Thread[5];
    int numGroups;
    private ThreadGroup[] childrenGroups = new ThreadGroup[3];
    private class ChildrenGroupsLock {}
    private Object childrenGroupsLock = new ChildrenGroupsLock();
    private class ChildrenThreadsLock {}
    private Object childrenThreadsLock = new ChildrenThreadsLock();
    private boolean isDaemon;
    private boolean isDestroyed;
    static ThreadGroup mSystem = new ThreadGroup();
    static ThreadGroup mMain = new ThreadGroup(mSystem, "main");
    public ThreadGroup(String name) {
        this(Thread.currentThread().getThreadGroup(), name);
    }
    public ThreadGroup(ThreadGroup parent, String name) {
        super();
        if (Thread.currentThread() != null) {
            parent.checkAccess();
        }
        this.name = name;
        this.setParent(parent);
        if (parent != null) {
            this.setMaxPriority(parent.getMaxPriority());
            if (parent.isDaemon()) {
                this.setDaemon(true);
            }
        }
    }
    ThreadGroup() {
        this.name = "system";
        this.setParent(null); 
    }
    public int activeCount() {
        int count = 0;
        synchronized (this.childrenThreadsLock) {
            for (int i = 0; i < numThreads; i++) {
                if(childrenThreads[i].isAlive()) {
                    count++;
                }
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                count += this.childrenGroups[i].activeCount();
            }
        }
        return count;
    }
    public int activeGroupCount() {
        int count = 0;
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                count += 1 + this.childrenGroups[i].activeGroupCount();
            }
        }
        return count;
    }
    final void add(Thread thread) throws IllegalThreadStateException {
        synchronized (this.childrenThreadsLock) {
            if (!isDestroyed) {
                if (childrenThreads.length == numThreads) {
                    Thread[] newThreads = new Thread[childrenThreads.length * 2];
                    System.arraycopy(childrenThreads, 0, newThreads, 0, numThreads);
                    newThreads[numThreads++] = thread;
                    childrenThreads = newThreads;
                } else {
                    childrenThreads[numThreads++] = thread;
                }
            } else {
                throw new IllegalThreadStateException();
            }
        }
    }
    private void add(ThreadGroup g) throws IllegalThreadStateException {
        synchronized (this.childrenGroupsLock) {
            if (!isDestroyed) {
                if (childrenGroups.length == numGroups) {
                    ThreadGroup[] newGroups = new ThreadGroup[childrenGroups.length * 2];
                    System.arraycopy(childrenGroups, 0, newGroups, 0, numGroups);
                    newGroups[numGroups++] = g;
                    childrenGroups = newGroups;
                } else {
                    childrenGroups[numGroups++] = g;
                }
            } else {
                throw new IllegalThreadStateException();
            }
        }
    }
    @Deprecated
    public boolean allowThreadSuspension(boolean b) {
        return true;
    }
    public final void checkAccess() {
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkAccess(this);
        }
    }
    public final void destroy() {
        checkAccess();
        synchronized (this.childrenThreadsLock) {
            synchronized (this.childrenGroupsLock) {
                if (this.isDestroyed) {
                    throw new IllegalThreadStateException(
                            "Thread group was already destroyed: "
                            + (this.name != null ? this.name : "n/a"));
                }
                if (this.numThreads > 0) {
                    throw new IllegalThreadStateException(
                            "Thread group still contains threads: "
                            + (this.name != null ? this.name : "n/a"));
                }
                int toDestroy = numGroups;
                for (int i = 0; i < toDestroy; i++) {
                    this.childrenGroups[0].destroy();
                }
                if (parent != null) {
                    parent.remove(this);
                }
                this.isDestroyed = true;
            }
        }
    }
    private void destroyIfEmptyDaemon() {
        synchronized (this.childrenThreadsLock) {
            if (isDaemon && !isDestroyed && numThreads == 0) {
                synchronized (this.childrenGroupsLock) {
                    if (numGroups == 0) {
                        destroy();
                    }
                }
            }
        }
    }
    public int enumerate(Thread[] threads) {
        return enumerate(threads, true);
    }
    public int enumerate(Thread[] threads, boolean recurse) {
        return enumerateGeneric(threads, recurse, 0, true);
    }
    public int enumerate(ThreadGroup[] groups) {
        return enumerate(groups, true);
    }
    public int enumerate(ThreadGroup[] groups, boolean recurse) {
        return enumerateGeneric(groups, recurse, 0, false);
    }
    private int enumerateGeneric(Object[] enumeration, boolean recurse, int enumerationIndex,
            boolean enumeratingThreads) {
        checkAccess();
        Object[] immediateCollection = enumeratingThreads ? (Object[]) childrenThreads
                : (Object[]) childrenGroups;
        Object syncLock = enumeratingThreads ? childrenThreadsLock : childrenGroupsLock;
        synchronized (syncLock) { 
            for (int i = enumeratingThreads ? numThreads : numGroups; --i >= 0;) {
                if (!enumeratingThreads || ((Thread) immediateCollection[i]).isAlive()) {
                    if (enumerationIndex >= enumeration.length) {
                        return enumerationIndex;
                    }
                    enumeration[enumerationIndex++] = immediateCollection[i];
                }
            }
        }
        if (recurse) { 
            synchronized (this.childrenGroupsLock) {
                for (int i = 0; i < numGroups; i++) {
                    if (enumerationIndex >= enumeration.length) {
                        return enumerationIndex;
                    }
                    enumerationIndex = childrenGroups[i].enumerateGeneric(enumeration, recurse,
                            enumerationIndex, enumeratingThreads);
                }
            }
        }
        return enumerationIndex;
    }
    public final int getMaxPriority() {
        return maxPriority;
    }
    public final String getName() {
        return name;
    }
    public final ThreadGroup getParent() {
        if (parent != null) {
            parent.checkAccess();
        }
        return parent;
    }
    public final void interrupt() {
        checkAccess();
        synchronized (this.childrenThreadsLock) {
            for (int i = 0; i < numThreads; i++) {
                this.childrenThreads[i].interrupt();
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                this.childrenGroups[i].interrupt();
            }
        }
    }
    public final boolean isDaemon() {
        return isDaemon;
    }
    public synchronized boolean isDestroyed() {
        return isDestroyed;
    }
    public void list() {
        System.out.println();
        list(0);
    }
    private void list(int levels) {
        for (int i = 0; i < levels; i++) {
            System.out.print("    "); 
        }
        System.out.println(this.toString());
        synchronized (this.childrenThreadsLock) {
            for (int i = 0; i < numThreads; i++) {
                for (int j = 0; j <= levels; j++) {
                    System.out.print("    ");
                }
                System.out.println(this.childrenThreads[i]);
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                this.childrenGroups[i].list(levels + 1);
            }
        }
    }
    public final boolean parentOf(ThreadGroup g) {
        while (g != null) {
            if (this == g) {
                return true;
            }
            g = g.parent;
        }
        return false;
    }
    final void remove(java.lang.Thread thread) {
        synchronized (this.childrenThreadsLock) {
            for (int i = 0; i < numThreads; i++) {
                if (childrenThreads[i].equals(thread)) {
                    numThreads--;
                    System
                            .arraycopy(childrenThreads, i + 1, childrenThreads, i, numThreads
                                    - i);
                    childrenThreads[numThreads] = null;
                    break;
                }
            }
        }
        destroyIfEmptyDaemon();
    }
    private void remove(ThreadGroup g) {
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                if (childrenGroups[i].equals(g)) {
                    numGroups--;
                    System.arraycopy(childrenGroups, i + 1, childrenGroups, i, numGroups - i);
                    childrenGroups[numGroups] = null;
                    break;
                }
            }
        }
        destroyIfEmptyDaemon();
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    public final void resume() {
        checkAccess();
        synchronized (this.childrenThreadsLock) {
            for (int i = 0; i < numThreads; i++) {
                this.childrenThreads[i].resume();
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                this.childrenGroups[i].resume();
            }
        }
    }
    public final void setDaemon(boolean isDaemon) {
        checkAccess();
        this.isDaemon = isDaemon;
    }
    public final void setMaxPriority(int newMax) {
        checkAccess();
        if (newMax <= this.maxPriority) {
            if (newMax < Thread.MIN_PRIORITY) {
                newMax = Thread.MIN_PRIORITY;
            }
            int parentPriority = parent == null ? newMax : parent.getMaxPriority();
            this.maxPriority = parentPriority <= newMax ? parentPriority : newMax;
            synchronized (this.childrenGroupsLock) {
                for (int i = 0; i < numGroups; i++) {
                    this.childrenGroups[i].setMaxPriority(newMax);
                }
            }
        }
    }
    private void setParent(ThreadGroup parent) {
        if (parent != null) {
            parent.add(this);
        }
        this.parent = parent;
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    public final void stop() {
        if (stopHelper()) {
            Thread.currentThread().stop();
        }
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    private final boolean stopHelper() {
        checkAccess();
        boolean stopCurrent = false;
        synchronized (this.childrenThreadsLock) {
            Thread current = Thread.currentThread();
            for (int i = 0; i < numThreads; i++) {
                if (this.childrenThreads[i] == current) {
                    stopCurrent = true;
                } else {
                    this.childrenThreads[i].stop();
                }
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                stopCurrent |= this.childrenGroups[i].stopHelper();
            }
        }
        return stopCurrent;
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    public final void suspend() {
        if (suspendHelper()) {
            Thread.currentThread().suspend();
        }
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    private final boolean suspendHelper() {
        checkAccess();
        boolean suspendCurrent = false;
        synchronized (this.childrenThreadsLock) {
            Thread current = Thread.currentThread();
            for (int i = 0; i < numThreads; i++) {
                if (this.childrenThreads[i] == current) {
                    suspendCurrent = true;
                } else {
                    this.childrenThreads[i].suspend();
                }
            }
        }
        synchronized (this.childrenGroupsLock) {
            for (int i = 0; i < numGroups; i++) {
                suspendCurrent |= this.childrenGroups[i].suspendHelper();
            }
        }
        return suspendCurrent;
    }
    @Override
    public String toString() {
        return getClass().getName() + "[name=" + this.getName() + ",maxpri="
                + this.getMaxPriority() + "]";
    }
    public void uncaughtException(Thread t, Throwable e) {
        if (parent != null) {
            parent.uncaughtException(t, e);
        } else if (Thread.getDefaultUncaughtExceptionHandler() != null) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(t, e);
        } else if (!(e instanceof ThreadDeath)) {
            e.printStackTrace(System.err);
        }
    }
    void addThread(Thread thread) throws IllegalThreadStateException {
        add(thread);
    }
    void removeThread(Thread thread) throws IllegalThreadStateException {
        remove(thread);
    }
}
