public class BytecodeGotoW extends BytecodeJmp {
  BytecodeGotoW(Method method, int bci) {
    super(method, bci);
  }
  public int getTargetBCI() {
    return bci() + javaSignedWordAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check goto_w");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._goto_w;
  }
  public static BytecodeGotoW at(Method method, int bci) {
    BytecodeGotoW b = new BytecodeGotoW(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeGotoW atCheck(Method method, int bci) {
    BytecodeGotoW b = new BytecodeGotoW(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeGotoW at(BytecodeStream bcs) {
    return new BytecodeGotoW(bcs.method(), bcs.bci());
  }
}
