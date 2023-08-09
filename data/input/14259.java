public class BytecodeSipush extends Bytecode {
  BytecodeSipush(Method method, int bci) {
    super(method, bci);
  }
  public short getValue() {
    return javaShortAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check sipush");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._sipush;
  }
  public static BytecodeSipush at(Method method, int bci) {
    BytecodeSipush b = new BytecodeSipush(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeSipush atCheck(Method method, int bci) {
    BytecodeSipush b = new BytecodeSipush(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeSipush at(BytecodeStream bcs) {
    return new BytecodeSipush(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("sipush");
    buf.append(spaces);
    buf.append(Short.toString(getValue()));
    return buf.toString();
  }
}
