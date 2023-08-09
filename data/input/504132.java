public abstract class FinalizableSoftReference<T> extends SoftReference<T>
    implements FinalizableReference {
  protected FinalizableSoftReference(T referent,
      FinalizableReferenceQueue queue) {
    super(referent, queue.queue);
    queue.cleanUp();
  }
}
