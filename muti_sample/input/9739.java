public class BytecodePutStatic extends BytecodeGetPut {
  BytecodePutStatic(Method method, int bci) {
    super(method, bci);
  }
  public boolean isStatic() {
    return true;
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check putstatic");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._putstatic;
  }
  public static BytecodePutStatic at(Method method, int bci) {
    BytecodePutStatic b = new BytecodePutStatic(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodePutStatic atCheck(Method method, int bci) {
    BytecodePutStatic b = new BytecodePutStatic(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodePutStatic at(BytecodeStream bcs) {
    return new BytecodePutStatic(bcs.method(), bcs.bci());
  }
}
