public class BytecodeIf extends BytecodeJmp {
  BytecodeIf(Method method, int bci) {
    super(method, bci);
  }
  public int getTargetBCI() {
    return bci() + javaShortAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check if");
    }
  }
  public boolean isValid() {
    int jcode = javaCode();
    return (jcode >= Bytecodes._ifeq && jcode <= Bytecodes._if_acmpne) ||
           jcode == Bytecodes._ifnull || jcode == Bytecodes._ifnonnull;
  }
  public static BytecodeIf at(Method method, int bci) {
    BytecodeIf b = new BytecodeIf(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeIf atCheck(Method method, int bci) {
    BytecodeIf b = new BytecodeIf(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeIf at(BytecodeStream bcs) {
    return new BytecodeIf(bcs.method(), bcs.bci());
  }
}
