public class BytecodeTableswitch extends Bytecode {
  BytecodeTableswitch(Method method, int bci) {
    super(method, bci);
  }
  public int  defaultOffset()     { return javaSignedWordAt(alignedOffset(1 + 0*jintSize)); }
  public int  lowKey()            { return javaSignedWordAt(alignedOffset(1 + 1*jintSize)); }
  public int  highKey()           { return javaSignedWordAt(alignedOffset(1 + 2*jintSize)); }
  public int  length()            { return highKey()-lowKey()+1; }
  public int  destOffsetAt(int i) {
    int x2 = alignedOffset(1 + (3 + i)*jintSize);
    int val = javaSignedWordAt(x2);
    return javaSignedWordAt(alignedOffset(1 + (3 + i)*jintSize));
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check tableswitch");
    }
  }
  public boolean isValid() {
    boolean result = javaCode() == Bytecodes._tableswitch;
    if (result == false) return false;
    int lo = lowKey();
    int hi = highKey();
    if (hi < lo) 
       return false;
    int i  = hi - lo - 1 ;
    while (i-- > 0) {
    }
    return true;
  }
  public static BytecodeTableswitch at(Method method, int bci) {
    BytecodeTableswitch b = new BytecodeTableswitch(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeTableswitch atCheck(Method method, int bci) {
    BytecodeTableswitch b = new BytecodeTableswitch(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeTableswitch at(BytecodeStream bcs) {
    return new BytecodeTableswitch(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("tableswitch");
    buf.append(spaces);
    buf.append("default: ");
    buf.append(Integer.toString(bci() + defaultOffset()));
    buf.append(comma);
    int lo = lowKey();
    int hi = highKey();
    int i  = hi - lo - 1 ;
    while (i-- > 0) {
       buf.append("case ");
       buf.append(Integer.toString(lo + i));
       buf.append(':');
       buf.append(Integer.toString(bci() + destOffsetAt(i)));
       buf.append(comma);
    }
    return buf.toString();
  }
}
