public class BytecodeANewArray extends BytecodeWithKlass {
  BytecodeANewArray(Method method, int bci) {
    super(method, bci);
  }
  public Klass getKlass() {
    return super.getKlass();
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check anewarray");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._anewarray;
  }
  public static BytecodeANewArray at(Method method, int bci) {
    BytecodeANewArray b = new BytecodeANewArray(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeANewArray atCheck(Method method, int bci) {
    BytecodeANewArray b = new BytecodeANewArray(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeANewArray at(BytecodeStream bcs) {
    return new BytecodeANewArray(bcs.method(), bcs.bci());
  }
}
