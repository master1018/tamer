public abstract class AbstractIterator<T> extends UnmodifiableIterator<T> {
  private State state = State.NOT_READY;
  private enum State {
    READY,
    NOT_READY,
    DONE,
    FAILED,
  }
  private T next;
  protected abstract T computeNext();
  protected final T endOfData() {
    state = State.DONE;
    return null;
  }
  public final boolean hasNext() {
    checkState(state != State.FAILED);
    switch (state) {
      case DONE:
        return false;
      case READY:
        return true;
      default:
    }
    return tryToComputeNext();
  }
  private boolean tryToComputeNext() {
    state = State.FAILED; 
    next = computeNext();
    if (state != State.DONE) {
      state = State.READY;
      return true;
    }
    return false;
  }
  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    state = State.NOT_READY;
    return next;
  }
  public final T peek() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return next;
  }
}
