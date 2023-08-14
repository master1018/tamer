public abstract class MemoizedBoolean {
  private boolean computed;
  private boolean value;
  protected abstract boolean computeValue();
  public boolean getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
