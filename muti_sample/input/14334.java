public abstract class MemoizedLong {
  private boolean computed;
  private long value;
  protected abstract long computeValue();
  public long getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
