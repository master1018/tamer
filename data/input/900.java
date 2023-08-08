public final class EmptyIterator<E> implements Iterator<E> {
    @SuppressWarnings("rawtypes")
    private static final EmptyIterator INSTANCE = new EmptyIterator();
    @SuppressWarnings("unchecked")
    public static <E> EmptyIterator<E> createEmptyIterator() {
        return INSTANCE;
    }
    private EmptyIterator() {
    }
    @Override
    public boolean hasNext() {
        return false;
    }
    @Override
    public E next() {
        throw new NoSuchElementException("no elements in empty iterator");
    }
    @Override
    public void remove() {
    }
}
