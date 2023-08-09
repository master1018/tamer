public abstract class ForwardingListIterator<E> extends ForwardingIterator<E>
    implements ListIterator<E> {
  @Override protected abstract ListIterator<E> delegate();
  public void add(E element) {
    delegate().add(element);
  }
  public boolean hasPrevious() {
    return delegate().hasPrevious();
  }
  public int nextIndex() {
    return delegate().nextIndex();
  }
  public E previous() {
    return delegate().previous();
  }
  public int previousIndex() {
    return delegate().previousIndex();
  }
  public void set(E element) {
    delegate().set(element);
  }
}
