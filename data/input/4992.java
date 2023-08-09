public class ForkJoinPool extends AbstractExecutorService {
    public static interface ForkJoinWorkerThreadFactory {
        public ForkJoinWorkerThread newThread(ForkJoinPool pool);
    }
    static class DefaultForkJoinWorkerThreadFactory
        implements ForkJoinWorkerThreadFactory {
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }
    public static final ForkJoinWorkerThreadFactory
        defaultForkJoinWorkerThreadFactory;
    private static final RuntimePermission modifyThreadPermission;
    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(modifyThreadPermission);
    }
    private static final AtomicInteger poolNumberGenerator;
    static final Random workerSeedGenerator;
    ForkJoinWorkerThread[] workers;
    private static final int INITIAL_QUEUE_CAPACITY = 8;
    private static final int MAXIMUM_QUEUE_CAPACITY = 1 << 24; 
    private ForkJoinTask<?>[] submissionQueue;
    private final ReentrantLock submissionLock;
    private final Condition termination;
    private final ForkJoinWorkerThreadFactory factory;
    final Thread.UncaughtExceptionHandler ueh;
    private final String workerNamePrefix;
    private volatile long stealCount;
    volatile long ctl;
    private static final int  AC_SHIFT   = 48;
    private static final int  TC_SHIFT   = 32;
    private static final int  ST_SHIFT   = 31;
    private static final int  EC_SHIFT   = 16;
    private static final int  MAX_ID     = 0x7fff;  
    private static final int  SMASK      = 0xffff;  
    private static final int  SHORT_SIGN = 1 << 15;
    private static final int  INT_SIGN   = 1 << 31;
    private static final long STOP_BIT   = 0x0001L << ST_SHIFT;
    private static final long AC_MASK    = ((long)SMASK) << AC_SHIFT;
    private static final long TC_MASK    = ((long)SMASK) << TC_SHIFT;
    private static final long TC_UNIT    = 1L << TC_SHIFT;
    private static final long AC_UNIT    = 1L << AC_SHIFT;
    private static final int  UAC_SHIFT  = AC_SHIFT - 32;
    private static final int  UTC_SHIFT  = TC_SHIFT - 32;
    private static final int  UAC_MASK   = SMASK << UAC_SHIFT;
    private static final int  UTC_MASK   = SMASK << UTC_SHIFT;
    private static final int  UAC_UNIT   = 1 << UAC_SHIFT;
    private static final int  UTC_UNIT   = 1 << UTC_SHIFT;
    private static final int  E_MASK     = 0x7fffffff; 
    private static final int  EC_UNIT    = 1 << EC_SHIFT;
    final int parallelism;
    volatile int queueBase;
    int queueTop;
    volatile boolean shutdown;
    final boolean locallyFifo;
    volatile int quiescerCount;
    volatile int blockedCount;
    private volatile int nextWorkerNumber;
    private int nextWorkerIndex;
    volatile int scanGuard;
    private static final int SG_UNIT = 1 << 16;
    private static final long SHRINK_RATE =
        4L * 1000L * 1000L * 1000L; 
    final void work(ForkJoinWorkerThread w) {
        boolean swept = false;                
        long c;
        while (!w.terminate && (int)(c = ctl) >= 0) {
            int a;                            
            if (!swept && (a = (int)(c >> AC_SHIFT)) <= 0)
                swept = scan(w, a);
            else if (tryAwaitWork(w, c))
                swept = false;
        }
    }
    final void signalWork() {
        long c; int e, u;
        while ((((e = (int)(c = ctl)) | (u = (int)(c >>> 32))) &
                (INT_SIGN|SHORT_SIGN)) == (INT_SIGN|SHORT_SIGN) && e >= 0) {
            if (e > 0) {                         
                int i; ForkJoinWorkerThread w; ForkJoinWorkerThread[] ws;
                if ((ws = workers) == null ||
                    (i = ~e & SMASK) >= ws.length ||
                    (w = ws[i]) == null)
                    break;
                long nc = (((long)(w.nextWait & E_MASK)) |
                           ((long)(u + UAC_UNIT) << 32));
                if (w.eventCount == e &&
                    UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc)) {
                    w.eventCount = (e + EC_UNIT) & E_MASK;
                    if (w.parked)
                        UNSAFE.unpark(w);
                    break;
                }
            }
            else if (UNSAFE.compareAndSwapLong
                     (this, ctlOffset, c,
                      (long)(((u + UTC_UNIT) & UTC_MASK) |
                             ((u + UAC_UNIT) & UAC_MASK)) << 32)) {
                addWorker();
                break;
            }
        }
    }
    private boolean tryReleaseWaiter() {
        long c; int e, i; ForkJoinWorkerThread w; ForkJoinWorkerThread[] ws;
        if ((e = (int)(c = ctl)) > 0 &&
            (int)(c >> AC_SHIFT) < 0 &&
            (ws = workers) != null &&
            (i = ~e & SMASK) < ws.length &&
            (w = ws[i]) != null) {
            long nc = ((long)(w.nextWait & E_MASK) |
                       ((c + AC_UNIT) & (AC_MASK|TC_MASK)));
            if (w.eventCount != e ||
                !UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc))
                return false;
            w.eventCount = (e + EC_UNIT) & E_MASK;
            if (w.parked)
                UNSAFE.unpark(w);
        }
        return true;
    }
    private boolean scan(ForkJoinWorkerThread w, int a) {
        int g = scanGuard; 
        int m = (parallelism == 1 - a && blockedCount == 0) ? 0 : g & SMASK;
        ForkJoinWorkerThread[] ws = workers;
        if (ws == null || ws.length <= m)         
            return false;
        for (int r = w.seed, k = r, j = -(m + m); j <= m + m; ++j) {
            ForkJoinTask<?> t; ForkJoinTask<?>[] q; int b, i;
            ForkJoinWorkerThread v = ws[k & m];
            if (v != null && (b = v.queueBase) != v.queueTop &&
                (q = v.queue) != null && (i = (q.length - 1) & b) >= 0) {
                long u = (i << ASHIFT) + ABASE;
                if ((t = q[i]) != null && v.queueBase == b &&
                    UNSAFE.compareAndSwapObject(q, u, t, null)) {
                    int d = (v.queueBase = b + 1) - v.queueTop;
                    v.stealHint = w.poolIndex;
                    if (d != 0)
                        signalWork();             
                    w.execTask(t);
                }
                r ^= r << 13; r ^= r >>> 17; w.seed = r ^ (r << 5);
                return false;                     
            }
            else if (j < 0) {                     
                r ^= r << 13; r ^= r >>> 17; k = r ^= r << 5;
            }
            else
                ++k;
        }
        if (scanGuard != g)                       
            return false;
        else {                                    
            ForkJoinTask<?> t; ForkJoinTask<?>[] q; int b, i;
            if ((b = queueBase) != queueTop &&
                (q = submissionQueue) != null &&
                (i = (q.length - 1) & b) >= 0) {
                long u = (i << ASHIFT) + ABASE;
                if ((t = q[i]) != null && queueBase == b &&
                    UNSAFE.compareAndSwapObject(q, u, t, null)) {
                    queueBase = b + 1;
                    w.execTask(t);
                }
                return false;
            }
            return true;                         
        }
    }
    private boolean tryAwaitWork(ForkJoinWorkerThread w, long c) {
        int v = w.eventCount;
        w.nextWait = (int)c;                      
        long nc = (long)(v & E_MASK) | ((c - AC_UNIT) & (AC_MASK|TC_MASK));
        if (ctl != c || !UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc)) {
            long d = ctl; 
            return (int)d != (int)c && ((d - c) & AC_MASK) >= 0L;
        }
        for (int sc = w.stealCount; sc != 0;) {   
            long s = stealCount;
            if (UNSAFE.compareAndSwapLong(this, stealCountOffset, s, s + sc))
                sc = w.stealCount = 0;
            else if (w.eventCount != v)
                return true;                      
        }
        if ((!shutdown || !tryTerminate(false)) &&
            (int)c != 0 && parallelism + (int)(nc >> AC_SHIFT) == 0 &&
            blockedCount == 0 && quiescerCount == 0)
            idleAwaitWork(w, nc, c, v);           
        for (boolean rescanned = false;;) {
            if (w.eventCount != v)
                return true;
            if (!rescanned) {
                int g = scanGuard, m = g & SMASK;
                ForkJoinWorkerThread[] ws = workers;
                if (ws != null && m < ws.length) {
                    rescanned = true;
                    for (int i = 0; i <= m; ++i) {
                        ForkJoinWorkerThread u = ws[i];
                        if (u != null) {
                            if (u.queueBase != u.queueTop &&
                                !tryReleaseWaiter())
                                rescanned = false; 
                            if (w.eventCount != v)
                                return true;
                        }
                    }
                }
                if (scanGuard != g ||              
                    (queueBase != queueTop && !tryReleaseWaiter()))
                    rescanned = false;
                if (!rescanned)
                    Thread.yield();                
                else
                    Thread.interrupted();          
            }
            else {
                w.parked = true;                   
                if (w.eventCount != v) {
                    w.parked = false;
                    return true;
                }
                LockSupport.park(this);
                rescanned = w.parked = false;
            }
        }
    }
    private void idleAwaitWork(ForkJoinWorkerThread w, long currentCtl,
                               long prevCtl, int v) {
        if (w.eventCount == v) {
            if (shutdown)
                tryTerminate(false);
            ForkJoinTask.helpExpungeStaleExceptions(); 
            while (ctl == currentCtl) {
                long startTime = System.nanoTime();
                w.parked = true;
                if (w.eventCount == v)             
                    LockSupport.parkNanos(this, SHRINK_RATE);
                w.parked = false;
                if (w.eventCount != v)
                    break;
                else if (System.nanoTime() - startTime <
                         SHRINK_RATE - (SHRINK_RATE / 10)) 
                    Thread.interrupted();          
                else if (UNSAFE.compareAndSwapLong(this, ctlOffset,
                                                   currentCtl, prevCtl)) {
                    w.terminate = true;            
                    w.eventCount = ((int)currentCtl + EC_UNIT) & E_MASK;
                    break;
                }
            }
        }
    }
    private void addSubmission(ForkJoinTask<?> t) {
        final ReentrantLock lock = this.submissionLock;
        lock.lock();
        try {
            ForkJoinTask<?>[] q; int s, m;
            if ((q = submissionQueue) != null) {    
                long u = (((s = queueTop) & (m = q.length-1)) << ASHIFT)+ABASE;
                UNSAFE.putOrderedObject(q, u, t);
                queueTop = s + 1;
                if (s - queueBase == m)
                    growSubmissionQueue();
            }
        } finally {
            lock.unlock();
        }
        signalWork();
    }
    private void growSubmissionQueue() {
        ForkJoinTask<?>[] oldQ = submissionQueue;
        int size = oldQ != null ? oldQ.length << 1 : INITIAL_QUEUE_CAPACITY;
        if (size > MAXIMUM_QUEUE_CAPACITY)
            throw new RejectedExecutionException("Queue capacity exceeded");
        if (size < INITIAL_QUEUE_CAPACITY)
            size = INITIAL_QUEUE_CAPACITY;
        ForkJoinTask<?>[] q = submissionQueue = new ForkJoinTask<?>[size];
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
    private boolean tryPreBlock() {
        int b = blockedCount;
        if (UNSAFE.compareAndSwapInt(this, blockedCountOffset, b, b + 1)) {
            int pc = parallelism;
            do {
                ForkJoinWorkerThread[] ws; ForkJoinWorkerThread w;
                int e, ac, tc, rc, i;
                long c = ctl;
                int u = (int)(c >>> 32);
                if ((e = (int)c) < 0) {
                }
                else if ((ac = (u >> UAC_SHIFT)) <= 0 && e != 0 &&
                         (ws = workers) != null &&
                         (i = ~e & SMASK) < ws.length &&
                         (w = ws[i]) != null) {
                    long nc = ((long)(w.nextWait & E_MASK) |
                               (c & (AC_MASK|TC_MASK)));
                    if (w.eventCount == e &&
                        UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc)) {
                        w.eventCount = (e + EC_UNIT) & E_MASK;
                        if (w.parked)
                            UNSAFE.unpark(w);
                        return true;             
                    }
                }
                else if ((tc = (short)(u >>> UTC_SHIFT)) >= 0 && ac + pc > 1) {
                    long nc = ((c - AC_UNIT) & AC_MASK) | (c & ~AC_MASK);
                    if (UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc))
                        return true;             
                }
                else if (tc + pc < MAX_ID) {
                    long nc = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK);
                    if (UNSAFE.compareAndSwapLong(this, ctlOffset, c, nc)) {
                        addWorker();
                        return true;            
                    }
                }
            } while (!UNSAFE.compareAndSwapInt(this, blockedCountOffset,
                                               b = blockedCount, b - 1));
        }
        return false;
    }
    private void postBlock() {
        long c;
        do {} while (!UNSAFE.compareAndSwapLong(this, ctlOffset,  
                                                c = ctl, c + AC_UNIT));
        int b;
        do {} while (!UNSAFE.compareAndSwapInt(this, blockedCountOffset,
                                               b = blockedCount, b - 1));
    }
    final void tryAwaitJoin(ForkJoinTask<?> joinMe) {
        int s;
        Thread.interrupted(); 
        if (joinMe.status >= 0) {
            if (tryPreBlock()) {
                joinMe.tryAwaitDone(0L);
                postBlock();
            }
            else if ((ctl & STOP_BIT) != 0L)
                joinMe.cancelIgnoringExceptions();
        }
    }
    final void timedAwaitJoin(ForkJoinTask<?> joinMe, long nanos) {
        while (joinMe.status >= 0) {
            Thread.interrupted();
            if ((ctl & STOP_BIT) != 0L) {
                joinMe.cancelIgnoringExceptions();
                break;
            }
            if (tryPreBlock()) {
                long last = System.nanoTime();
                while (joinMe.status >= 0) {
                    long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                    if (millis <= 0)
                        break;
                    joinMe.tryAwaitDone(millis);
                    if (joinMe.status < 0)
                        break;
                    if ((ctl & STOP_BIT) != 0L) {
                        joinMe.cancelIgnoringExceptions();
                        break;
                    }
                    long now = System.nanoTime();
                    nanos -= now - last;
                    last = now;
                }
                postBlock();
                break;
            }
        }
    }
    private void awaitBlocker(ManagedBlocker blocker)
        throws InterruptedException {
        while (!blocker.isReleasable()) {
            if (tryPreBlock()) {
                try {
                    do {} while (!blocker.isReleasable() && !blocker.block());
                } finally {
                    postBlock();
                }
                break;
            }
        }
    }
    private void addWorker() {
        Throwable ex = null;
        ForkJoinWorkerThread t = null;
        try {
            t = factory.newThread(this);
        } catch (Throwable e) {
            ex = e;
        }
        if (t == null) {  
            long c;       
            do {} while (!UNSAFE.compareAndSwapLong
                         (this, ctlOffset, c = ctl,
                          (((c - AC_UNIT) & AC_MASK) |
                           ((c - TC_UNIT) & TC_MASK) |
                           (c & ~(AC_MASK|TC_MASK)))));
            if (!tryTerminate(false) && ex != null &&
                !(Thread.currentThread() instanceof ForkJoinWorkerThread))
                UNSAFE.throwException(ex);
        }
        else
            t.start();
    }
    final String nextWorkerName() {
        for (int n;;) {
            if (UNSAFE.compareAndSwapInt(this, nextWorkerNumberOffset,
                                         n = nextWorkerNumber, ++n))
                return workerNamePrefix + n;
        }
    }
    final int registerWorker(ForkJoinWorkerThread w) {
        for (int g;;) {
            ForkJoinWorkerThread[] ws;
            if (((g = scanGuard) & SG_UNIT) == 0 &&
                UNSAFE.compareAndSwapInt(this, scanGuardOffset,
                                         g, g | SG_UNIT)) {
                int k = nextWorkerIndex;
                try {
                    if ((ws = workers) != null) { 
                        int n = ws.length;
                        if (k < 0 || k >= n || ws[k] != null) {
                            for (k = 0; k < n && ws[k] != null; ++k)
                                ;
                            if (k == n)
                                ws = workers = Arrays.copyOf(ws, n << 1);
                        }
                        ws[k] = w;
                        nextWorkerIndex = k + 1;
                        int m = g & SMASK;
                        g = (k > m) ? ((m << 1) + 1) & SMASK : g + (SG_UNIT<<1);
                    }
                } finally {
                    scanGuard = g;
                }
                return k;
            }
            else if ((ws = workers) != null) { 
                for (ForkJoinWorkerThread u : ws) {
                    if (u != null && u.queueBase != u.queueTop) {
                        if (tryReleaseWaiter())
                            break;
                    }
                }
            }
        }
    }
    final void deregisterWorker(ForkJoinWorkerThread w, Throwable ex) {
        int idx = w.poolIndex;
        int sc = w.stealCount;
        int steps = 0;
        do {
            long s, c;
            int g;
            if (steps == 0 && ((g = scanGuard) & SG_UNIT) == 0 &&
                UNSAFE.compareAndSwapInt(this, scanGuardOffset,
                                         g, g |= SG_UNIT)) {
                ForkJoinWorkerThread[] ws = workers;
                if (ws != null && idx >= 0 &&
                    idx < ws.length && ws[idx] == w)
                    ws[idx] = null;    
                nextWorkerIndex = idx;
                scanGuard = g + SG_UNIT;
                steps = 1;
            }
            if (steps == 1 &&
                UNSAFE.compareAndSwapLong(this, ctlOffset, c = ctl,
                                          (((c - AC_UNIT) & AC_MASK) |
                                           ((c - TC_UNIT) & TC_MASK) |
                                           (c & ~(AC_MASK|TC_MASK)))))
                steps = 2;
            if (sc != 0 &&
                UNSAFE.compareAndSwapLong(this, stealCountOffset,
                                          s = stealCount, s + sc))
                sc = 0;
        } while (steps != 2 || sc != 0);
        if (!tryTerminate(false)) {
            if (ex != null)   
                signalWork();
            else
                tryReleaseWaiter();
        }
    }
    private boolean tryTerminate(boolean now) {
        long c;
        while (((c = ctl) & STOP_BIT) == 0) {
            if (!now) {
                if ((int)(c >> AC_SHIFT) != -parallelism)
                    return false;
                if (!shutdown || blockedCount != 0 || quiescerCount != 0 ||
                    queueBase != queueTop) {
                    if (ctl == c) 
                        return false;
                    continue;
                }
            }
            if (UNSAFE.compareAndSwapLong(this, ctlOffset, c, c | STOP_BIT))
                startTerminating();
        }
        if ((short)(c >>> TC_SHIFT) == -parallelism) { 
            final ReentrantLock lock = this.submissionLock;
            lock.lock();
            try {
                termination.signalAll();
            } finally {
                lock.unlock();
            }
        }
        return true;
    }
    private void startTerminating() {
        cancelSubmissions();
        for (int pass = 0; pass < 3; ++pass) {
            ForkJoinWorkerThread[] ws = workers;
            if (ws != null) {
                for (ForkJoinWorkerThread w : ws) {
                    if (w != null) {
                        w.terminate = true;
                        if (pass > 0) {
                            w.cancelTasks();
                            if (pass > 1 && !w.isInterrupted()) {
                                try {
                                    w.interrupt();
                                } catch (SecurityException ignore) {
                                }
                            }
                        }
                    }
                }
                terminateWaiters();
            }
        }
    }
    private void cancelSubmissions() {
        while (queueBase != queueTop) {
            ForkJoinTask<?> task = pollSubmission();
            if (task != null) {
                try {
                    task.cancel(false);
                } catch (Throwable ignore) {
                }
            }
        }
    }
    private void terminateWaiters() {
        ForkJoinWorkerThread[] ws = workers;
        if (ws != null) {
            ForkJoinWorkerThread w; long c; int i, e;
            int n = ws.length;
            while ((i = ~(e = (int)(c = ctl)) & SMASK) < n &&
                   (w = ws[i]) != null && w.eventCount == (e & E_MASK)) {
                if (UNSAFE.compareAndSwapLong(this, ctlOffset, c,
                                              (long)(w.nextWait & E_MASK) |
                                              ((c + AC_UNIT) & AC_MASK) |
                                              (c & (TC_MASK|STOP_BIT)))) {
                    w.terminate = true;
                    w.eventCount = e + EC_UNIT;
                    if (w.parked)
                        UNSAFE.unpark(w);
                }
            }
        }
    }
    final void addQuiescerCount(int delta) {
        int c;
        do {} while (!UNSAFE.compareAndSwapInt(this, quiescerCountOffset,
                                               c = quiescerCount, c + delta));
    }
    final void addActiveCount(int delta) {
        long d = delta < 0 ? -AC_UNIT : AC_UNIT;
        long c;
        do {} while (!UNSAFE.compareAndSwapLong(this, ctlOffset, c = ctl,
                                                ((c + d) & AC_MASK) |
                                                (c & ~AC_MASK)));
    }
    final int idlePerActive() {
        int p = parallelism;
        int a = p + (int)(ctl >> AC_SHIFT);
        return (a > (p >>>= 1) ? 0 :
                a > (p >>>= 1) ? 1 :
                a > (p >>>= 1) ? 2 :
                a > (p >>>= 1) ? 4 :
                8);
    }
    public ForkJoinPool() {
        this(Runtime.getRuntime().availableProcessors(),
             defaultForkJoinWorkerThreadFactory, null, false);
    }
    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }
    public ForkJoinPool(int parallelism,
                        ForkJoinWorkerThreadFactory factory,
                        Thread.UncaughtExceptionHandler handler,
                        boolean asyncMode) {
        checkPermission();
        if (factory == null)
            throw new NullPointerException();
        if (parallelism <= 0 || parallelism > MAX_ID)
            throw new IllegalArgumentException();
        this.parallelism = parallelism;
        this.factory = factory;
        this.ueh = handler;
        this.locallyFifo = asyncMode;
        long np = (long)(-parallelism); 
        this.ctl = ((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK);
        this.submissionQueue = new ForkJoinTask<?>[INITIAL_QUEUE_CAPACITY];
        int n = parallelism << 1;
        if (n >= MAX_ID)
            n = MAX_ID;
        else { 
            n |= n >>> 1; n |= n >>> 2; n |= n >>> 4; n |= n >>> 8;
        }
        workers = new ForkJoinWorkerThread[n + 1];
        this.submissionLock = new ReentrantLock();
        this.termination = submissionLock.newCondition();
        StringBuilder sb = new StringBuilder("ForkJoinPool-");
        sb.append(poolNumberGenerator.incrementAndGet());
        sb.append("-worker-");
        this.workerNamePrefix = sb.toString();
    }
    public <T> T invoke(ForkJoinTask<T> task) {
        Thread t = Thread.currentThread();
        if (task == null)
            throw new NullPointerException();
        if (shutdown)
            throw new RejectedExecutionException();
        if ((t instanceof ForkJoinWorkerThread) &&
            ((ForkJoinWorkerThread)t).pool == this)
            return task.invoke();  
        else {
            addSubmission(task);
            return task.join();
        }
    }
    private <T> void forkOrSubmit(ForkJoinTask<T> task) {
        ForkJoinWorkerThread w;
        Thread t = Thread.currentThread();
        if (shutdown)
            throw new RejectedExecutionException();
        if ((t instanceof ForkJoinWorkerThread) &&
            (w = (ForkJoinWorkerThread)t).pool == this)
            w.pushTask(task);
        else
            addSubmission(task);
    }
    public void execute(ForkJoinTask<?> task) {
        if (task == null)
            throw new NullPointerException();
        forkOrSubmit(task);
    }
    public void execute(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) 
            job = (ForkJoinTask<?>) task;
        else
            job = ForkJoinTask.adapt(task, null);
        forkOrSubmit(job);
    }
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        forkOrSubmit(task);
        return task;
    }
    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<T> job = ForkJoinTask.adapt(task);
        forkOrSubmit(job);
        return job;
    }
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<T> job = ForkJoinTask.adapt(task, result);
        forkOrSubmit(job);
        return job;
    }
    public ForkJoinTask<?> submit(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>) 
            job = (ForkJoinTask<?>) task;
        else
            job = ForkJoinTask.adapt(task, null);
        forkOrSubmit(job);
        return job;
    }
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ArrayList<ForkJoinTask<T>> forkJoinTasks =
            new ArrayList<ForkJoinTask<T>>(tasks.size());
        for (Callable<T> task : tasks)
            forkJoinTasks.add(ForkJoinTask.adapt(task));
        invoke(new InvokeAll<T>(forkJoinTasks));
        @SuppressWarnings({"unchecked", "rawtypes"})
            List<Future<T>> futures = (List<Future<T>>) (List) forkJoinTasks;
        return futures;
    }
    static final class InvokeAll<T> extends RecursiveAction {
        final ArrayList<ForkJoinTask<T>> tasks;
        InvokeAll(ArrayList<ForkJoinTask<T>> tasks) { this.tasks = tasks; }
        public void compute() {
            try { invokeAll(tasks); }
            catch (Exception ignore) {}
        }
        private static final long serialVersionUID = -7914297376763021607L;
    }
    public ForkJoinWorkerThreadFactory getFactory() {
        return factory;
    }
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return ueh;
    }
    public int getParallelism() {
        return parallelism;
    }
    public int getPoolSize() {
        return parallelism + (short)(ctl >>> TC_SHIFT);
    }
    public boolean getAsyncMode() {
        return locallyFifo;
    }
    public int getRunningThreadCount() {
        int r = parallelism + (int)(ctl >> AC_SHIFT);
        return (r <= 0) ? 0 : r; 
    }
    public int getActiveThreadCount() {
        int r = parallelism + (int)(ctl >> AC_SHIFT) + blockedCount;
        return (r <= 0) ? 0 : r; 
    }
    public boolean isQuiescent() {
        return parallelism + (int)(ctl >> AC_SHIFT) + blockedCount == 0;
    }
    public long getStealCount() {
        return stealCount;
    }
    public long getQueuedTaskCount() {
        long count = 0;
        ForkJoinWorkerThread[] ws;
        if ((short)(ctl >>> TC_SHIFT) > -parallelism &&
            (ws = workers) != null) {
            for (ForkJoinWorkerThread w : ws)
                if (w != null)
                    count -= w.queueBase - w.queueTop; 
        }
        return count;
    }
    public int getQueuedSubmissionCount() {
        return -queueBase + queueTop;
    }
    public boolean hasQueuedSubmissions() {
        return queueBase != queueTop;
    }
    protected ForkJoinTask<?> pollSubmission() {
        ForkJoinTask<?> t; ForkJoinTask<?>[] q; int b, i;
        while ((b = queueBase) != queueTop &&
               (q = submissionQueue) != null &&
               (i = (q.length - 1) & b) >= 0) {
            long u = (i << ASHIFT) + ABASE;
            if ((t = q[i]) != null &&
                queueBase == b &&
                UNSAFE.compareAndSwapObject(q, u, t, null)) {
                queueBase = b + 1;
                return t;
            }
        }
        return null;
    }
    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        while (queueBase != queueTop) {
            ForkJoinTask<?> t = pollSubmission();
            if (t != null) {
                c.add(t);
                ++count;
            }
        }
        ForkJoinWorkerThread[] ws;
        if ((short)(ctl >>> TC_SHIFT) > -parallelism &&
            (ws = workers) != null) {
            for (ForkJoinWorkerThread w : ws)
                if (w != null)
                    count += w.drainTasksTo(c);
        }
        return count;
    }
    public String toString() {
        long st = getStealCount();
        long qt = getQueuedTaskCount();
        long qs = getQueuedSubmissionCount();
        int pc = parallelism;
        long c = ctl;
        int tc = pc + (short)(c >>> TC_SHIFT);
        int rc = pc + (int)(c >> AC_SHIFT);
        if (rc < 0) 
            rc = 0;
        int ac = rc + blockedCount;
        String level;
        if ((c & STOP_BIT) != 0)
            level = (tc == 0) ? "Terminated" : "Terminating";
        else
            level = shutdown ? "Shutting down" : "Running";
        return super.toString() +
            "[" + level +
            ", parallelism = " + pc +
            ", size = " + tc +
            ", active = " + ac +
            ", running = " + rc +
            ", steals = " + st +
            ", tasks = " + qt +
            ", submissions = " + qs +
            "]";
    }
    public void shutdown() {
        checkPermission();
        shutdown = true;
        tryTerminate(false);
    }
    public List<Runnable> shutdownNow() {
        checkPermission();
        shutdown = true;
        tryTerminate(true);
        return Collections.emptyList();
    }
    public boolean isTerminated() {
        long c = ctl;
        return ((c & STOP_BIT) != 0L &&
                (short)(c >>> TC_SHIFT) == -parallelism);
    }
    public boolean isTerminating() {
        long c = ctl;
        return ((c & STOP_BIT) != 0L &&
                (short)(c >>> TC_SHIFT) != -parallelism);
    }
    final boolean isAtLeastTerminating() {
        return (ctl & STOP_BIT) != 0L;
    }
    public boolean isShutdown() {
        return shutdown;
    }
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.submissionLock;
        lock.lock();
        try {
            for (;;) {
                if (isTerminated())
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            lock.unlock();
        }
    }
    public static interface ManagedBlocker {
        boolean block() throws InterruptedException;
        boolean isReleasable();
    }
    public static void managedBlock(ManagedBlocker blocker)
        throws InterruptedException {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread w = (ForkJoinWorkerThread) t;
            w.pool.awaitBlocker(blocker);
        }
        else {
            do {} while (!blocker.isReleasable() && !blocker.block());
        }
    }
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return (RunnableFuture<T>) ForkJoinTask.adapt(runnable, value);
    }
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return (RunnableFuture<T>) ForkJoinTask.adapt(callable);
    }
    private static final sun.misc.Unsafe UNSAFE;
    private static final long ctlOffset;
    private static final long stealCountOffset;
    private static final long blockedCountOffset;
    private static final long quiescerCountOffset;
    private static final long scanGuardOffset;
    private static final long nextWorkerNumberOffset;
    private static final long ABASE;
    private static final int ASHIFT;
    static {
        poolNumberGenerator = new AtomicInteger();
        workerSeedGenerator = new Random();
        modifyThreadPermission = new RuntimePermission("modifyThread");
        defaultForkJoinWorkerThreadFactory =
            new DefaultForkJoinWorkerThreadFactory();
        int s;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class k = ForkJoinPool.class;
            ctlOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("ctl"));
            stealCountOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("stealCount"));
            blockedCountOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("blockedCount"));
            quiescerCountOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("quiescerCount"));
            scanGuardOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("scanGuard"));
            nextWorkerNumberOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("nextWorkerNumber"));
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
