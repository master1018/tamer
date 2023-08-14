public final class ReadOnlyIterator<E> implements Iterator<E> {
    private final Iterator<E> it;
    public ReadOnlyIterator(Iterator<E> it) {
        if (it == null) {
            throw new NullPointerException();
        }
        this.it = it;
    }
    public void remove() {
        throw new UnsupportedOperationException(Messages.getString("awt.50")); 
    }
    public boolean hasNext() {
        return it.hasNext();
    }
    public E next() {
        return it.next();
    }
}
