public abstract class BytecodeGetPut extends BytecodeWithCPIndex {
  BytecodeGetPut(Method method, int bci) {
    super(method, bci);
  }
  public Symbol name() {
    ConstantPool cp = method().getConstants();
    return cp.getNameRefAt(index());
  }
  public Symbol signature() {
    ConstantPool cp = method().getConstants();
    return cp.getSignatureRefAt(index());
  }
  public Field getField() {
    return method().getConstants().getFieldRefAt(index());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(indexForFieldOrMethod()));
    buf.append(" [Field ");
    StringBuffer sigBuf = new StringBuffer();
    new SignatureConverter(signature(), sigBuf).dispatchField();
    buf.append(sigBuf.toString().replace('/', '.'));
    buf.append(spaces);
    buf.append(name().asString());
    buf.append("]");
    if (code() != javaCode()) {
       buf.append(spaces);
       buf.append('[');
       buf.append(getBytecodeName());
       buf.append(']');
    }
    return buf.toString();
  }
  public abstract boolean isStatic();
}
