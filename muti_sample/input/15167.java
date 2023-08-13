public class ForkJoinWorkerThread extends Thread {
    private static final int  SMASK  = 0xffff;
    private static final int INITIAL_QUEUE_CAPACITY = 1 << 13;
    private static final int MAXIMUM_QUEUE_CAPACITY = 1 << 24; 
    ForkJoinTask<?>[] queue;
    final ForkJoinPool pool;
    int queueTop;
    volatile int queueBase;
    int stealHint;
    final int poolIndex;
    int nextWait;
    volatile int eventCount;
    int seed;
    int stealCount;
    volatile boolean terminate;
    volatile boolean parked;
    final boolean locallyFifo;
    ForkJoinTask<?> currentSteal;
    ForkJoinTask<?> currentJoin;
    protected ForkJoinWorkerThread(ForkJoinPool pool) {
        super(pool.nextWorkerName());
        this.pool = pool;
        int k = pool.registerWorker(this);
        poolIndex = k;
        eventCount = ~k & SMASK; 
        locallyFifo = pool.locallyFifo;
        Thread.UncaughtExceptionHandler ueh = pool.ueh;
        if (ueh != null)
            setUncaughtExceptionHandler(ueh);
        setDaemon(true);
    }
    public ForkJoinPool getPool() {
        return pool;
    }
    public int getPoolIndex() {
        return poolIndex;
    }
    private int nextSeed() {
        int r = seed;
        r ^= r << 13;
        r ^= r >>> 17;
        r ^= r << 5;
        return seed = r;
    }
    protected void onStart() {
        queue = new ForkJoinTask<?>[INITIAL_QUEUE_CAPACITY];
        int r = pool.workerSeedGenerator.nextInt();
        seed = (r == 0) ? 1 : r; 
    }
    protected void onTermination(Throwable exception) {
        try {
            terminate = true;
            cancelTasks();
            pool.deregisterWorker(this, exception);
        } catch (Throwable ex) {        
            if (exception == null)      
                exception = ex;
        } finally {
            if (exception != null)
                UNSAFE.throwException(exception);
        }
    }
    public void run() {
        Throwable exception = null;
        try {
            onStart();
            pool.work(this);
        } catch (Throwable ex) {
            exception = ex;
        } finally {
            onTermination(exception);
        }
    }
    private static final boolean casSlotNull(ForkJoinTask<?>[] q, int i,
                                             ForkJoinTask<?> t) {
        return UNSAFE.compareAndSwapObject(q, (i << ASHIFT) + ABASE, t, null);
    }
    private static final void writeSlot(ForkJoinTask<?>[] q, int i,
                                        ForkJoinTask<?> t) {
        UNSAFE.putObjectVolatile(q, (i << ASHIFT) + ABASE, t);
    }
    final void pushTask(ForkJoinTask<?> t) {
        ForkJoinTask<?>[] q; int s, m;
        if ((q = queue) != null) {    
            long u = (((s = queueTop) & (m = q.length - 1)) << ASHIFT) + ABASE;
            UNSAFE.putOrderedObject(q, u, t);
            queueTop = s + 1;         
            if ((s -= queueBase) <= 2)
                pool.signalWork();
            else if (s == m)
                growQueue();
        }
    }
    private void growQueue() {
        ForkJoinTask<?>[] oldQ = queue;
        int size = oldQ != null ? oldQ.length << 1 : INITIAL_QUEUE_CAPACITY;
        if (size > MAXIMUM_QUEUE_CAPACITY)
            throw new RejectedExecutionException("Queue capacity exceeded");
        if (size < INITIAL_QUEUE_CAPACITY)
            size = INITIAL_QUEUE_CAPACITY;
        ForkJoinTask<?>[] q = queue = new ForkJoinTask<?>[size];
        int mask = size - 1;
        int top = queueTop;
        int oldMask;
        if (oldQ != null && (oldMask = oldQ.length - 1) >= 0) {
            for (int b = queueBase; b != top; ++b) {
                long u = ((b & oldMask) << ASHIFT) + ABASE;
                Object x = UNSAFE.getObjectVolatile(oldQ, u);
                if (x != null && UNSAFE.compareAndSwapObject(oldQ, u, x, null))
                    UNSAFE.putObjectVolatile
                        (q, ((b & mask) << ASHIFT) + ABASE, x);
            }
        }
    }
    final ForkJoinTask<?> deqTask() {
        ForkJoinTask<?> t; ForkJoinTask<?>[] q; int b, i;
        if (queueTop != (b = queueBase) &&
            (q = queue) != null && 
            (i = (q.length - 1) & b) >= 0 &&
            (t = q[i]) != null && queueBase == b &&
            UNSAFE.compareAndSwapObject(q, (i << ASHIFT) + ABASE, t, null)) {
            queueBase = b + 1;
            return t;
        }
        return null;
    }
    final ForkJoinTask<?> locallyDeqTask() {
        ForkJoinTask<?> t; int m, b, i;
        ForkJoinTask<?>[] q = queue;
        if (q != null && (m = q.length - 1) >= 0) {
            while (queueTop != (b = queueBase)) {
                if ((t = q[i = m & b]) != null &&
                    queueBase == b &&
                    UNSAFE.compareAndSwapObject(q, (i << ASHIFT) + ABASE,
                                                t, null)) {
                    queueBase = b + 1;
                    return t;
                }
            }
        }
        return null;
    }
    private ForkJoinTask<?> popTask() {
        int m;
        ForkJoinTask<?>[] q = queue;
        if (q != null && (m = q.length - 1) >= 0) {
            for (int s; (s = queueTop) != queueBase;) {
                int i = m & --s;
                long u = (i << ASHIFT) + ABASE; 
                ForkJoinTask<?> t = q[i];
                if (t == null)   
                    break;
                if (UNSAFE.compareAndSwapObject(q, u, t, null)) {
                    queueTop = s; 
                    return t;
                }
            }
        }
        return null;
    }
    final boolean unpushTask(ForkJoinTask<?> t) {
        ForkJoinTask<?>[] q;
        int s;
        if ((q = queue) != null && (s = queueTop) != queueBase &&
            UNSAFE.compareAndSwapObject
            (q, (((q.length - 1) & --s) << ASHIFT) + ABASE, t, null)) {
            queueTop = s; 
            return true;
        }
        return false;
    }
    final ForkJoinTask<?> peekTask() {
        int m;
        ForkJoinTask<?>[] q = queue;
        if (q == null || (m = q.length - 1) < 0)
            return null;
        int i = locallyFifo ? queueBase : (queueTop - 1);
        return q[i & m];
    }
    final void execTask(ForkJoinTask<?> t) {
        currentSteal = t;
        for (;;) {
            if (t != null)
                t.doExec();
            if (queueTop == queueBase)
                break;
            t = locallyFifo ? locallyDeqTask() : popTask();
        }
        ++stealCount;
        currentSteal = null;
    }
    final void cancelTasks() {
        ForkJoinTask<?> cj = currentJoin; 
        if (cj != null && cj.status >= 0)
            cj.cancelIgnoringExceptions();
        ForkJoinTask<?> cs = currentSteal;
        if (cs != null && cs.status >= 0)
            cs.cancelIgnoringExceptions();
        while (queueBase != queueTop) {
            ForkJoinTask<?> t = deqTask();
            if (t != null)
                t.cancelIgnoringExceptions();
        }
    }
    final int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int n = 0;
        while (queueBase != queueTop) {
            ForkJoinTask<?> t = deqTask();
            if (t != null) {
                c.add(t);
                ++n;
            }
        }
        return n;
    }
    final int getQueueSize() {
        return queueTop - queueBase;
    }
    final ForkJoinTask<?> pollLocalTask() {
        return locallyFifo ? locallyDeqTask() : popTask();
    }
    final ForkJoinTask<?> pollTask() {
        ForkJoinWorkerThread[] ws;
        ForkJoinTask<?> t = pollLocalTask();
        if (t != null || (ws = pool.workers) == null)
            return t;
        int n = ws.length; 
        int steps = n << 1;
        int r = nextSeed();
        int i = 0;
        while (i < steps) {
            ForkJoinWorkerThread w = ws[(i++ + r) & (n - 1)];
            if (w != null && w.queueBase != w.queueTop && w.queue != null) {
                if ((t = w.deqTask()) != null)
                    return t;
                i = 0;
            }
        }
        return null;
    }
    private static final int MAX_HELP = 16;
    final int joinTask(ForkJoinTask<?> joinMe) {
        ForkJoinTask<?> prevJoin = currentJoin;
        currentJoin = joinMe;
        for (int s, retries = MAX_HELP;;) {
            if ((s = joinMe.status) < 0) {
                currentJoin = prevJoin;
                return s;
            }
            if (retries > 0) {
                if (queueTop != queueBase) {
                    if (!localHelpJoinTask(joinMe))
                        retries = 0;           
                }
                else if (retries == MAX_HELP >>> 1) {
                    --retries;                 
                    if (tryDeqAndExec(joinMe) >= 0)
                        Thread.yield();        
                }
                else
                    retries = helpJoinTask(joinMe) ? MAX_HELP : retries - 1;
            }
            else {
                retries = MAX_HELP;           
                pool.tryAwaitJoin(joinMe);
            }
        }
    }
    private boolean localHelpJoinTask(ForkJoinTask<?> joinMe) {
        int s, i; ForkJoinTask<?>[] q; ForkJoinTask<?> t;
        if ((s = queueTop) != queueBase && (q = queue) != null &&
            (i = (q.length - 1) & --s) >= 0 &&
            (t = q[i]) != null) {
            if (t != joinMe && t.status >= 0)
                return false;
            if (UNSAFE.compareAndSwapObject
                (q, (i << ASHIFT) + ABASE, t, null)) {
                queueTop = s;           
                t.doExec();
            }
        }
        return true;
    }
    private boolean helpJoinTask(ForkJoinTask<?> joinMe) {
        boolean helped = false;
        int m = pool.scanGuard & SMASK;
        ForkJoinWorkerThread[] ws = pool.workers;
        if (ws != null && ws.length > m && joinMe.status >= 0) {
            int levels = MAX_HELP;              
            ForkJoinTask<?> task = joinMe;      
            outer:for (ForkJoinWorkerThread thread = this;;) {
                ForkJoinWorkerThread v = ws[thread.stealHint & m];
                if (v == null || v.currentSteal != task) {
                    for (int j = 0; ;) {        
                        if ((v = ws[j]) != null && v.currentSteal == task) {
                            thread.stealHint = j;
                            break;              
                        }
                        if (++j > m)
                            break outer;        
                    }
                }
                for (;;) {
                    ForkJoinTask<?>[] q; int b, i;
                    if (joinMe.status < 0)
                        break outer;
                    if ((b = v.queueBase) == v.queueTop ||
                        (q = v.queue) == null ||
                        (i = (q.length-1) & b) < 0)
                        break;                  
                    long u = (i << ASHIFT) + ABASE;
                    ForkJoinTask<?> t = q[i];
                    if (task.status < 0)
                        break outer;            
                    if (t != null && v.queueBase == b &&
                        UNSAFE.compareAndSwapObject(q, u, t, null)) {
                        v.queueBase = b + 1;
                        v.stealHint = poolIndex;
                        ForkJoinTask<?> ps = currentSteal;
                        currentSteal = t;
                        t.doExec();
                        currentSteal = ps;
                        helped = true;
                    }
                }
                ForkJoinTask<?> next = v.currentJoin;
                if (--levels > 0 && task.status >= 0 &&
                    next != null && next != task) {
                    task = next;
                    thread = v;
                }
                else
                    break;  
            }
        }
        return helped;
    }
    private int tryDeqAndExec(ForkJoinTask<?> t) {
        int m = pool.scanGuard & SMASK;
        ForkJoinWorkerThread[] ws = pool.workers;
        if (ws != null && ws.length > m && t.status >= 0) {
            for (int j = 0; j <= m; ++j) {
                ForkJoinTask<?>[] q; int b, i;
                ForkJoinWorkerThread v = ws[j];
                if (v != null &&
                    (b = v.queueBase) != v.queueTop &&
                    (q = v.queue) != null &&
                    (i = (q.length - 1) & b) >= 0 &&
                    q[i] ==  t) {
                    long u = (i << ASHIFT) + ABASE;
                    if (v.queueBase == b &&
                        UNSAFE.compareAndSwapObject(q, u, t, null)) {
                        v.queueBase = b + 1;
                        v.stealHint = poolIndex;
                        ForkJoinTask<?> ps = currentSteal;
                        currentSteal = t;
                        t.doExec();
                        currentSteal = ps;
                    }
                    break;
                }
            }
        }
        return t.status;
    }
    final int getEstimatedSurplusTaskCount() {
        return queueTop - queueBase - pool.idlePerActive();
    }
    final void helpQuiescePool() {
        boolean active = true;
        ForkJoinTask<?> ps = currentSteal; 
        ForkJoinPool p = pool;
        p.addQuiescerCount(1);
        for (;;) {
            ForkJoinWorkerThread[] ws = p.workers;
            ForkJoinWorkerThread v = null;
            int n;
            if (queueTop != queueBase)
                v = this;
            else if (ws != null && (n = ws.length) > 1) {
                ForkJoinWorkerThread w;
                int r = nextSeed(); 
                int steps = n << 1;
                for (int i = 0; i < steps; ++i) {
                    if ((w = ws[(i + r) & (n - 1)]) != null &&
                        w.queueBase != w.queueTop) {
                        v = w;
                        break;
                    }
                }
            }
            if (v != null) {
                ForkJoinTask<?> t;
                if (!active) {
                    active = true;
                    p.addActiveCount(1);
                }
                if ((t = (v != this) ? v.deqTask() :
                     locallyFifo ? locallyDeqTask() : popTask()) != null) {
                    currentSteal = t;
                    t.doExec();
                    currentSteal = ps;
                }
            }
            else {
                if (active) {
                    active = false;
                    p.addActiveCount(-1);
                }
                if (p.isQuiescent()) {
                    p.addActiveCount(1);
                    p.addQuiescerCount(-1);
                    break;
                }
            }
        }
    }
    private static final sun.misc.Unsafe UNSAFE;
    private static final long ABASE;
    private static final int ASHIFT;
    static {
        int s;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class a = ForkJoinTask[].class;
            ABASE = UNSAFE.arrayBaseOffset(a);
            s = UNSAFE.arrayIndexScale(a);
        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s-1)) != 0)
            throw new Error("data type scale not a power of two");
        ASHIFT = 31 - Integer.numberOfLeadingZeros(s);
    }
}
