public abstract class MemoizedChar {
  private boolean computed;
  private char value;
  protected abstract char computeValue();
  public char getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
