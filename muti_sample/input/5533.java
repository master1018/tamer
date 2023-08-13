public class ConstantLongValue extends ScopeValue {
  private long value;
  public ConstantLongValue(long value) {
    this.value = value;
  }
  public boolean isConstantLong() {
    return true;
  }
  public long getValue() {
    return value;
  }
  ConstantLongValue(DebugInfoReadStream stream) {
    value = stream.readLong();
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print(value);
  }
}
