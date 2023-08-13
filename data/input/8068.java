public class BytecodeFastIAccess0 extends BytecodeGetPut {
  BytecodeFastIAccess0(Method method, int bci) {
    super(method, bci);
  }
  public int index() {
    return (int) (0xFF & javaShortAt(2));
  }
  public boolean isStatic() {
    return false;
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check fast_iaccess_0");
    }
  }
  public boolean isValid() {
    return code() == Bytecodes._fast_iaccess_0;
  }
  public static BytecodeFastIAccess0 at(Method method, int bci) {
    BytecodeFastIAccess0 b = new BytecodeFastIAccess0(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeFastIAccess0 atCheck(Method method, int bci) {
    BytecodeFastIAccess0 b = new BytecodeFastIAccess0(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeFastIAccess0 at(BytecodeStream bcs) {
    return new BytecodeFastIAccess0(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("aload_0");
    buf.append(spaces);
    buf.append(super.toString());
    return buf.toString();
  }
}
