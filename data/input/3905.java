public abstract class BytecodeJmp extends Bytecode {
  BytecodeJmp(Method method, int bci) {
    super(method, bci);
  }
  public abstract int getTargetBCI();
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append(Integer.toString(getTargetBCI()));
    return buf.toString();
  }
}
