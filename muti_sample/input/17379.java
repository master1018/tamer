public class BytecodeNew extends BytecodeWithKlass {
  BytecodeNew(Method method, int bci) {
    super(method, bci);
  }
  public InstanceKlass getNewKlass() {
    return (InstanceKlass) getKlass();
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check new");
    }
  }
  public boolean isValid() {
    return javaCode() == Bytecodes._new;
  }
  public static BytecodeNew at(Method method, int bci) {
    BytecodeNew b = new BytecodeNew(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeNew atCheck(Method method, int bci) {
    BytecodeNew b = new BytecodeNew(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeNew at(BytecodeStream bcs) {
    return new BytecodeNew(bcs.method(), bcs.bci());
  }
}
