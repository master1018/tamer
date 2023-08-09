public class BytecodeBipush extends Bytecode {
  BytecodeBipush(Method method, int bci) {
    super(method, bci);
  }
  public byte getValue() {
    return javaByteAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check bipush");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._bipush;
  }
  public static BytecodeBipush at(Method method, int bci) {
    BytecodeBipush b = new BytecodeBipush(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeBipush atCheck(Method method, int bci) {
    BytecodeBipush b = new BytecodeBipush(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeBipush at(BytecodeStream bcs) {
    return new BytecodeBipush(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("bipush");
    buf.append(spaces);
    buf.append(Byte.toString(getValue()));
    return buf.toString();
  }
}
