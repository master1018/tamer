public abstract class MemoizedFloat {
  private boolean computed;
  private float value;
  protected abstract float computeValue();
  public float getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
