public class LinkedBlockingQueue<E> extends AbstractQueue<E>
        implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -6903933977591709194L;
    static class Node<E> {
        volatile E item;
        Node<E> next;
        Node(E x) { item = x; }
    }
    private final int capacity;
    private final AtomicInteger count = new AtomicInteger(0);
    private transient Node<E> head;
    private transient Node<E> last;
    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();
    private final ReentrantLock putLock = new ReentrantLock();
    private final Condition notFull = putLock.newCondition();
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }
    private void insert(E x) {
        last = last.next = new Node<E>(x);
    }
    private E extract() {
        Node<E> h = head;
        Node<E> first = h.next;
        h.next = null; 
        head = first;
        E x = first.item;
        first.item = null;
        return x;
    }
    private void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }
    private void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }
    public LinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }
    public LinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }
    public LinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        for (E e : c)
            add(e);
    }
    public int size() {
        return count.get();
    }
    public int remainingCapacity() {
        return capacity - count.get();
    }
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            try {
                while (count.get() == capacity)
                    notFull.await();
            } catch (InterruptedException ie) {
                notFull.signal(); 
                throw ie;
            }
            insert(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
    }
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            for (;;) {
                if (count.get() < capacity) {
                    insert(e);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity)
                        notFull.signal();
                    break;
                }
                if (nanos <= 0)
                    return false;
                try {
                    nanos = notFull.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notFull.signal(); 
                    throw ie;
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return true;
    }
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        final AtomicInteger count = this.count;
        if (count.get() == capacity)
            return false;
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                insert(e);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                    notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return c >= 0;
    }
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            try {
                while (count.get() == 0)
                    notEmpty.await();
            } catch (InterruptedException ie) {
                notEmpty.signal(); 
                throw ie;
            }
            x = extract();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            for (;;) {
                if (count.get() > 0) {
                    x = extract();
                    c = count.getAndDecrement();
                    if (c > 1)
                        notEmpty.signal();
                    break;
                }
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
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0)
            return null;
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = extract();
                c = count.getAndDecrement();
                if (c > 1)
                    notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }
    public E peek() {
        if (count.get() == 0)
            return null;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            Node<E> first = head.next;
            if (first == null)
                return null;
            else
                return first.item;
        } finally {
            takeLock.unlock();
        }
    }
    public boolean remove(Object o) {
        if (o == null) return false;
        boolean removed = false;
        fullyLock();
        try {
            Node<E> trail = head;
            Node<E> p = head.next;
            while (p != null) {
                if (o.equals(p.item)) {
                    removed = true;
                    break;
                }
                trail = p;
                p = p.next;
            }
            if (removed) {
                p.item = null;
                trail.next = p.next;
                if (last == p)
                    last = trail;
                if (count.getAndDecrement() == capacity)
                    notFull.signalAll();
            }
        } finally {
            fullyUnlock();
        }
        return removed;
    }
    public Object[] toArray() {
        fullyLock();
        try {
            int size = count.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next)
                a[k++] = p.item;
            return a;
        } finally {
            fullyUnlock();
        }
    }
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = count.get();
            if (a.length < size)
                a = (T[])java.lang.reflect.Array.newInstance
                    (a.getClass().getComponentType(), size);
            int k = 0;
            for (Node p = head.next; p != null; p = p.next)
                a[k++] = (T)p.item;
            if (a.length > k)
                a[k] = null;
            return a;
        } finally {
            fullyUnlock();
        }
    }
    public String toString() {
        fullyLock();
        try {
            return super.toString();
        } finally {
            fullyUnlock();
        }
    }
    public void clear() {
        fullyLock();
        try {
            head.next = null;
            assert head.item == null;
            last = head;
            if (count.getAndSet(0) == capacity)
                notFull.signalAll();
        } finally {
            fullyUnlock();
        }
    }
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        Node<E> first;
        fullyLock();
        try {
            first = head.next;
            head.next = null;
            assert head.item == null;
            last = head;
            if (count.getAndSet(0) == capacity)
                notFull.signalAll();
        } finally {
            fullyUnlock();
        }
        int n = 0;
        for (Node<E> p = first; p != null; p = p.next) {
            c.add(p.item);
            p.item = null;
            ++n;
        }
        return n;
    }
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        fullyLock();
        try {
            int n = 0;
            Node<E> p = head.next;
            while (p != null && n < maxElements) {
                c.add(p.item);
                p.item = null;
                p = p.next;
                ++n;
            }
            if (n != 0) {
                head.next = p;
                assert head.item == null;
                if (p == null)
                    last = head;
                if (count.getAndAdd(-n) == capacity)
                    notFull.signalAll();
            }
            return n;
        } finally {
            fullyUnlock();
        }
    }
    public Iterator<E> iterator() {
      return new Itr();
    }
    private class Itr implements Iterator<E> {
        private Node<E> current;
        private Node<E> lastRet;
        private E currentElement;
        Itr() {
            final ReentrantLock putLock = LinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = LinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                current = head.next;
                if (current != null)
                    currentElement = current.item;
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }
        public boolean hasNext() {
            return current != null;
        }
        public E next() {
            final ReentrantLock putLock = LinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = LinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                if (current == null)
                    throw new NoSuchElementException();
                E x = currentElement;
                lastRet = current;
                current = current.next;
                if (current != null)
                    currentElement = current.item;
                return x;
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }
        public void remove() {
            if (lastRet == null)
                throw new IllegalStateException();
            final ReentrantLock putLock = LinkedBlockingQueue.this.putLock;
            final ReentrantLock takeLock = LinkedBlockingQueue.this.takeLock;
            putLock.lock();
            takeLock.lock();
            try {
                Node<E> node = lastRet;
                lastRet = null;
                Node<E> trail = head;
                Node<E> p = head.next;
                while (p != null && p != node) {
                    trail = p;
                    p = p.next;
                }
                if (p == node) {
                    p.item = null;
                    trail.next = p.next;
                    if (last == p)
                        last = trail;
                    int c = count.getAndDecrement();
                    if (c == capacity)
                        notFull.signalAll();
                }
            } finally {
                takeLock.unlock();
                putLock.unlock();
            }
        }
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        fullyLock();
        try {
            s.defaultWriteObject();
            for (Node<E> p = head.next; p != null; p = p.next)
                s.writeObject(p.item);
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        count.set(0);
        last = head = new Node<E>(null);
        for (;;) {
            E item = (E)s.readObject();
            if (item == null)
                break;
            add(item);
        }
    }
}
