public abstract class MemoizedDouble {
  private boolean computed;
  private double value;
  protected abstract double computeValue();
  public double getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
