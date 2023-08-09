public abstract class ForwardingCollection<E> extends ForwardingObject
    implements Collection<E> {
  @Override protected abstract Collection<E> delegate();
  public Iterator<E> iterator() {
    return delegate().iterator();
  }
  public int size() {
    return delegate().size();
  }
  public boolean removeAll(Collection<?> collection) {
    return delegate().removeAll(collection);
  }
  public boolean isEmpty() {
    return delegate().isEmpty();
  }
  public boolean contains(Object object) {
    return delegate().contains(object);
  }
  public Object[] toArray() {
    return delegate().toArray();
  }
  public <T> T[] toArray(T[] array) {
    return delegate().toArray(array);
  }
  public boolean add(E element) {
    return delegate().add(element);
  }
  public boolean remove(Object object) {
    return delegate().remove(object);
  }
  public boolean containsAll(Collection<?> collection) {
    return delegate().containsAll(collection);
  }
  public boolean addAll(Collection<? extends E> collection) {
    return delegate().addAll(collection);
  }
  public boolean retainAll(Collection<?> collection) {
    return delegate().retainAll(collection);
  }
  public void clear() {
    delegate().clear();
  }
}
