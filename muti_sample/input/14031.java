public class BytecodeInvoke extends BytecodeWithCPIndex {
  BytecodeInvoke(Method method, int bci) {
    super(method, bci);
  }
  public static BytecodeInvoke at(Method method, int bci) {
    BytecodeInvoke b = new BytecodeInvoke(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeInvoke atCheck(Method method, int bci) {
    BytecodeInvoke b = new BytecodeInvoke(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeInvoke at(BytecodeStream bcs) {
    return new BytecodeInvoke(bcs.method(), bcs.bci());
  }
  public Symbol name() {
    ConstantPool cp = method().getConstants();
    if (isInvokedynamic()) {
       int[] nt = cp.getNameAndTypeAt(indexForFieldOrMethod());
       return cp.getSymbolAt(nt[0]);
    }
    return cp.getNameRefAt(index());
  }
  public Symbol signature() {
    ConstantPool cp = method().getConstants();
    if (isInvokedynamic()) {
       int[] nt = cp.getNameAndTypeAt(indexForFieldOrMethod());
       return cp.getSymbolAt(nt[1]);
    }
    return cp.getSignatureRefAt(index());
  }
  public int getSecondaryIndex() {
    if (isInvokedynamic()) {
      return VM.getVM().getBytes().swapInt(javaSignedWordAt(1));
    }
    return super.getSecondaryIndex();  
  }
  public Method getInvokedMethod() {
    return method().getConstants().getMethodRefAt(index());
  }
  public int resultType() {
    ResultTypeFinder rts = new ResultTypeFinder(signature());
    rts.iterate();
    return rts.type();
  }
  public int adjustedInvokeCode() {
    return javaCode();
  }
  public boolean isInvokeinterface() { return adjustedInvokeCode() == Bytecodes._invokeinterface; }
  public boolean isInvokevirtual()   { return adjustedInvokeCode() == Bytecodes._invokevirtual;   }
  public boolean isInvokestatic()    { return adjustedInvokeCode() == Bytecodes._invokestatic;    }
  public boolean isInvokespecial()   { return adjustedInvokeCode() == Bytecodes._invokespecial;   }
  public boolean isInvokedynamic()   { return adjustedInvokeCode() == Bytecodes._invokedynamic; }
  public boolean isValid()           { return isInvokeinterface() ||
                                              isInvokevirtual()   ||
                                              isInvokestatic()    ||
                                              isInvokespecial(); }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check invoke");
    }
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(indexForFieldOrMethod()));
    if (isInvokedynamic()) {
       buf.append('(');
       buf.append(Integer.toString(getSecondaryIndex()));
       buf.append(')');
    }
    buf.append(" [Method ");
    StringBuffer sigBuf = new StringBuffer();
    new SignatureConverter(signature(), sigBuf).iterateReturntype();
    buf.append(sigBuf.toString().replace('/', '.'));
    buf.append(spaces);
    buf.append(name().asString());
    buf.append('(');
    sigBuf = new StringBuffer();
    new SignatureConverter(signature(), sigBuf).iterateParameters();
    buf.append(sigBuf.toString().replace('/', '.'));
    buf.append(')');
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
