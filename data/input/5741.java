public class BytecodeStore extends BytecodeLoadStore {
  BytecodeStore(Method method, int bci) {
    super(method, bci);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check store");
    }
  }
  public boolean isValid() {
    int jcode = javaCode();
    switch (jcode) {
       case Bytecodes._istore:
       case Bytecodes._lstore:
       case Bytecodes._fstore:
       case Bytecodes._dstore:
       case Bytecodes._astore:
          return true;
       default:
          return false;
    }
  }
  public static BytecodeStore at(Method method, int bci) {
    BytecodeStore b = new BytecodeStore(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeStore atCheck(Method method, int bci) {
    BytecodeStore b = new BytecodeStore(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeStore at(BytecodeStream bcs) {
    return new BytecodeStore(bcs.method(), bcs.bci());
  }
}
