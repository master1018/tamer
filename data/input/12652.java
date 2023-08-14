public class BytecodeGoto extends BytecodeJmp {
  BytecodeGoto(Method method, int bci) {
    super(method, bci);
  }
  public int getTargetBCI() {
    return bci() + javaShortAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check goto");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._goto;
  }
  public static BytecodeGoto at(Method method, int bci) {
    BytecodeGoto b = new BytecodeGoto(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeGoto atCheck(Method method, int bci) {
    BytecodeGoto b = new BytecodeGoto(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeGoto at(BytecodeStream bcs) {
    return new BytecodeGoto(bcs.method(), bcs.bci());
  }
}
