public abstract class AbstractSequentialList<E> extends AbstractList<E> {
    protected AbstractSequentialList() {
        super();
    }
    @Override
    public void add(int location, E object) {
        listIterator(location).add(object);
    }
    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        ListIterator<E> it = listIterator(location);
        Iterator<? extends E> colIt = collection.iterator();
        int next = it.nextIndex();
        while (colIt.hasNext()) {
            it.add(colIt.next());
        }
        return next != it.nextIndex();
    }
    @Override
    public E get(int location) {
        try {
            return listIterator(location).next();
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }
    @Override
    public Iterator<E> iterator() {
        return listIterator(0);
    }
    @Override
    public abstract ListIterator<E> listIterator(int location);
    @Override
    public E remove(int location) {
        try {
            ListIterator<E> it = listIterator(location);
            E result = it.next();
            it.remove();
            return result;
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException();
        }
    }
    @Override
    public E set(int location, E object) {
        ListIterator<E> it = listIterator(location);
        if (!it.hasNext()) {
            throw new IndexOutOfBoundsException();
        }
        E result = it.next();
        it.set(object);
        return result;
    }
}
