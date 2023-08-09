public class BytecodeNewArray extends Bytecode {
  BytecodeNewArray(Method method, int bci) {
    super(method, bci);
  }
  public int getType() {
    return (int) javaByteAt(1);
  }
  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check newarray");
    }
  }
  public boolean isValid() {
    boolean result = javaCode() == Bytecodes._newarray;
    if (result == false) return false;
    switch (getType()) {
       case TypeArrayKlass.T_BOOLEAN:
       case TypeArrayKlass.T_CHAR:
       case TypeArrayKlass.T_FLOAT:
       case TypeArrayKlass.T_DOUBLE:
       case TypeArrayKlass.T_BYTE:
       case TypeArrayKlass.T_SHORT:
       case TypeArrayKlass.T_INT:
       case TypeArrayKlass.T_LONG:
          break;
       default:
          return false;
     }
     return true;
  }
  public String getTypeName() {
     String result;
     switch (getType()) {
       case TypeArrayKlass.T_BOOLEAN:
          result = "boolean";
          break;
       case TypeArrayKlass.T_CHAR:
          result = "char";
          break;
       case TypeArrayKlass.T_FLOAT:
          result = "float";
          break;
       case TypeArrayKlass.T_DOUBLE:
          result = "double";
          break;
       case TypeArrayKlass.T_BYTE:
          result = "byte";
          break;
       case TypeArrayKlass.T_SHORT:
          result = "short";
          break;
       case TypeArrayKlass.T_INT:
          result = "int";
          break;
       case TypeArrayKlass.T_LONG:
          result = "long";
          break;
       default: 
          result = "<invalid>";
          break;
     }
     return result;
  }
  public static BytecodeNewArray at(Method method, int bci) {
    BytecodeNewArray b = new BytecodeNewArray(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }
  public static BytecodeNewArray atCheck(Method method, int bci) {
    BytecodeNewArray b = new BytecodeNewArray(method, bci);
    return (b.isValid() ? b : null);
  }
  public static BytecodeNewArray at(BytecodeStream bcs) {
    return new BytecodeNewArray(bcs.method(), bcs.bci());
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("newarray");
    buf.append(spaces);
    buf.append(getTypeName());
    return buf.toString();
  }
}
