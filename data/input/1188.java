public class ConstantOopReadValue extends ScopeValue {
  private OopHandle value;
  public ConstantOopReadValue(DebugInfoReadStream stream) {
    value = stream.readOopHandle();
  }
  public boolean isConstantOop() {
    return true;
  }
  public OopHandle getValue() {
    return value;
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print(value);
  }
}
