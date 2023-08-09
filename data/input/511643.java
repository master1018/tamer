public abstract class ForwardingList<E> extends ForwardingCollection<E>
    implements List<E> {
  @Override protected abstract List<E> delegate();
  public void add(int index, E element) {
    delegate().add(index, element);
  }
  public boolean addAll(int index, Collection<? extends E> elements) {
    return delegate().addAll(index, elements);
  }
  public E get(int index) {
    return delegate().get(index);
  }
  public int indexOf(Object element) {
    return delegate().indexOf(element);
  }
  public int lastIndexOf(Object element) {
    return delegate().lastIndexOf(element);
  }
  public ListIterator<E> listIterator() {
    return delegate().listIterator();
  }
  public ListIterator<E> listIterator(int index) {
    return delegate().listIterator(index);
  }
  public E remove(int index) {
    return delegate().remove(index);
  }
  public E set(int index, E element) {
    return delegate().set(index, element);
  }
  @GwtIncompatible("List.subList")
  public List<E> subList(int fromIndex, int toIndex) {
    return Platform.subList(delegate(), fromIndex, toIndex);
  }
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
