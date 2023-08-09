public class BytecodePutField extends BytecodeGetPut {
  BytecodePutField(Method method, int bci) {
    super(method, bci);
  }
  public boolean isStatic() {
    return false;
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check putfield");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._putfield;
  }
  public static BytecodePutField at(Method method, int bci) {
    BytecodePutField b = new BytecodePutField(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodePutField atCheck(Method method, int bci) {
    BytecodePutField b = new BytecodePutField(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodePutField at(BytecodeStream bcs) {
    return new BytecodePutField(bcs.method(), bcs.bci());
  }
}
