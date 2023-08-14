public abstract class ForwardingSet<E> extends ForwardingCollection<E>
    implements Set<E> {
  @Override protected abstract Set<E> delegate();
  @Override public boolean equals(@Nullable Object object) {
    return object == this || delegate().equals(object);
  }
  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
