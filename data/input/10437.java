public class BytecodeJsr extends BytecodeJmp {
  BytecodeJsr(Method method, int bci) {
    super(method, bci);
  }
  public int getTargetBCI() {
    return bci() + javaShortAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check jsr");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._jsr;
  }
  public static BytecodeJsr at(Method method, int bci) {
    BytecodeJsr b = new BytecodeJsr(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeJsr atCheck(Method method, int bci) {
    BytecodeJsr b = new BytecodeJsr(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeJsr at(BytecodeStream bcs) {
    return new BytecodeJsr(bcs.method(), bcs.bci());
  }
}
