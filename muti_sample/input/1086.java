public abstract class MemoizedInt {
  private boolean computed;
  private int value;
  protected abstract int computeValue();
  public int getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
