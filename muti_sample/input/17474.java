public abstract class MemoizedByte {
  private boolean computed;
  private byte value;
  protected abstract byte computeValue();
  public byte getValue() {
    if (!computed) {
      value = computeValue();
      computed = true;
    }
    return value;
  }
}
