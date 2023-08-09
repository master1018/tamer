public class BytecodeLoad extends BytecodeLoadStore {
  BytecodeLoad(Method method, int bci) {
    super(method, bci);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check load");
    }
  }
  public boolean isValid() {
    int jcode = javaCode();
    switch (jcode) {
       case Bytecodes._iload:
       case Bytecodes._lload:
       case Bytecodes._fload:
       case Bytecodes._dload:
       case Bytecodes._aload:
          return true;
       default:
          return false;
    }
  }
  public static BytecodeLoad at(Method method, int bci) {
    BytecodeLoad b = new BytecodeLoad(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeLoad atCheck(Method method, int bci) {
    BytecodeLoad b = new BytecodeLoad(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeLoad at(BytecodeStream bcs) {
    return new BytecodeLoad(bcs.method(), bcs.bci());
  }
}
