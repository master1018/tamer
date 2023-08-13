public class ConstantDoubleValue extends ScopeValue {
  private double value;
  public ConstantDoubleValue(double value) {
    this.value = value;
  }
  public boolean isConstantDouble() {
    return true;
  }
  public double getValue() {
    return value;
  }
  ConstantDoubleValue(DebugInfoReadStream stream) {
    value = stream.readDouble();
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print(value);
  }
}
