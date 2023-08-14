public class ConstantIntValue extends ScopeValue {
  private int value;
  public ConstantIntValue(int value) {
    this.value = value;
  }
  public boolean isConstantInt() {
    return true;
  }
  public int getValue() {
    return value;
  }
  ConstantIntValue(DebugInfoReadStream stream) {
    value = stream.readSignedInt();
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print(value);
  }
}
