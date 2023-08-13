public class Phaser {
    private volatile long state;
    private static final int  MAX_PARTIES     = 0xffff;
    private static final int  MAX_PHASE       = Integer.MAX_VALUE;
    private static final int  PARTIES_SHIFT   = 16;
    private static final int  PHASE_SHIFT     = 32;
    private static final int  UNARRIVED_MASK  = 0xffff;      
    private static final long PARTIES_MASK    = 0xffff0000L; 
    private static final long TERMINATION_BIT = 1L << 63;
    private static final int  ONE_ARRIVAL     = 1;
    private static final int  ONE_PARTY       = 1 << PARTIES_SHIFT;
    private static final int  EMPTY           = 1;
    private static int unarrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 : counts & UNARRIVED_MASK;
    }
    private static int partiesOf(long s) {
        return (int)s >>> PARTIES_SHIFT;
    }
    private static int phaseOf(long s) {
        return (int)(s >>> PHASE_SHIFT);
    }
    private static int arrivedOf(long s) {
        int counts = (int)s;
        return (counts == EMPTY) ? 0 :
            (counts >>> PARTIES_SHIFT) - (counts & UNARRIVED_MASK);
    }
    private final Phaser parent;
    private final Phaser root;
    private final AtomicReference<QNode> evenQ;
    private final AtomicReference<QNode> oddQ;
    private AtomicReference<QNode> queueFor(int phase) {
        return ((phase & 1) == 0) ? evenQ : oddQ;
    }
    private String badArrive(long s) {
        return "Attempted arrival of unregistered party for " +
            stateToString(s);
    }
    private String badRegister(long s) {
        return "Attempt to register more than " +
            MAX_PARTIES + " parties for " + stateToString(s);
    }
    private int doArrive(boolean deregister) {
        int adj = deregister ? ONE_ARRIVAL|ONE_PARTY : ONE_ARRIVAL;
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            int counts = (int)s;
            int unarrived = (counts & UNARRIVED_MASK) - 1;
            if (phase < 0)
                return phase;
            else if (counts == EMPTY || unarrived < 0) {
                if (root == this || reconcileState() == s)
                    throw new IllegalStateException(badArrive(s));
            }
            else if (UNSAFE.compareAndSwapLong(this, stateOffset, s, s-=adj)) {
                if (unarrived == 0) {
                    long n = s & PARTIES_MASK;  
                    int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                    if (root != this)
                        return parent.doArrive(nextUnarrived == 0);
                    if (onAdvance(phase, nextUnarrived))
                        n |= TERMINATION_BIT;
                    else if (nextUnarrived == 0)
                        n |= EMPTY;
                    else
                        n |= nextUnarrived;
                    n |= (long)((phase + 1) & MAX_PHASE) << PHASE_SHIFT;
                    UNSAFE.compareAndSwapLong(this, stateOffset, s, n);
                    releaseWaiters(phase);
                }
                return phase;
            }
        }
    }
    private int doRegister(int registrations) {
        long adj = ((long)registrations << PARTIES_SHIFT) | registrations;
        final Phaser parent = this.parent;
        int phase;
        for (;;) {
            long s = state;
            int counts = (int)s;
            int parties = counts >>> PARTIES_SHIFT;
            int unarrived = counts & UNARRIVED_MASK;
            if (registrations > MAX_PARTIES - parties)
                throw new IllegalStateException(badRegister(s));
            else if ((phase = (int)(s >>> PHASE_SHIFT)) < 0)
                break;
            else if (counts != EMPTY) {             
                if (parent == null || reconcileState() == s) {
                    if (unarrived == 0)             
                        root.internalAwaitAdvance(phase, null);
                    else if (UNSAFE.compareAndSwapLong(this, stateOffset,
                                                       s, s + adj))
                        break;
                }
            }
            else if (parent == null) {              
                long next = ((long)phase << PHASE_SHIFT) | adj;
                if (UNSAFE.compareAndSwapLong(this, stateOffset, s, next))
                    break;
            }
            else {
                synchronized (this) {               
                    if (state == s) {               
                        parent.doRegister(1);
                        do {                        
                            phase = (int)(root.state >>> PHASE_SHIFT);
                        } while (!UNSAFE.compareAndSwapLong
                                 (this, stateOffset, state,
                                  ((long)phase << PHASE_SHIFT) | adj));
                        break;
                    }
                }
            }
        }
        return phase;
    }
    private long reconcileState() {
        final Phaser root = this.root;
        long s = state;
        if (root != this) {
            int phase, u, p;
            while ((phase = (int)(root.state >>> PHASE_SHIFT)) !=
                   (int)(s >>> PHASE_SHIFT) &&
                   !UNSAFE.compareAndSwapLong
                   (this, stateOffset, s,
                    s = (((long)phase << PHASE_SHIFT) |
                         (s & PARTIES_MASK) |
                         ((p = (int)s >>> PARTIES_SHIFT) == 0 ? EMPTY :
                          (u = (int)s & UNARRIVED_MASK) == 0 ? p : u))))
                s = state;
        }
        return s;
    }
    public Phaser() {
        this(null, 0);
    }
    public Phaser(int parties) {
        this(null, parties);
    }
    public Phaser(Phaser parent) {
        this(parent, 0);
    }
    public Phaser(Phaser parent, int parties) {
        if (parties >>> PARTIES_SHIFT != 0)
            throw new IllegalArgumentException("Illegal number of parties");
        int phase = 0;
        this.parent = parent;
        if (parent != null) {
            final Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0)
                phase = parent.doRegister(1);
        }
        else {
            this.root = this;
            this.evenQ = new AtomicReference<QNode>();
            this.oddQ = new AtomicReference<QNode>();
        }
        this.state = (parties == 0) ? (long)EMPTY :
            ((long)phase << PHASE_SHIFT) |
            ((long)parties << PARTIES_SHIFT) |
            ((long)parties);
    }
    public int register() {
        return doRegister(1);
    }
    public int bulkRegister(int parties) {
        if (parties < 0)
            throw new IllegalArgumentException();
        if (parties == 0)
            return getPhase();
        return doRegister(parties);
    }
    public int arrive() {
        return doArrive(false);
    }
    public int arriveAndDeregister() {
        return doArrive(true);
    }
    public int arriveAndAwaitAdvance() {
        final Phaser root = this.root;
        for (;;) {
            long s = (root == this) ? state : reconcileState();
            int phase = (int)(s >>> PHASE_SHIFT);
            int counts = (int)s;
            int unarrived = (counts & UNARRIVED_MASK) - 1;
            if (phase < 0)
                return phase;
            else if (counts == EMPTY || unarrived < 0) {
                if (reconcileState() == s)
                    throw new IllegalStateException(badArrive(s));
            }
            else if (UNSAFE.compareAndSwapLong(this, stateOffset, s,
                                               s -= ONE_ARRIVAL)) {
                if (unarrived != 0)
                    return root.internalAwaitAdvance(phase, null);
                if (root != this)
                    return parent.arriveAndAwaitAdvance();
                long n = s & PARTIES_MASK;  
                int nextUnarrived = (int)n >>> PARTIES_SHIFT;
                if (onAdvance(phase, nextUnarrived))
                    n |= TERMINATION_BIT;
                else if (nextUnarrived == 0)
                    n |= EMPTY;
                else
                    n |= nextUnarrived;
                int nextPhase = (phase + 1) & MAX_PHASE;
                n |= (long)nextPhase << PHASE_SHIFT;
                if (!UNSAFE.compareAndSwapLong(this, stateOffset, s, n))
                    return (int)(state >>> PHASE_SHIFT); 
                releaseWaiters(phase);
                return nextPhase;
            }
        }
    }
    public int awaitAdvance(int phase) {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase)
            return root.internalAwaitAdvance(phase, null);
        return p;
    }
    public int awaitAdvanceInterruptibly(int phase)
        throws InterruptedException {
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, 0L);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
        }
        return p;
    }
    public int awaitAdvanceInterruptibly(int phase,
                                         long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        final Phaser root = this.root;
        long s = (root == this) ? state : reconcileState();
        int p = (int)(s >>> PHASE_SHIFT);
        if (phase < 0)
            return phase;
        if (p == phase) {
            QNode node = new QNode(this, phase, true, true, nanos);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted)
                throw new InterruptedException();
            else if (p == phase)
                throw new TimeoutException();
        }
        return p;
    }
    public void forceTermination() {
        final Phaser root = this.root;
        long s;
        while ((s = root.state) >= 0) {
            if (UNSAFE.compareAndSwapLong(root, stateOffset,
                                          s, s | TERMINATION_BIT)) {
                releaseWaiters(0);
                releaseWaiters(1);
                return;
            }
        }
    }
    public final int getPhase() {
        return (int)(root.state >>> PHASE_SHIFT);
    }
    public int getRegisteredParties() {
        return partiesOf(state);
    }
    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }
    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }
    public Phaser getParent() {
        return parent;
    }
    public Phaser getRoot() {
        return root;
    }
    public boolean isTerminated() {
        return root.state < 0L;
    }
    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }
    public String toString() {
        return stateToString(reconcileState());
    }
    private String stateToString(long s) {
        return super.toString() +
            "[phase = " + phaseOf(s) +
            " parties = " + partiesOf(s) +
            " arrived = " + arrivedOf(s) + "]";
    }
    private void releaseWaiters(int phase) {
        QNode q;   
        Thread t;  
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        while ((q = head.get()) != null &&
               q.phase != (int)(root.state >>> PHASE_SHIFT)) {
            if (head.compareAndSet(q, q.next) &&
                (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }
    private int abortWait(int phase) {
        AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
        for (;;) {
            Thread t;
            QNode q = head.get();
            int p = (int)(root.state >>> PHASE_SHIFT);
            if (q == null || ((t = q.thread) != null && q.phase == p))
                return p;
            if (head.compareAndSet(q, q.next) && t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
    }
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    static final int SPINS_PER_ARRIVAL = (NCPU < 2) ? 1 : 1 << 8;
    private int internalAwaitAdvance(int phase, QNode node) {
        releaseWaiters(phase-1);          
        boolean queued = false;           
        int lastUnarrived = 0;            
        int spins = SPINS_PER_ARRIVAL;
        long s;
        int p;
        while ((p = (int)((s = state) >>> PHASE_SHIFT)) == phase) {
            if (node == null) {           
                int unarrived = (int)s & UNARRIVED_MASK;
                if (unarrived != lastUnarrived &&
                    (lastUnarrived = unarrived) < NCPU)
                    spins += SPINS_PER_ARRIVAL;
                boolean interrupted = Thread.interrupted();
                if (interrupted || --spins < 0) { 
                    node = new QNode(this, phase, false, false, 0L);
                    node.wasInterrupted = interrupted;
                }
            }
            else if (node.isReleasable()) 
                break;
            else if (!queued) {           
                AtomicReference<QNode> head = (phase & 1) == 0 ? evenQ : oddQ;
                QNode q = node.next = head.get();
                if ((q == null || q.phase == phase) &&
                    (int)(state >>> PHASE_SHIFT) == phase) 
                    queued = head.compareAndSet(q, node);
            }
            else {
                try {
                    ForkJoinPool.managedBlock(node);
                } catch (InterruptedException ie) {
                    node.wasInterrupted = true;
                }
            }
        }
        if (node != null) {
            if (node.thread != null)
                node.thread = null;       
            if (node.wasInterrupted && !node.interruptible)
                Thread.currentThread().interrupt();
            if (p == phase && (p = (int)(state >>> PHASE_SHIFT)) == phase)
                return abortWait(phase); 
        }
        releaseWaiters(phase);
        return p;
    }
    static final class QNode implements ForkJoinPool.ManagedBlocker {
        final Phaser phaser;
        final int phase;
        final boolean interruptible;
        final boolean timed;
        boolean wasInterrupted;
        long nanos;
        long lastTime;
        volatile Thread thread; 
        QNode next;
        QNode(Phaser phaser, int phase, boolean interruptible,
              boolean timed, long nanos) {
            this.phaser = phaser;
            this.phase = phase;
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.timed = timed;
            this.lastTime = timed ? System.nanoTime() : 0L;
            thread = Thread.currentThread();
        }
        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (phaser.getPhase() != phase) {
                thread = null;
                return true;
            }
            if (Thread.interrupted())
                wasInterrupted = true;
            if (wasInterrupted && interruptible) {
                thread = null;
                return true;
            }
            if (timed) {
                if (nanos > 0L) {
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                }
                if (nanos <= 0L) {
                    thread = null;
                    return true;
                }
            }
            return false;
        }
        public boolean block() {
            if (isReleasable())
                return true;
            else if (!timed)
                LockSupport.park(this);
            else if (nanos > 0)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class k = Phaser.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
