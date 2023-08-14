public class ConcurrentLinkedQueue<E> extends AbstractQueue<E>
        implements Queue<E>, java.io.Serializable {
    private static final long serialVersionUID = 196745693267521676L;
    private static class Node<E> {
        private volatile E item;
        private volatile Node<E> next;
        private static final
            AtomicReferenceFieldUpdater<Node, Node>
            nextUpdater =
            AtomicReferenceFieldUpdater.newUpdater
            (Node.class, Node.class, "next");
        private static final
            AtomicReferenceFieldUpdater<Node, Object>
            itemUpdater =
            AtomicReferenceFieldUpdater.newUpdater
            (Node.class, Object.class, "item");
        Node(E item) { setItem(item); }
        E getItem() {
            return item;
        }
        boolean casItem(E cmp, E val) {
            return itemUpdater.compareAndSet(this, cmp, val);
        }
        void setItem(E val) {
            itemUpdater.set(this, val);
        }
        Node<E> getNext() {
            return next;
        }
        boolean casNext(Node<E> cmp, Node<E> val) {
            return nextUpdater.compareAndSet(this, cmp, val);
        }
        void setNext(Node<E> val) {
            nextUpdater.set(this, val);
    }
    }
    private static final
        AtomicReferenceFieldUpdater<ConcurrentLinkedQueue, Node>
        tailUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (ConcurrentLinkedQueue.class, Node.class, "tail");
    private static final
        AtomicReferenceFieldUpdater<ConcurrentLinkedQueue, Node>
        headUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (ConcurrentLinkedQueue.class,  Node.class, "head");
    private boolean casTail(Node<E> cmp, Node<E> val) {
        return tailUpdater.compareAndSet(this, cmp, val);
    }
    private boolean casHead(Node<E> cmp, Node<E> val) {
        return headUpdater.compareAndSet(this, cmp, val);
    }
    private transient volatile Node<E> head = new Node<E>(null);
    private transient volatile Node<E> tail = head;
    public ConcurrentLinkedQueue() {}
    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        for (Iterator<? extends E> it = c.iterator(); it.hasNext();)
            add(it.next());
    }
    public boolean add(E e) {
        return offer(e);
    }
    private static final int HOPS = 1;
    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p))
            h.setNext(h);
    }
    final Node<E> succ(Node<E> p) {
        Node<E> next = p.getNext();
        return (p == next) ? head : next;
    }
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        Node<E> n = new Node<E>(e);
        retry:
        for (;;) {
            Node<E> t = tail;
            Node<E> p = t;
            for (int hops = 0; ; hops++) {
                Node<E> next = succ(p);
                if (next != null) {
                    if (hops > HOPS && t != tail)
                        continue retry;
                    p = next;
                } else if (p.casNext(null, n)) {
                    if (hops >= HOPS)
                        casTail(t, n);  
                    return true;
                } else {
                    p = succ(p);
                }
            }
        }
    }
    public E poll() {
        Node<E> h = head;
        Node<E> p = h;
        for (int hops = 0; ; hops++) {
            E item = p.getItem();
            if (item != null && p.casItem(item, null)) {
                if (hops >= HOPS) {
                    Node<E> q = p.getNext();
                    updateHead(h, (q != null) ? q : p);
                }
                return item;
            }
            Node<E> next = succ(p);
            if (next == null) {
                updateHead(h, p);
                break;
            }
            p = next;
        }
        return null;
    }
    public E peek() {
        Node<E> h = head;
        Node<E> p = h;
        E item;
        for (;;) {
            item = p.getItem();
            if (item != null)
                break;
            Node<E> next = succ(p);
            if (next == null) {
                break;
            }
            p = next;
        }
        updateHead(h, p);
        return item;
    }
    Node<E> first() {
        Node<E> h = head;
        Node<E> p = h;
        Node<E> result;
        for (;;) {
            E item = p.getItem();
            if (item != null) {
                result = p;
                break;
            }
            Node<E> next = succ(p);
            if (next == null) {
                result = null;
                break;
            }
            p = next;
        }
        updateHead(h, p);
        return result;
    }
    public boolean isEmpty() {
        return first() == null;
    }
    public int size() {
        int count = 0;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            if (p.getItem() != null) {
                if (++count == Integer.MAX_VALUE)
                    break;
            }
        }
        return count;
    }
    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.getItem();
            if (item != null &&
                o.equals(item))
                return true;
        }
        return false;
    }
    public boolean remove(Object o) {
        if (o == null) return false;
        Node<E> pred = null;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.getItem();
            if (item != null && o.equals(item) && p.casItem(item, null)) {
                Node<E> next = succ(p);
                if (pred != null && next != null)
                    pred.casNext(p, next);
                return true;
            }
            pred = p;
        }
        return false;
    }
    public Object[] toArray() {
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.getItem();
            if (item != null)
                al.add(item);
        }
        return al.toArray();
    }
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int k = 0;
        Node<E> p;
        for (p = first(); p != null && k < a.length; p = succ(p)) {
            E item = p.getItem();
            if (item != null)
                a[k++] = (T)item;
        }
        if (p == null) {
            if (k < a.length)
                a[k] = null;
            return a;
        }
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> q = first(); q != null; q = succ(q)) {
            E item = q.getItem();
            if (item != null)
                al.add(item);
        }
        return al.toArray(a);
    }
    public Iterator<E> iterator() {
        return new Itr();
    }
    private class Itr implements Iterator<E> {
        private Node<E> nextNode;
        private E nextItem;
        private Node<E> lastRet;
        Itr() {
            advance();
        }
        private E advance() {
            lastRet = nextNode;
            E x = nextItem;
            Node<E> pred, p;
            if (nextNode == null) {
                p = first();
                pred = null;
            } else {
                pred = nextNode;
                p = succ(nextNode);
            }
            for (;;) {
                if (p == null) {
                    nextNode = null;
                    nextItem = null;
                    return x;
                }
                E item = p.getItem();
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    return x;
                } else {
                    Node<E> next = succ(p);
                    if (pred != null && next != null)
                        pred.casNext(p, next);
                    p = next;
                }
            }
        }
        public boolean hasNext() {
            return nextNode != null;
        }
        public E next() {
            if (nextNode == null) throw new NoSuchElementException();
            return advance();
        }
        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            l.setItem(null);
            lastRet = null;
        }
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            Object item = p.getItem();
            if (item != null)
                s.writeObject(item);
        }
        s.writeObject(null);
    }
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        head = new Node<E>(null);
        tail = head;
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E)s.readObject();
            if (item == null)
                break;
            else
                offer(item);
        }
    }
}
