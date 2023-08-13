public abstract class ForwardingSortedSet<E> extends ForwardingSet<E>
    implements SortedSet<E> {
  @Override protected abstract SortedSet<E> delegate();
  public Comparator<? super E> comparator() {
    return delegate().comparator();
  }
  public E first() {
    return delegate().first();
  }
  public SortedSet<E> headSet(E toElement) {
    return delegate().headSet(toElement);
  }
  public E last() {
    return delegate().last();
  }
  public SortedSet<E> subSet(E fromElement, E toElement) {
    return delegate().subSet(fromElement, toElement);
  }
  public SortedSet<E> tailSet(E fromElement) {
    return delegate().tailSet(fromElement);
  }
}
