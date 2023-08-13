public class Interpreter {
  private static AddressField codeField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("AbstractInterpreter");
    codeField     = type.getAddressField("_code");
  }
  public Interpreter() {
  }
  public StubQueue getCode() {
    Address code = codeField.getValue();
    if (code == null) return null;
    return new StubQueue(code, InterpreterCodelet.class);
  }
  public boolean contains(Address pc) {
    return getCode().contains(pc);
  }
  public InterpreterCodelet getCodeletContaining(Address pc) {
    return (InterpreterCodelet) getCode().getStubContaining(pc);
  }
}
