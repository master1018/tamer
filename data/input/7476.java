public class BytecodeRet extends BytecodeWideable {
  BytecodeRet(Method method, int bci) {
    super(method, bci);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check ret");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._ret;
  }
  public static BytecodeRet at(Method method, int bci) {
    BytecodeRet b = new BytecodeRet(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeRet atCheck(Method method, int bci) {
    BytecodeRet b = new BytecodeRet(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeRet at(BytecodeStream bcs) {
    return new BytecodeRet(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("ret");
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(getLocalVarIndex()));
    return buf.toString();
  }
}
