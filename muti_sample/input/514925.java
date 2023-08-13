public abstract class AbstractQueue<E> extends AbstractCollection<E> implements
        Queue<E> {
    protected AbstractQueue() {
        super();
    }
    @Override
    public boolean add(E o) {
        if (null == o) {
            throw new NullPointerException();
        }
        if (offer(o)) {
            return true;
        }
        throw new IllegalStateException();
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (null == c) {
            throw new NullPointerException();
        }
        if (this == c) {
            throw new IllegalArgumentException();
        }
        return super.addAll(c);
    }
    public E remove() {
        E o = poll();
        if (null == o) {
            throw new NoSuchElementException();
        }
        return o;
    }
    public E element() {
        E o = peek();
        if (null == o) {
            throw new NoSuchElementException();
        }
        return o;
    }
    @Override
    public void clear() {
        E o;
        do {
            o = poll();
        } while (null != o);
    }
}
