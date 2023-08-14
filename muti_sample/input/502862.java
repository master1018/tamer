public abstract class FinalizablePhantomReference<T>
    extends PhantomReference<T> implements FinalizableReference {
  protected FinalizablePhantomReference(T referent,
      FinalizableReferenceQueue queue) {
    super(referent, queue.queue);
    queue.cleanUp();
  }
}
