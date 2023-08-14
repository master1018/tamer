public abstract class ForwardingMultiset<E> extends ForwardingCollection<E>
    implements Multiset<E> {
  @Override protected abstract Multiset<E> delegate();
  public int count(Object element) {
    return delegate().count(element);
  }
  public int add(E element, int occurrences) {
    return delegate().add(element, occurrences);
  }
  public int remove(Object element, int occurrences) {
    return delegate().remove(element, occurrences);
  }
  public Set<E> elementSet() {
    return delegate().elementSet();
  }
  public Set<Entry<E>> entrySet() {
    return delegate().entrySet();
  }
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
  public int setCount(E element, int count) {
    return delegate().setCount(element, count);
  }
  public boolean setCount(E element, int oldCount, int newCount) {
    return delegate().setCount(element, oldCount, newCount);
  }
}
