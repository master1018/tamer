public class RuntimeStub extends CodeBlob {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("RuntimeStub");
  }
  public RuntimeStub(Address addr) {
    super(addr);
  }
  public boolean isRuntimeStub() {
    return true;
  }
  public String getName() {
    return "RuntimeStub: " + super.getName();
  }
}
