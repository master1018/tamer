public class BytecodeJsrW extends BytecodeJmp {
  BytecodeJsrW(Method method, int bci) {
    super(method, bci);
  }
  public int getTargetBCI() {
    return bci() + javaSignedWordAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check jsr_w");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._jsr_w;
  }
  public static BytecodeJsrW at(Method method, int bci) {
    BytecodeJsrW b = new BytecodeJsrW(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeJsrW atCheck(Method method, int bci) {
    BytecodeJsrW b = new BytecodeJsrW(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeJsrW at(BytecodeStream bcs) {
    return new BytecodeJsrW(bcs.method(), bcs.bci());
  }
}
