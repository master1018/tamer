public abstract class ForwardingQueue<E> extends ForwardingCollection<E>
    implements Queue<E> {
  @Override protected abstract Queue<E> delegate();
  public boolean offer(E o) {
    return delegate().offer(o);
  }
  public E poll() {
    return delegate().poll();
  }
  public E remove() {
    return delegate().remove();
  }
  public E peek() {
    return delegate().peek();
  }
  public E element() {
    return delegate().element();
  }
}
