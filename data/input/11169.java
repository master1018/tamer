public class BytecodeWithKlass extends BytecodeWithCPIndex {
  BytecodeWithKlass(Method method, int bci) {
    super(method, bci);
  }
  protected Klass getKlass() {
    return method().getConstants().getKlassRefAt(index());
  }
  public Symbol getClassName() {
    ConstantPool.CPSlot obj = method().getConstants().getSlotAt(index());
    if (obj.isMetaData()) {
      return obj.getSymbol();
    } else {
      return ((Klass)obj.getOop()).getName();
    }
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(index()));
    buf.append(spaces);
    buf.append("[Class ");
    buf.append(getClassName().asString().replace('/', '.'));
    buf.append(']');
    if (code() != javaCode()) {
       buf.append(spaces);
       buf.append('[');
       buf.append(getBytecodeName());
       buf.append(']');
    }
    return buf.toString();
  }
}
