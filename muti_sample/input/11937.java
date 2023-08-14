public abstract class MemoizedShort {
  private boolean computed;
  private short value;
  protected abstract short computeValue();
  public short getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
