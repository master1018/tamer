public class BytecodeIinc extends BytecodeWideable {
  BytecodeIinc(Method method, int bci) {
    super(method, bci);
  }
  public int getIncrement() {
    return (isWide()) ? (int) javaShortAt(3) : (int) javaByteAt(2);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check iinc");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._iinc;
  }
  public static BytecodeIinc at(Method method, int bci) {
    BytecodeIinc b = new BytecodeIinc(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeIinc atCheck(Method method, int bci) {
    BytecodeIinc b = new BytecodeIinc(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeIinc at(BytecodeStream bcs) {
    return new BytecodeIinc(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("iinc");
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(getLocalVarIndex()));
    buf.append(" by ");
    buf.append(Integer.toString(getIncrement()));
    return buf.toString();
  }
}
