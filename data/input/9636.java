public class ListBuffer<A> extends AbstractQueue<A> {
    public static <T> ListBuffer<T> lb() {
        return new ListBuffer<T>();
    }
    public static <T> ListBuffer<T> of(T x) {
        ListBuffer<T> lb = new ListBuffer<T>();
        lb.add(x);
        return lb;
    }
    public List<A> elems;
    public List<A> last;
    public int count;
    public boolean shared;
    public ListBuffer() {
        clear();
    }
    public final void clear() {
        this.elems = new List<A>(null,null);
        this.last = this.elems;
        count = 0;
        shared = false;
    }
    public int length() {
        return count;
    }
    public int size() {
        return count;
    }
    public boolean isEmpty() {
        return count == 0;
    }
    public boolean nonEmpty() {
        return count != 0;
    }
    private void copy() {
        List<A> p = elems = new List<A>(elems.head, elems.tail);
        while (true) {
            List<A> tail = p.tail;
            if (tail == null) break;
            tail = new List<A>(tail.head, tail.tail);
            p.setTail(tail);
            p = tail;
        }
        last = p;
        shared = false;
    }
    public ListBuffer<A> prepend(A x) {
        elems = elems.prepend(x);
        count++;
        return this;
    }
    public ListBuffer<A> append(A x) {
        x.getClass(); 
        if (shared) copy();
        last.head = x;
        last.setTail(new List<A>(null,null));
        last = last.tail;
        count++;
        return this;
    }
    public ListBuffer<A> appendList(List<A> xs) {
        while (xs.nonEmpty()) {
            append(xs.head);
            xs = xs.tail;
        }
        return this;
    }
    public ListBuffer<A> appendList(ListBuffer<A> xs) {
        return appendList(xs.toList());
    }
    public ListBuffer<A> appendArray(A[] xs) {
        for (int i = 0; i < xs.length; i++) {
            append(xs[i]);
        }
        return this;
    }
    public List<A> toList() {
        shared = true;
        return elems;
    }
    public boolean contains(Object x) {
        return elems.contains(x);
    }
    public <T> T[] toArray(T[] vec) {
        return elems.toArray(vec);
    }
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }
    public A first() {
        return elems.head;
    }
    public A next() {
        A x = elems.head;
        if (elems != last) {
            elems = elems.tail;
            count--;
        }
        return x;
    }
    public Iterator<A> iterator() {
        return new Iterator<A>() {
            List<A> elems = ListBuffer.this.elems;
            public boolean hasNext() {
                return elems != last;
            }
            public A next() {
                if (elems == last)
                    throw new NoSuchElementException();
                A elem = elems.head;
                elems = elems.tail;
                return elem;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    public boolean add(A a) {
        append(a);
        return true;
    }
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    public boolean containsAll(Collection<?> c) {
        for (Object x: c) {
            if (!contains(x))
                return false;
        }
        return true;
    }
    public boolean addAll(Collection<? extends A> c) {
        for (A a: c)
            append(a);
        return true;
    }
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    public boolean offer(A a) {
        append(a);
        return true;
    }
    public A poll() {
        return next();
    }
    public A peek() {
        return first();
    }
}
