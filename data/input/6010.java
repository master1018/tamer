public class List<A> extends AbstractCollection<A> implements java.util.List<A> {
    public A head;
    public List<A> tail;
    List(A head, List<A> tail) {
        this.tail = tail;
        this.head = head;
    }
    @SuppressWarnings("unchecked")
    public static <A> List<A> nil() {
        return (List<A>)EMPTY_LIST;
    }
    private static List<?> EMPTY_LIST = new List<Object>(null,null) {
        public List<Object> setTail(List<Object> tail) {
            throw new UnsupportedOperationException();
        }
        public boolean isEmpty() {
            return true;
        }
    };
    public static <A> List<A> of(A x1) {
        return new List<A>(x1, List.<A>nil());
    }
    public static <A> List<A> of(A x1, A x2) {
        return new List<A>(x1, of(x2));
    }
    public static <A> List<A> of(A x1, A x2, A x3) {
        return new List<A>(x1, of(x2, x3));
    }
    @SuppressWarnings({"varargs", "unchecked"})
    public static <A> List<A> of(A x1, A x2, A x3, A... rest) {
        return new List<A>(x1, new List<A>(x2, new List<A>(x3, from(rest))));
    }
    public static <A> List<A> from(A[] array) {
        List<A> xs = nil();
        if (array != null)
            for (int i = array.length - 1; i >= 0; i--)
                xs = new List<A>(array[i], xs);
        return xs;
    }
    @Deprecated
    public static <A> List<A> fill(int len, A init) {
        List<A> l = nil();
        for (int i = 0; i < len; i++) l = new List<A>(init, l);
        return l;
    }
    @Override
    public boolean isEmpty() {
        return tail == null;
    }
    public boolean nonEmpty() {
        return tail != null;
    }
    public int length() {
        List<A> l = this;
        int len = 0;
        while (l.tail != null) {
            l = l.tail;
            len++;
        }
        return len;
    }
    @Override
    public int size() {
        return length();
    }
    public List<A> setTail(List<A> tail) {
        this.tail = tail;
        return tail;
    }
    public List<A> prepend(A x) {
        return new List<A>(x, this);
    }
    public List<A> prependList(List<A> xs) {
        if (this.isEmpty()) return xs;
        if (xs.isEmpty()) return this;
        if (xs.tail.isEmpty()) return prepend(xs.head);
        List<A> result = this;
        List<A> rev = xs.reverse();
        Assert.check(rev != xs);
        while (rev.nonEmpty()) {
            List<A> h = rev;
            rev = rev.tail;
            h.setTail(result);
            result = h;
        }
        return result;
    }
    public List<A> reverse() {
        if (isEmpty() || tail.isEmpty())
            return this;
        List<A> rev = nil();
        for (List<A> l = this; l.nonEmpty(); l = l.tail)
            rev = new List<A>(l.head, rev);
        return rev;
    }
    public List<A> append(A x) {
        return of(x).prependList(this);
    }
    public List<A> appendList(List<A> x) {
        return x.prependList(this);
    }
    public List<A> appendList(ListBuffer<A> x) {
        return appendList(x.toList());
    }
    @Override @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] vec) {
        int i = 0;
        List<A> l = this;
        Object[] dest = vec;
        while (l.nonEmpty() && i < vec.length) {
            dest[i] = l.head;
            l = l.tail;
            i++;
        }
        if (l.isEmpty()) {
            if (i < vec.length)
                vec[i] = null;
            return vec;
        }
        vec = (T[])Array.newInstance(vec.getClass().getComponentType(), size());
        return toArray(vec);
    }
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }
    public String toString(String sep) {
        if (isEmpty()) {
            return "";
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(head);
            for (List<A> l = tail; l.nonEmpty(); l = l.tail) {
                buf.append(sep);
                buf.append(l.head);
            }
            return buf.toString();
        }
    }
    @Override
    public String toString() {
        return toString(",");
    }
    @Override
    public int hashCode() {
        List<A> l = this;
        int h = 1;
        while (l.tail != null) {
            h = h * 31 + (l.head == null ? 0 : l.head.hashCode());
            l = l.tail;
        }
        return h;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof List<?>)
            return equals(this, (List<?>)other);
        if (other instanceof java.util.List<?>) {
            List<A> t = this;
            Iterator<?> oIter = ((java.util.List<?>) other).iterator();
            while (t.tail != null && oIter.hasNext()) {
                Object o = oIter.next();
                if ( !(t.head == null ? o == null : t.head.equals(o)))
                    return false;
                t = t.tail;
            }
            return (t.isEmpty() && !oIter.hasNext());
        }
        return false;
    }
    public static boolean equals(List<?> xs, List<?> ys) {
        while (xs.tail != null && ys.tail != null) {
            if (xs.head == null) {
                if (ys.head != null) return false;
            } else {
                if (!xs.head.equals(ys.head)) return false;
            }
            xs = xs.tail;
            ys = ys.tail;
        }
        return xs.tail == null && ys.tail == null;
    }
    @Override
    public boolean contains(Object x) {
        List<A> l = this;
        while (l.tail != null) {
            if (x == null) {
                if (l.head == null) return true;
            } else {
                if (l.head.equals(x)) return true;
            }
            l = l.tail;
        }
        return false;
    }
    public A last() {
        A last = null;
        List<A> t = this;
        while (t.tail != null) {
            last = t.head;
            t = t.tail;
        }
        return last;
    }
    @SuppressWarnings("unchecked")
    public static <T> List<T> convert(Class<T> klass, List<?> list) {
        if (list == null)
            return null;
        for (Object o : list)
            klass.cast(o);
        return (List<T>)list;
    }
    private static Iterator<?> EMPTYITERATOR = new Iterator<Object>() {
            public boolean hasNext() {
                return false;
            }
            public Object next() {
                throw new java.util.NoSuchElementException();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    @SuppressWarnings("unchecked")
    private static <A> Iterator<A> emptyIterator() {
        return (Iterator<A>)EMPTYITERATOR;
    }
    @Override
    public Iterator<A> iterator() {
        if (tail == null)
            return emptyIterator();
        return new Iterator<A>() {
            List<A> elems = List.this;
            public boolean hasNext() {
                return elems.tail != null;
            }
            public A next() {
                if (elems.tail == null)
                    throw new NoSuchElementException();
                A result = elems.head;
                elems = elems.tail;
                return result;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    public A get(int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException(String.valueOf(index));
        List<A> l = this;
        for (int i = index; i-- > 0 && !l.isEmpty(); l = l.tail)
            ;
        if (l.isEmpty())
            throw new IndexOutOfBoundsException("Index: " + index + ", " +
                                                "Size: " + size());
        return l.head;
    }
    public boolean addAll(int index, Collection<? extends A> c) {
        if (c.isEmpty())
            return false;
        throw new UnsupportedOperationException();
    }
    public A set(int index, A element) {
        throw new UnsupportedOperationException();
    }
    public void add(int index, A element) {
        throw new UnsupportedOperationException();
    }
    public A remove(int index) {
        throw new UnsupportedOperationException();
    }
    public int indexOf(Object o) {
        int i = 0;
        for (List<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o))
                return i;
        }
        return -1;
    }
    public int lastIndexOf(Object o) {
        int last = -1;
        int i = 0;
        for (List<A> l = this; l.tail != null; l = l.tail, i++) {
            if (l.head == null ? o == null : l.head.equals(o))
                last = i;
        }
        return last;
    }
    public ListIterator<A> listIterator() {
        return Collections.unmodifiableList(new ArrayList<A>(this)).listIterator();
    }
    public ListIterator<A> listIterator(int index) {
        return Collections.unmodifiableList(new ArrayList<A>(this)).listIterator(index);
    }
    public java.util.List<A> subList(int fromIndex, int toIndex) {
        if  (fromIndex < 0 || toIndex > size() || fromIndex > toIndex)
            throw new IllegalArgumentException();
        ArrayList<A> a = new ArrayList<A>(toIndex - fromIndex);
        int i = 0;
        for (List<A> l = this; l.tail != null; l = l.tail, i++) {
            if (i == toIndex)
                break;
            if (i >= fromIndex)
                a.add(l.head);
        }
        return Collections.unmodifiableList(a);
    }
}
