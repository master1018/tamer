public abstract class ForwardingIterator<T>
    extends ForwardingObject implements Iterator<T> {
  @Override protected abstract Iterator<T> delegate();
  public boolean hasNext() {
    return delegate().hasNext();
  }
  public T next() {
    return delegate().next();
  }
  public void remove() {
    delegate().remove();
  }
}
