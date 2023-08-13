public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;
    static abstract class Transferer {
        abstract Object transfer(Object e, boolean timed, long nanos);
    }
    static final int NCPUS = Runtime.getRuntime().availableProcessors();
    static final int maxTimedSpins = (NCPUS < 2)? 0 : 32;
    static final int maxUntimedSpins = maxTimedSpins * 16;
    static final long spinForTimeoutThreshold = 1000L;
    static final class TransferStack extends Transferer {
        static final int REQUEST    = 0;
        static final int DATA       = 1;
        static final int FULFILLING = 2;
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }
        static final class SNode {
            volatile SNode next;        
            volatile SNode match;       
            volatile Thread waiter;     
            Object item;                
            int mode;
            SNode(Object item) {
                this.item = item;
            }
            static final AtomicReferenceFieldUpdater<SNode, SNode>
                nextUpdater = AtomicReferenceFieldUpdater.newUpdater
                (SNode.class, SNode.class, "next");
            boolean casNext(SNode cmp, SNode val) {
                return (cmp == next &&
                        nextUpdater.compareAndSet(this, cmp, val));
            }
            static final AtomicReferenceFieldUpdater<SNode, SNode>
                matchUpdater = AtomicReferenceFieldUpdater.newUpdater
                (SNode.class, SNode.class, "match");
            boolean tryMatch(SNode s) {
                if (match == null &&
                    matchUpdater.compareAndSet(this, null, s)) {
                    Thread w = waiter;
                    if (w != null) {    
                        waiter = null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return match == s;
            }
            void tryCancel() {
                matchUpdater.compareAndSet(this, null, this);
            }
            boolean isCancelled() {
                return match == this;
            }
        }
        volatile SNode head;
        static final AtomicReferenceFieldUpdater<TransferStack, SNode>
            headUpdater = AtomicReferenceFieldUpdater.newUpdater
            (TransferStack.class,  SNode.class, "head");
        boolean casHead(SNode h, SNode nh) {
            return h == head && headUpdater.compareAndSet(this, h, nh);
        }
        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }
        Object transfer(Object e, boolean timed, long nanos) {
            SNode s = null; 
            int mode = (e == null)? REQUEST : DATA;
            for (;;) {
                SNode h = head;
                if (h == null || h.mode == mode) {  
                    if (timed && nanos <= 0) {      
                        if (h != null && h.isCancelled())
                            casHead(h, h.next);     
                        else
                            return null;
                    } else if (casHead(h, s = snode(s, e, h, mode))) {
                        SNode m = awaitFulfill(s, timed, nanos);
                        if (m == s) {               
                            clean(s);
                            return null;
                        }
                        if ((h = head) != null && h.next == s)
                            casHead(h, s.next);     
                        return mode == REQUEST? m.item : s.item;
                    }
                } else if (!isFulfilling(h.mode)) { 
                    if (h.isCancelled())            
                        casHead(h, h.next);         
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) {
                        for (;;) { 
                            SNode m = s.next;       
                            if (m == null) {        
                                casHead(s, null);   
                                s = null;           
                                break;              
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) {
                                casHead(s, mn);     
                                return (mode == REQUEST)? m.item : s.item;
                            } else                  
                                s.casNext(m, mn);   
                        }
                    }
                } else {                            
                    SNode m = h.next;               
                    if (m == null)                  
                        casHead(h, null);           
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          
                            casHead(h, mn);         
                        else                        
                            h.casNext(m, mn);       
                    }
                }
            }
        }
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            long lastTime = (timed)? System.nanoTime() : 0;
            Thread w = Thread.currentThread();
            SNode h = head;
            int spins = (shouldSpin(s)?
                         (timed? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel();
                SNode m = s.match;
                if (m != null)
                    return m;
                if (timed) {
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                    if (nanos <= 0) {
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    spins = shouldSpin(s)? (spins-1) : 0;
                else if (s.waiter == null)
                    s.waiter = w; 
                else if (!timed)
                    LockSupport.park();
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(nanos);
            }
        }
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }
        void clean(SNode s) {
            s.item = null;   
            s.waiter = null; 
            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }
    }
    static final class TransferQueue extends Transferer {
        static final class QNode {
            volatile QNode next;          
            volatile Object item;         
            volatile Thread waiter;       
            final boolean isData;
            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }
            static final AtomicReferenceFieldUpdater<QNode, QNode>
                nextUpdater = AtomicReferenceFieldUpdater.newUpdater
                (QNode.class, QNode.class, "next");
            boolean casNext(QNode cmp, QNode val) {
                return (next == cmp &&
                        nextUpdater.compareAndSet(this, cmp, val));
            }
            static final AtomicReferenceFieldUpdater<QNode, Object>
                itemUpdater = AtomicReferenceFieldUpdater.newUpdater
                (QNode.class, Object.class, "item");
            boolean casItem(Object cmp, Object val) {
                return (item == cmp &&
                        itemUpdater.compareAndSet(this, cmp, val));
            }
            void tryCancel(Object cmp) {
                itemUpdater.compareAndSet(this, cmp, this);
            }
            boolean isCancelled() {
                return item == this;
            }
            boolean isOffList() {
                return next == this;
            }
        }
        transient volatile QNode head;
        transient volatile QNode tail;
        transient volatile QNode cleanMe;
        TransferQueue() {
            QNode h = new QNode(null, false); 
            head = h;
            tail = h;
        }
        static final AtomicReferenceFieldUpdater<TransferQueue, QNode>
            headUpdater = AtomicReferenceFieldUpdater.newUpdater
            (TransferQueue.class,  QNode.class, "head");
        void advanceHead(QNode h, QNode nh) {
            if (h == head && headUpdater.compareAndSet(this, h, nh))
                h.next = h; 
        }
        static final AtomicReferenceFieldUpdater<TransferQueue, QNode>
            tailUpdater = AtomicReferenceFieldUpdater.newUpdater
            (TransferQueue.class, QNode.class, "tail");
        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                tailUpdater.compareAndSet(this, t, nt);
        }
        static final AtomicReferenceFieldUpdater<TransferQueue, QNode>
            cleanMeUpdater = AtomicReferenceFieldUpdater.newUpdater
            (TransferQueue.class, QNode.class, "cleanMe");
        boolean casCleanMe(QNode cmp, QNode val) {
            return (cleanMe == cmp &&
                    cleanMeUpdater.compareAndSet(this, cmp, val));
        }
        Object transfer(Object e, boolean timed, long nanos) {
            QNode s = null; 
            boolean isData = (e != null);
            for (;;) {
                QNode t = tail;
                QNode h = head;
                if (t == null || h == null)         
                    continue;                       
                if (h == t || t.isData == isData) { 
                    QNode tn = t.next;
                    if (t != tail)                  
                        continue;
                    if (tn != null) {               
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);
                    if (!t.casNext(null, s))        
                        continue;
                    advanceTail(t, s);              
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {                   
                        clean(t, s);
                        return null;
                    }
                    if (!s.isOffList()) {           
                        advanceHead(t, s);          
                        if (x != null)              
                            s.item = s;
                        s.waiter = null;
                    }
                    return (x != null)? x : e;
                } else {                            
                    QNode m = h.next;               
                    if (t != tail || m == null || h != head)
                        continue;                   
                    Object x = m.item;
                    if (isData == (x != null) ||    
                        x == m ||                   
                        !m.casItem(x, e)) {         
                        advanceHead(h, m);          
                        continue;
                    }
                    advanceHead(h, m);              
                    LockSupport.unpark(m.waiter);
                    return (x != null)? x : e;
                }
            }
        }
        Object awaitFulfill(QNode s, Object e, boolean timed, long nanos) {
            long lastTime = (timed)? System.nanoTime() : 0;
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
                         (timed? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel(e);
                Object x = s.item;
                if (x != e)
                    return x;
                if (timed) {
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                    if (nanos <= 0) {
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park();
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(nanos);
            }
        }
        void clean(QNode pred, QNode s) {
            s.waiter = null; 
            while (pred.next == s) { 
                QNode h = head;
                QNode hn = h.next;   
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               
                        d == dp ||                 
                        !d.isCancelled() ||        
                        (d != t &&                 
                         (dn = d.next) != null &&  
                         dn != d &&                
                         dp.casNext(d, dn)))       
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      
                } else if (casCleanMe(null, pred))
                    return;          
            }
        }
    }
    private transient volatile Transferer transferer;
    public SynchronousQueue() {
        this(false);
    }
    public SynchronousQueue(boolean fair) {
        transferer = (fair)? new TransferQueue() : new TransferStack();
    }
    public void put(E o) throws InterruptedException {
        if (o == null) throw new NullPointerException();
        if (transferer.transfer(o, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }
    public boolean offer(E o, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (o == null) throw new NullPointerException();
        if (transferer.transfer(o, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }
    public E take() throws InterruptedException {
        Object e = transferer.transfer(null, false, 0);
        if (e != null)
            return (E)e;
        Thread.interrupted();
        throw new InterruptedException();
    }
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        Object e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return (E)e;
        throw new InterruptedException();
    }
    public E poll() {
        return (E)transferer.transfer(null, true, 0);
    }
    public boolean isEmpty() {
        return true;
    }
    public int size() {
        return 0;
    }
    public int remainingCapacity() {
        return 0;
    }
    public void clear() {
    }
    public boolean contains(Object o) {
        return false;
    }
    public boolean remove(Object o) {
        return false;
    }
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }
    public boolean removeAll(Collection<?> c) {
        return false;
    }
    public boolean retainAll(Collection<?> c) {
        return false;
    }
    public E peek() {
        return null;
    }
    static class EmptyIterator<E> implements Iterator<E> {
        public boolean hasNext() {
            return false;
        }
        public E next() {
            throw new NoSuchElementException();
        }
        public void remove() {
            throw new IllegalStateException();
        }
    }
    public Iterator<E> iterator() {
        return new EmptyIterator<E>();
    }
    public Object[] toArray() {
        return new Object[0];
    }
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while ( (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        E e;
        while (n < maxElements && (e = poll()) != null) {
            c.add(e);
            ++n;
        }
        return n;
    }
    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue();
        else
            transferer = new TransferStack();
    }
}
