public class BytecodeGetField extends BytecodeGetPut {
  BytecodeGetField(Method method, int bci) {
    super(method, bci);
  }
  public boolean isStatic() {
    return false;
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check getfield");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._getfield;
  }
  public static BytecodeGetField at(Method method, int bci) {
    BytecodeGetField b = new BytecodeGetField(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeGetField atCheck(Method method, int bci) {
    BytecodeGetField b = new BytecodeGetField(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeGetField at(BytecodeStream bcs) {
    return new BytecodeGetField(bcs.method(), bcs.bci());
  }
}
