public abstract class MemoizedObject {
  private boolean computed;
  private Object value;
  protected abstract Object computeValue();
  public Object getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
