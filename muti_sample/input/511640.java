public class PriorityBlockingQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = 5595510919245408276L;
    private final PriorityQueue<E> q;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition notEmpty = lock.newCondition();
    public PriorityBlockingQueue() {
        q = new PriorityQueue<E>();
    }
    public PriorityBlockingQueue(int initialCapacity) {
        q = new PriorityQueue<E>(initialCapacity, null);
    }
    public PriorityBlockingQueue(int initialCapacity,
                                 Comparator<? super E> comparator) {
        q = new PriorityQueue<E>(initialCapacity, comparator);
    }
    public PriorityBlockingQueue(Collection<? extends E> c) {
        q = new PriorityQueue<E>(c);
    }
    public boolean add(E e) {
        return offer(e);
    }
    public boolean offer(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean ok = q.offer(e);
            assert ok;
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }
    public void put(E e) {
        offer(e); 
    }
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e); 
    }
    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.poll();
        } finally {
            lock.unlock();
        }
    }
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            try {
                while (q.size() == 0)
                    notEmpty.await();
            } catch (InterruptedException ie) {
                notEmpty.signal(); 
                throw ie;
            }
            E x = q.poll();
            assert x != null;
            return x;
        } finally {
            lock.unlock();
        }
    }
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                E x = q.poll();
                if (x != null)
                    return x;
                if (nanos <= 0)
                    return null;
                try {
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); 
                    throw ie;
                }
            }
        } finally {
            lock.unlock();
        }
    }
    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.peek();
        } finally {
            lock.unlock();
        }
    }
    public Comparator<? super E> comparator() {
        return q.comparator();
    }
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.size();
        } finally {
            lock.unlock();
        }
    }
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }
    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.remove(o);
        } finally {
            lock.unlock();
        }
    }
    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.contains(o);
        } finally {
            lock.unlock();
        }
    }
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray();
        } finally {
            lock.unlock();
        }
    }
    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toString();
        } finally {
            lock.unlock();
        }
    }
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            E e;
            while ( (e = q.poll()) != null) {
                c.add(e);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            E e;
            while (n < maxElements && (e = q.poll()) != null) {
                c.add(e);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            q.clear();
        } finally {
            lock.unlock();
        }
    }
    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray(a);
        } finally {
            lock.unlock();
        }
    }
    public Iterator<E> iterator() {
        return new Itr(toArray());
    }
    private class Itr implements Iterator<E> {
        final Object[] array; 
        int cursor;           
        int lastRet;          
        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }
        public boolean hasNext() {
            return cursor < array.length;
        }
        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E)array[cursor++];
        }
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            Object x = array[lastRet];
            lastRet = -1;
            lock.lock();
            try {
                for (Iterator it = q.iterator(); it.hasNext(); ) {
                    if (it.next() == x) {
                        it.remove();
                        return;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        lock.lock();
        try {
            s.defaultWriteObject();
        } finally {
            lock.unlock();
        }
    }
}
